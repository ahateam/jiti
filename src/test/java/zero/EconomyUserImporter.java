package zero;

import com.alibaba.druid.pool.DruidPooledConnection;

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
			orgUserService.importORGUsersOffline(conn, 123456L, "306用户测试数据.xlsx");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
