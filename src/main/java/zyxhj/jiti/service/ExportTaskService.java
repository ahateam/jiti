package zyxhj.jiti.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alicloud.openservices.tablestore.SyncClient;

import io.vertx.core.Vertx;
import zyxhj.core.domain.ExportTask;
import zyxhj.core.repository.ExportTaskRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.UploadFile;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;

public class ExportTaskService {

	private static Logger log = LoggerFactory.getLogger(ExportTaskService.class);
	private ExportTaskRepository taskRepository;
	private static ORGUserService orgUserService;
	private static ORGService orgService;
	private static UploadFile uploadFile;

	public ExportTaskService() {
		try {

			taskRepository = Singleton.ins(ExportTaskRepository.class);
			orgUserService = Singleton.ins(ORGUserService.class);
			orgService = Singleton.ins(ORGService.class);
			uploadFile = Singleton.ins(UploadFile.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 创建导出任务
	public ExportTask createExportTask(DruidPooledConnection conn, String title, Long orgId, Long userId, Long areaId)
			throws Exception {
		ExportTask imp = new ExportTask();
		imp.id = IDUtils.getSimpleId();
		imp.areaId = areaId;
		imp.orgId = orgId;
		imp.title = title;
		imp.userId = userId;
		imp.createTime = new Date();
		imp.startTime = new Date();
		imp.finishTime = new Date();
		imp.amount = 0;
		imp.completedCount = 0;
		imp.successCount = 0;
		imp.failureCount = 0;
		imp.status = ExportTask.STATUS.WAITING.v();
		taskRepository.insert(conn, imp);
		return imp;
	}

	public List<ExportTask> getExportTaskLikeTitle(DruidPooledConnection conn, Long areaId, String title, Integer count,
			Integer offset) throws ServerException {
		if (StringUtils.isBlank(title)) {
			return taskRepository.getList(conn, EXP.INS().key("area_id", areaId).append("ORDER BY create_time DESC"),
					count, offset);
		} else {
			return taskRepository.getList(conn, EXP.INS().key("area_id", areaId).LIKE("title", title), count, offset);
		}

	}

	// 查询导出任务列表
	public List<ExportTask> getExportTaskList(DruidPooledConnection conn, Long areaId, Integer count, Integer offset)
			throws Exception {
		return taskRepository.getList(conn, EXP.INS().key("area_id", areaId).append("ORDER BY create_time DESC"), count,
				offset);
	}

	public ExportTask getExportTask(DruidPooledConnection conn, Long taskId) throws Exception {
		return taskRepository.get(conn, EXP.INS().key("id", taskId));
	}

	// 写入数据到Excel
	public void exportData(Long orgId, Long taskId) throws Exception {

		System.out.println("进入ExportDataIntoOSS");
		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DruidDataSource dds;
			DruidPooledConnection conn = null;
			SyncClient client = null;
			try {
				dds = DataSource.getDruidDataSource("rdsDefault.prop");
				conn = (DruidPooledConnection) dds.getConnection();
				client = DataSource.getTableStoreSyncClient("tsDefault.prop");
			} catch (Exception e) {
				e.printStackTrace();
			}

			// 修改任务状态为正在生成文件
			ExportTask exp = new ExportTask();
			exp.status = ExportTask.STATUS.PROGRESSING.v();
			// 总数
			int size;
			try {
				size = orgUserService.getExportDataCount(conn, orgId);
				exp.amount = size;
				exp.startTime = new Date();
				taskRepository.update(conn, EXP.INS().key("id", taskId), exp, true);
				if (size > 0) {

					XSSFWorkbook dataListExcel = new XSSFWorkbook();
					// 2.在workbook中添加一个sheet,对应Excel文件中的sheet
					XSSFSheet sheet = dataListExcel.createSheet("sheet1");
					// 3.设置表头，即每个列的列名
					String[] titles = new String[] { "户序号", "户主姓名", "地址", "姓名", "性别", "身份证号码", "是否集体组织成员", "个人持股数（股）",
							"与户主关系", "成员股权证号", "本户资产股", "本户资源股", "合作社名称", "合作社地址", "合作社成立时间", "合作社信用代码", "集体资产股",
							"原合作社集体资产股", "集体资源股", "原合作社集体资源股" };
					// 3.1创建第一行
					XSSFRow row = sheet.createRow(0);
//			        // 此处创建一个序号列
					// 将列名写入
					for (int i = 0; i < titles.length; i++) {
						// 给列写入数据,创建单元格，写入数据
						row.createCell(i).setCellValue(titles[i]);
					}

					Integer count = 0;
					Integer offset = 0;
					int forLength = 0;
					if (size < 100) {
						forLength = 1;
						count = size;
					} else {
						forLength = 100;
						count = size / 100;
						if (size % 100 > 0) {
							forLength++;
						}
					}
					List<Map<String, Object>> ecportDataList = new ArrayList<Map<String, Object>>();
					System.out.println("开始导出数据");

					for (int i = 0; i < forLength; i++) {
						if (size % 100 > 0) {
							if (i == 100) {
								count = (size % 100);
								List<Map<String, Object>> DataList = orgUserService.getExportData(conn, orgId, count,
										offset);
								ecportDataList.addAll(DataList);
							} else {
								List<Map<String, Object>> DataList = orgUserService.getExportData(conn, orgId, count,
										offset);
								ecportDataList.addAll(DataList);
							}
						} else {
							List<Map<String, Object>> DataList = orgUserService.getExportData(conn, orgId, count,
									offset);
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
					// 将文件上传到OSS
					String url = ExportDataIntoOSS(dataListExcel, org.name);
					// 修改任务状态为文件已生成
					exp.completedCount = offset;
					exp.failureCount = size - offset;
					exp.fileUrls = url;
					exp.status = ExportTask.STATUS.FILE_READY.v();
					exp.finishTime = new Date();
					taskRepository.update(conn, EXP.INS().key("id", taskId), exp, true);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			future.complete("ok");
		}, res -> {
			System.out.println("The result is: " + res.result());
		});
	}

	public String ExportDataIntoOSS(XSSFWorkbook dataListExcel, String orgName) {

		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			dataListExcel.write(outputStream);
			InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
			// 生成文件名
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
			String OSSCatalogue = uploadFile.OSSCATALOGUE_PRINT_EXCEL;
			String fileName = orgName + "" + dateFormat.format(date) + ".xlsx";
			if(OSSCatalogue==null) {
				System.out.println("OSSCatalogue==null");
			}
			if(inputStream==null) {
				System.out.println("inputStream==null");
			}

			if(fileName==null) {
				System.out.println("fileName==null");
			}
			// 上传到OSS
			String url = uploadFile.uploadFileToOSS(UploadFile.BUCKETNAME_JITI, OSSCatalogue, fileName, inputStream);
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
							String idNumber = listresult.get(i).get(key).toString();
							if(!StringUtils.isBlank(idNumber)) {
								if(idNumber.length()<8 || idNumber.length() > 20  ) {
									if(idNumber.length()==25 && idNumber.indexOf("年")>0) {
										idNumber = idNumber.substring(0, 11);
									}else {
										idNumber = "";
									}
								}
							}
							row.createCell(5).setCellValue(idNumber);
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
