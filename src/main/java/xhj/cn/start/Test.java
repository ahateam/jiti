package xhj.cn.start;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.domain.Message;
import zyxhj.jiti.domain.Notice;
import zyxhj.jiti.domain.NoticeTask;
import zyxhj.jiti.domain.NoticeTaskRecord;
import zyxhj.jiti.domain.PrintingTemplate;
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

		//	 RDSUtils.dropTableByEntity(dsRds, ORGPermission.class);

			RDSUtils.createTableByEntity(dsRds, PrintingTemplate.class); 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
