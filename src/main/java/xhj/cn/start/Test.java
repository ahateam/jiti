package xhj.cn.start;

import com.alibaba.druid.pool.DruidDataSource;
import com.alicloud.openservices.tablestore.SyncClient;

import zyxhj.core.domain.ExportTask;
import zyxhj.core.domain.ImportTempRecord;
import zyxhj.core.domain.Mail;
import zyxhj.jiti.domain.Feedback;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.rds.RDSUtils;
import zyxhj.utils.data.ts.TSUtils;

public class Test {

	public static void main(String[] args) {

		testDB();

	}

	private static void testDB() {
		System.out.println("testDB");

		try {
			DruidDataSource dds = DataSource.getDruidDataSource("rdsDefault.prop");
			SyncClient client = DataSource.getTableStoreSyncClient("tsDefault.prop");
//			 RDSUtils.dropTableByEntity(dds, Feedback.class);

//			 RDSUtils.createTableByEntity(dds, Feedback.class);
			TSUtils.createTableByEntity(client, ImportTempRecord.class);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
