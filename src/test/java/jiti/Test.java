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
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
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

	private static ORGService orgService = new ORGService();
	private static ORGUserService orgUserService = new ORGUserService();

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
		
		lockstyle.setFillPattern(FillPatternType.SOLID_FOREGROUND );
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

	//复制文件
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
		orgService.getFamilyInfo(conn, 397652553337218L, 10L);
	}
	
	
}
