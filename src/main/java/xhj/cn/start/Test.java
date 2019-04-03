package xhj.cn.start;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Asset;
import zyxhj.jiti.domain.Family;
import zyxhj.jiti.domain.ORGExamine;
import zyxhj.jiti.domain.VoteTicket;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;
import zyxhj.utils.data.rds.RDSUtils;

public class Test {

	private static DruidPooledConnection conn;

	static {
		DataSourceUtils.initDataSourceConfig();
		// contentService = ContentService.getInstance();

		try {
			conn = (DruidPooledConnection) DataSourceUtils.getDataSource("rdsDefault").openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		testDB();

	}

	private static void testDB() {
		System.out.println("testDB");
		try { 
			DataSource dsRds = DataSourceUtils.getDataSource("rdsDefault");

			// RDSUtils.dropTableByEntity(dsRds, VoteTicket.class);

		//	 RDSUtils.createTableByEntity(dsRds, VoteTicket.class);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
