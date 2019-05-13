package xhj.cn.start;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.AssetImportRecord;
import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGDistrict;
import zyxhj.jiti.domain.ORGExamine;
import zyxhj.jiti.domain.ORGPermission;
import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.jiti.domain.ORGUserImportRecord;
import zyxhj.jiti.domain.ORGUserImportTask;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.Superior;
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

		//	 RDSUtils.createTableByEntity(dsRds, ORGUserImportTask.class); 

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
