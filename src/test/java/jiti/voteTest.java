package jiti;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.controller.VoteController;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;

public class voteTest {

	private static DruidPooledConnection conn;
	private static VoteController voteController = new VoteController("node");

	private static Integer count =10;
	private static Integer offset =0;
	private static Long orgId =397652553337218L;
	private static Long userId =397652698555992L;
	
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			conn = DataSource.getDruidDataSource("rdsDefault.prop").getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conn.close();
	}
	
	@org.junit.Test
	public void getVoteByUserRoles() throws Exception {
		JSONArray roles = new JSONArray();
		roles.add(104);
		roles.add(101);
		roles.add(109);
		voteController.getVoteByUserRoles(orgId, userId, roles, count, offset);
	}
	
}

