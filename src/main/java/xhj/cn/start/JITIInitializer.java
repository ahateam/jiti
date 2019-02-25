package xhj.cn.start;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.core.domain.Tag;
import zyxhj.core.domain.TagGroup;
import zyxhj.core.domain.User;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.Vote;
import zyxhj.jiti.domain.VoteOption;
import zyxhj.jiti.domain.VoteTicket;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;
import zyxhj.utils.data.rds.RDSUtils;

public class JITIInitializer {

	private static DruidPooledConnection conn;

	static {
		DataSourceUtils.initDataSourceConfig();

		try {
			conn = (DruidPooledConnection) DataSourceUtils.getDataSource("rdsDefault").openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		System.out.println("Initializer");

		DataSource dsRds = null;
		DruidPooledConnection conn = null;
		try {
			dsRds = DataSourceUtils.getDataSource("rdsDefault");

			conn = (DruidPooledConnection) dsRds.openConnection();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		initDB(dsRds);

		initData(conn);
	}

	private static void initDB(DataSource dsRds) {

		RDSUtils.createTableByEntity(dsRds, Tag.class);
		RDSUtils.createTableByEntity(dsRds, TagGroup.class);
		RDSUtils.createTableByEntity(dsRds, User.class);

		RDSUtils.createTableByEntity(dsRds, ORG.class);
		RDSUtils.createTableByEntity(dsRds, ORGUser.class);
		RDSUtils.createTableByEntity(dsRds, ORGUserRole.class);
		
		RDSUtils.createTableByEntity(dsRds, Vote.class);
		RDSUtils.createTableByEntity(dsRds, VoteOption.class);
		RDSUtils.createTableByEntity(dsRds, VoteTicket.class);

	}

	private static void initData(DruidPooledConnection conn) {

		initRole(conn);

		initTag(conn);

	}

	private static void initRole(DruidPooledConnection conn) {
		try {

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void initTag(DruidPooledConnection conn) {

	}
}
