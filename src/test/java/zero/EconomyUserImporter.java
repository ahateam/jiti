package zero;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSourceUtils;

public class EconomyUserImporter {

	private static final String EXCEL_XLS = "xls";
	private static final String EXCEL_XLSX = "xlsx";

	private static DruidPooledConnection conn;

	private static ORGService orgService;
	private static ORGUserService orgUserService;

	static {
		DataSourceUtils.initDataSourceConfig();

		try {

			orgService = Singleton.ins(ORGService.class);
			orgUserService = Singleton.ins(ORGUserService.class);

			conn = (DruidPooledConnection) DataSourceUtils.getDataSource("rdsDefault").openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		try {
			orgUserService.importORGUsersOffline(conn, 123L, "用户表模板.xlsx");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
