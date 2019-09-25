package zyxhj.jiti.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;

import zyxhj.core.domain.ExportTask;
import zyxhj.core.domain.ImportTask;
import zyxhj.core.repository.ExportTaskRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class ExportTaskService {

	private static Logger log = LoggerFactory.getLogger(ExportTaskService.class);
	private ExportTaskRepository taskRepository;
	private static ORGUserService orgUserService;
	private static ORGService orgService;

	public ExportTaskService() {
		try {

			taskRepository = Singleton.ins(ExportTaskRepository.class);
			orgUserService = Singleton.ins(ORGUserService.class);
			orgService = Singleton.ins(ORGService.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static String BUCKETNAME = "jitijingji-test1";

	private static String ENDPOINT = "http://oss-cn-hangzhou.aliyuncs.com";// endpoint
	private static String ACCESSKEYID = "LTAIJ9mYIjuW54Cj";// accessKeyId
	private static String ACCESSKEYSECRET = "89EMlXLsP13H8mWKIvdr4iM1OvdVxs";// accessKeySecret

	// 创建导出任务
	public ExportTask createExportTask(DruidPooledConnection conn, String title, Long orgId, Long userId)
			throws Exception {
		ExportTask imp = new ExportTask();
		imp.id = IDUtils.getSimpleId();
		imp.orgId = orgId;
		imp.title = title;
		imp.userId = userId;
		imp.createTime = new Date();
		imp.amount = 0;
		imp.completedCount = 0;
		imp.successCount = 0;
		imp.failureCount = 0;
		imp.status = ExportTask.STATUS.WAITING.v();
		taskRepository.insert(conn, imp);
		return imp;
	}

	// 查询导出任务列表
	public List<ExportTask> getExportTaskList(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		return taskRepository.getList(conn, null, count, offset);
	}

	public ExportTask getExportTask(DruidPooledConnection conn, Long taskId) throws Exception {
		return taskRepository.get(conn, EXP.INS().key("id", taskId));
	}

	// 导出数据到OSS
	public void ExportDataIntoOSS(DruidPooledConnection conn, Long orgId, Long taskId) throws Exception {

		System.out.println("进入ExportDataIntoOSS");

		// 修改任务状态为正在生成文件
		ExportTask exp = new ExportTask();
		exp.status = ExportTask.STATUS.PROGRESSING.v();
		// 总数
		int size = orgUserService.getExportDataCount(conn, orgId);
		exp.amount = size;
		exp.startTime = new Date();
		taskRepository.update(conn, EXP.INS().key("id", taskId), exp, true);

		Integer count = size / 100;
		Integer offset = 0;
		XSSFWorkbook dataListExcel = new XSSFWorkbook();
		// 2.在workbook中添加一个sheet,对应Excel文件中的sheet
		XSSFSheet sheet = dataListExcel.createSheet("sheet1");
		// 3.设置表头，即每个列的列名
		String[] titles = new String[] { "户序号", "户主姓名", "地址", "姓名", "性别", "身份证号码", "是否集体组织成员", "个人持股数（股）", "与户主关系",
				"成员股权证号", "本户资产股", "本户资源股", "合作社名称", "合作社地址", "合作社成立时间", "合作社信用代码", "集体资产股", "原合作社集体资产股", "集体资源股",
				"原合作社集体资源股" };
		// 3.1创建第一行
		XSSFRow row = sheet.createRow(0);
//        // 此处创建一个序号列
		// 将列名写入
		for (int i = 0; i < titles.length; i++) {
			// 给列写入数据,创建单元格，写入数据
			row.createCell(i).setCellValue(titles[i]);
		}

		// 添加数据到Excel表中
		System.out.println("offset:" + offset);

		List<Map<String, Object>> ecportDataList = new ArrayList<Map<String, Object>>();

		System.out.println("开始导出数据");
		for (int i = 0; i < 100; i++) {
			if (size % 100 > 0) {
				if (i == 99) {
					List<Map<String, Object>> DataList = orgUserService.getExportData(conn, orgId, (size % 100),
							offset);
					ecportDataList.addAll(DataList);
				} else {
					List<Map<String, Object>> DataList = orgUserService.getExportData(conn, orgId, count, offset);
					ecportDataList.addAll(DataList);
				}
			} else {
				List<Map<String, Object>> DataList = orgUserService.getExportData(conn, orgId, count, offset);
				ecportDataList.addAll(DataList);
			}
			offset += count;
			// 修改已经完成的数量
			exp.successCount = offset;
			taskRepository.update(conn, EXP.INS().key("id", taskId), exp, true);

		}

		exportDataModile(sheet, ecportDataList, row, titles);

		// 获取组织名称
		ORG org = orgService.getORGById(conn, orgId);
		String url = exportData(dataListExcel, org.name);
		// 修改任务状态为文件已生成
		exp.completedCount = offset;
		exp.failureCount = size - offset;
		exp.fileUrls = url;
		exp.status = ExportTask.STATUS.FILE_READY.v();
		exp.finishTime = new Date();
		taskRepository.update(conn, EXP.INS().key("id", taskId), exp, true);
	}

	public String exportData(XSSFWorkbook dataListExcel, String orgName) {

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			dataListExcel.write(outputStream);
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			// 上传到OSS
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
			// 当前下载时间作为文件名
			String fileName = "print-excel/" + orgName + "" + dateFormat.format(date) + ".xlsx";

			OSS ossClient = new OSSClientBuilder().build(ENDPOINT, ACCESSKEYID, ACCESSKEYSECRET);
			ossClient.putObject(BUCKETNAME, fileName, inputStream);
			// 关闭OSSClient。
			ossClient.shutdown();

			String url = "https://" + BUCKETNAME + ".oss-cn-hangzhou.aliyuncs.com/" + fileName;
			inputStream.close();
			outputStream.close();
			return url;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void exportDataModile(XSSFSheet sheet, List<Map<String, Object>> listresult, XSSFRow row, String[] titles) {
		for (int i = 0; i < listresult.size(); i++) {
			// 创建行
			row = sheet.createRow(i + 1);
			for (String key : listresult.get(i).keySet()) {
				for (String s : titles) {
					if (key.equals(s)) {
						if ("户序号".equals(s)) {
							row.createCell(0).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("户主姓名".equals(s)) {
							row.createCell(1).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("地址".equals(s)) {
							row.createCell(2).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("姓名".equals(s)) {
							row.createCell(3).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("性别".equals(s)) {
							row.createCell(4).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("身份证号码".equals(s)) {
							row.createCell(5).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("是否集体组织成员".equals(s)) {
							row.createCell(6).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("个人持股数（股）".equals(s)) {
							row.createCell(7).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("与户主关系".equals(s)) {
							row.createCell(8).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("成员股权证号".equals(s)) {
							row.createCell(9).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("成员资产股".equals(s)) {
							row.createCell(10).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("本户资源股".equals(s)) {
							row.createCell(11).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("合作社名称".equals(s)) {
							row.createCell(12).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("合作社地址".equals(s)) {
							row.createCell(13).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("合作社成立时间".equals(s)) {
							row.createCell(14).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("合作社信用代码".equals(s)) {
							row.createCell(15).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("集体资产股".equals(s)) {
							row.createCell(16).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("原合作社集体资产股".equals(s)) {
							row.createCell(17).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("集体资源股".equals(s)) {
							row.createCell(18).setCellValue(listresult.get(i).get(key).toString());
							break;
						} else if ("原合作社集体资源股".equals(s)) {
							row.createCell(19).setCellValue(listresult.get(i).get(key).toString());
							break;
						}
					}
				}
			}
		}
	}

}
