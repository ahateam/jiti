package zyxhj.jiti.service;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.test.repository.TestRepository;
import zyxhj.utils.data.DataSourceUtils;

public class Test {

	private static DruidPooledConnection conn;

	static {
		DataSourceUtils.initDataSourceConfig();
		try {
			conn = (DruidPooledConnection) DataSourceUtils.getDataSource("rdsDefault").openConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static TestRepository testRepository = new TestRepository();
	private static ORGUserRepository oRGUserRepository = new ORGUserRepository();

	public static void main(String[] args) throws Exception {
		Long orgId = 397912000965281L;
		JSONArray json = new JSONArray();
		json.add("103");
		json.add("104");
		json.add("105");
		JSONObject js = new JSONObject();
		js.put("roles", json);
		int p = oRGUserRepository.getParticipateCount(conn, orgId, 123L,js);
		System.out.println(p);
	}
}
