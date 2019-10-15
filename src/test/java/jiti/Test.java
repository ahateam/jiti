package jiti;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.controller.ORGController;
import zyxhj.jiti.controller.VersionController;
import zyxhj.jiti.controller.VoteController;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.jiti.service.VoteService;
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

	private static ORGService or = new ORGService();

	public static void main(String[] args) throws Exception {
		//
		// String phone = "[17353166886,18651813050]";
		// JSONArray json = new JSONArray();
		// json.add("18651813050");
		// json.add("17353166886");
		// // or.SendSms(phone);

		Integer j = 3508;
		// System.out.println((i/100)+1);

		for (int i = 0; i < (j / 100) + 1; i++) {
			System.out.println(i);
		}
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
	public void testExportDataIntoOSS() {
		try {

			// 导入数据到程序中
			XSSFWorkbook readXssfWorkbook = new XSSFWorkbook("C:\\Users\\Admin\\Desktop\\1.xlsx");
			// 得到第一张工作表
			XSSFSheet sheet = readXssfWorkbook.getSheetAt(0);
			for (int i = 0; i < 10; i++) {
				XSSFRow row = sheet.createRow((i + 1));
				for (int j = 0; j < 20; j++) {
					row.createCell(j).setCellValue("a" + i);
				}
			}
			OutputStream outputStream = new FileOutputStream("C:\\Users\\Admin\\Desktop\\1.xlsx");
			readXssfWorkbook.write(outputStream);

//		for (int r = 1; r < 50; r++) {
//			XSSFRow row = sheet.createRow(r);
//			for (int c = 0; c < 20; c++) {
//				row.createCell(c).setCellValue("csss" + c);
//			}
//		}
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		readXssfWorkbook.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
			// TODO: handle exception
		}

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

	public static void dealExcel(String in, String out) throws Exception {
		if (!in.equals(out)) {
			copyFile(in, out);
		}
		FileInputStream input = new FileInputStream(new File(out));
		Workbook wb = WorkbookFactory.create(input);
		FileOutputStream output = new FileOutputStream(new File(out));
		Sheet sheet = wb.getSheetAt(0);
		System.out.println(sheet.getRow(0).getLastCellNum());
		int columnCount = -1;
		int rowCount = 0;
		sheet.createRow(1);
		for (int r = 1; r < 20; r++) {
			Row row = sheet.createRow(r);
			for (int c = 0; c < 20; c++) {
				Cell cell = row.createCell(c);
				cell.setCellValue("aaa" + c);

			}
		}
//	        for (Row row : sheet) {
//	            if (columnCount == -1) {
//	                columnCount = row.getLastCellNum();
//	            }
//
//	            if (row.getLastCellNum() == columnCount) {
//	                //增加列
//	                Cell last = row.createCell(columnCount);
//	                if (rowCount == 0) {
//	                    last.setCellValue("Test");
//	                } else {
//	                    last.setCellValue(rowCount);
//	                }
//	                rowCount++;
//	            }
//	        }

		//// 添加行
		// Row row = sheet.createRow(sheet.getLastRowNum());
		// Cell cell = row.createCell(0);
		// cell.setCellValue("Test");

		output.flush();
		wb.write(output);
		wb.close();
		output.close();
	}

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

}
