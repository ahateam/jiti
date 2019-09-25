package xhj.cn.start;

import com.alibaba.druid.pool.DruidDataSource;

import zyxhj.core.domain.ExportTask;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.rds.RDSUtils;

public class Test {

	public static void main(String[] args) {

		testDB();

	}

	private static void testDB() {
		System.out.println("testDB");

		try {
			DruidDataSource dds = DataSource.getDruidDataSource("rdsDefault.prop");

			// RDSUtils.dropTableByEntity(dds, ORGPermission.class);

			 RDSUtils.createTableByEntity(dds, ExportTask.class);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
