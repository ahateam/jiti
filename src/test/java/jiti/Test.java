package jiti;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.core.domain.ExportTask;
import zyxhj.core.repository.ExportTaskRepository;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.controller.ORGController;
import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGExamine;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.repository.ExamineRepository;
import zyxhj.jiti.repository.ORGExamineRepository;
import zyxhj.jiti.repository.ORGRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.service.FeedbackService;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.jiti.service.SingleCertificateTaskService;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;

public class Test {

	private static DruidPooledConnection conn;

	static {
		try {
			conn = DataSource.getDruidDataSource("rdsDefault.prop").getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ORGService orgService = new ORGService();
	private static ORGUserService orgUserService = new ORGUserService();
	private static SingleCertificateTaskService scftService = new SingleCertificateTaskService();

	public static void main(String[] args) throws Exception {

		testesss();
	}

	@org.junit.Test
	public void testAAA() throws ServerException {

		int[] a = { 101, 102 };

		JSONArray js = (JSONArray) JSONArray.toJSON(a);
		js.add(103);
		System.out.println(js.toJSONString());

		EXP tag = testJsonContainsORKey(js, "roles", null);

		EXP tags = EXP.INS().key("org_id", 400942027079516L).and(tag);

		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		tags.toSQL(sb, params);
		System.out.println(sb.toString());

		ORGUserRepository user = new ORGUserRepository();

		List<ORGUser> userlist = user.getList(conn, tags, 10, 0);

		System.out.println(userlist.size());

	}

	public EXP testJsonContainsORKey(JSONArray tags, String column, String tagGroup) throws ServerException {

		if (tags == null && tags.size() == 0) {
			return null;
		}
		String path;
		if (tagGroup == null) {
			path = "$";
		} else {
			path = "$." + tagGroup;
		}
		EXP tag = EXP.INS();
		for (int i = 0; i < tags.size(); i++) {
			System.out.println(tags.get(i));
			tag.or(EXP.JSON_CONTAINS(column, path, tags.get(i)));
		}
		return tag;
	}

	public void testEditUserMobile() throws Exception {

		ORGController c = new ORGController("node");

		c.editUserMobile(400987736416750L, null, "");
		System.out.println();
	}

	@org.junit.Test
	public void testiiii() {
		try {
			dealExcel("C:\\Users\\Admin\\Desktop\\省标打印程序.xlsm", "C:\\Users\\Admin\\Desktop\\省标打印程序2.xlsm");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void dealExcel(String template, String out) throws Exception {
		if (!template.equals(out)) {
			copyFile(template, out);
		}
		FileInputStream input = new FileInputStream(new File(out));
		Workbook wb = WorkbookFactory.create(input);
		FileOutputStream output = new FileOutputStream(new File(out));
		Sheet sheet = wb.getSheetAt(0);
		System.out.println(sheet.getRow(0).getLastCellNum());
		XSSFCellStyle lockstyle = (XSSFCellStyle) wb.createCellStyle();
		lockstyle.setLocked(true);

		lockstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		int columnCount = -1;
		int rowCount = 0;
		sheet.createRow(1);
		for (int r = 1; r < 20; r++) {
			Row row = sheet.createRow(r);
			for (int c = 0; c < 20; c++) {
				Cell cell = row.createCell(c);
				cell.setCellValue("row-" + r + "---" + c);
				cell.setCellStyle(lockstyle);
			}
		}
		output.flush();
		wb.write(output);
		wb.close();
		output.close();
	}

	// 复制文件
	public static void copyFile(String in, String out) throws Exception {
		InputStream inputStream = new FileInputStream(new File(in));

		File copy = new File(out);
		if (copy.exists()) {
			copy.delete();
		}
		copy.createNewFile();
		OutputStream outputStream = new FileOutputStream(copy);

		byte[] buffer = new byte[1024 * 4];
		while ((inputStream.read(buffer)) != -1) {
			outputStream.write(buffer);
		}

		inputStream.close();
		outputStream.close();

	}

	@org.junit.Test
	public void testget() throws Exception {
		FeedbackService fservice = new FeedbackService();
		for (int i = 0; i < 100; i++) {
			fservice.createFeedback(conn, Long.valueOf(i), "ifdasfas" + i, "10086" + i);
		}
	}

	public void show() {
		for (int i = 1; i < 10; i++) {
			for (int j = 10 - i; j > 0; j--) {
				System.out.print(" ");
			}
			for (int j = 1; j < 2 * i; j++) {
				System.out.print("*");
			}
			System.out.println();
		}
		for (int i = 9; i > 0; i--) {
			for (int j = 10 - i; j > 0; j--) {
				System.out.print(" ");
			}
			for (int j = 1; j < 2 * i; j++) {
				System.out.print("*");
			}
			System.out.println();
		}
	}

	@org.junit.Test
	public void testssss() {
		try {
			orgService.delSubOrg(conn, 403008741706773L);
		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 修改文件地址
	 */
	public void eidtFilePath() {
		// 修改org表的文件地址
		// 获取所有组织
		try {

			editorg();
			editexp();
			edituser();
			editexamine();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@org.junit.Test
	public void editorg() throws Exception {
		ORGRepository org = new ORGRepository();
		List<ORG> orgs = org.getOrgs(conn);
		for (ORG o : orgs) {
			String imgOrg = o.imgOrg;
			String imgAuth = o.imgAuth;
			if (!StringUtils.isBlank(imgOrg)) {
				imgOrg = imgOrg.substring(imgOrg.indexOf(".com") + 5, imgOrg.length());
			}
			if (!StringUtils.isBlank(imgAuth)) {
				imgAuth = imgAuth.substring(imgAuth.indexOf(".com") + 5, imgAuth.length());
			}
			o.imgOrg = imgOrg;
			o.imgAuth = imgAuth;
			int i = org.update(conn, EXP.INS().key("id", o.id), o, true);
			System.out.println(i);
		}
	}

	@org.junit.Test
	public void editexp() throws Exception {
		// 修改导出数据文件地址
		ExportTaskRepository exp = new ExportTaskRepository();
		List<ExportTask> exps = exp.getExportTasks(conn);

		for (ExportTask o : exps) {
			String fileUrls = o.fileUrls;
			if (!StringUtils.isBlank(fileUrls)) {
				fileUrls = fileUrls.substring(fileUrls.indexOf(".com") + 5, fileUrls.length());
			}
			o.fileUrls = fileUrls;
			int i = exp.update(conn, EXP.INS().key("id", o.id), o, true);
			System.out.println(i);
		}

	}

	@org.junit.Test
	public void editexamine() throws Exception {
		// 修改审批表文件地址
		ORGExamineRepository exa = new ORGExamineRepository();
		List<ORGExamine> es = exa.getExamines(conn);
		for (ORGExamine o : es) {
			String imgOrg = o.imgOrg;
			String imgAuth = o.imgAuth;
			if (!StringUtils.isBlank(imgOrg)) {
				imgOrg = imgOrg.substring(imgOrg.indexOf(".com") + 5, imgOrg.length());
			}
			if (!StringUtils.isBlank(imgAuth)) {
				imgAuth = imgAuth.substring(imgAuth.indexOf(".com") + 5, imgAuth.length());
			}
			o.imgOrg = imgOrg;
			o.imgAuth = imgAuth;
			int i = exa.update(conn, EXP.INS().key("id", o.id), o, true);
			System.out.println(i);
		}

	}

	@org.junit.Test
	public void edituser() throws Exception {
		// 修改ORGUser表的
		ORGUserRepository user = new ORGUserRepository();
		List<ORGUser> users = user.getUserss(conn);

		for (ORGUser o : users) {
			String shareCerImg = o.shareCerImg;
			if (!StringUtils.isBlank(shareCerImg)) {
				shareCerImg = shareCerImg.substring(shareCerImg.indexOf(".com") + 5, shareCerImg.length());
			}
			o.shareCerImg = shareCerImg;
			int i = user.update(conn, EXP.INS().key("user_id", o.userId).andKey("org_id", o.orgId), o, true);
			System.out.println(i);
		}

	}

	@org.junit.Test
	public static void testPss() {
		List<Integer> list = new ArrayList<Integer>();
		list.add(1);
		list.add(1);
		list.add(1);
		list.add(1);
		list.add(2);
		list.add(2);
		list.add(2);
		list.add(3);
		list.add(3);
		list.add(3);
		List<Integer> list1 = new ArrayList<Integer>();
		int now = 0;
		int last = 0;
		int c = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < list.size(); j++) {
				if (i == 0 && j == 0) {
					last = now = list.get(j);
				}
				now = list.get(j);
				if (now == c) {
					System.out.print(now + "\t");
					now = last;
				} else if (now == last) {
					System.out.print(now + "\t");
					list1.add(now);
				} else if (last + 1 == now) {
					System.out.print(now + "\t");
					list1.add(now);
					last = now;
				} else if (last + 1 < now) {
					System.out.print(now + "\t");
					c = now;
					now = last + 1;
					last = now;
					list1.add(now);
				}
				System.out.println(now);
			}
			list.clear();
			list.add(5);
			list.add(5);
			list.add(5);
			list.add(5);
			list.add(25);
			list.add(25);
			list.add(28);
			list.add(29);
			list.add(31);
			list.add(32);
		}
//		for (int i = 0; i < list1.size(); i++) {
//			System.out.println(list1.get(i) + "\t");
//		}
	}
	
	public static void testesss() {
		String a = "2019年12月08日";
		System.out.println(a.length());
	}
}
