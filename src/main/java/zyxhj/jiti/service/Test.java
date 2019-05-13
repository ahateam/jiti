package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.repository.AssetRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.repository.VoteRepository;
import zyxhj.test.repository.TestRepository;
import zyxhj.utils.api.ServerException;
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
	private static VoteRepository voteRepository = new VoteRepository();
	private static AssetRepository assetRepository = new AssetRepository();

	public static void main(String[] args) throws Exception {

		// 查询可投票人数
		// getParticipateCount();

		// 根据组织分类查询投票列表
		// getVotesByOrgId();

		// 获取资产列表
		// queryAssets();

		// 区级统计
		// sumAssetByDstrictId();

		//区根据类型获取资产列表
		// getAssetListByTypes();
		
		//组织根据年份，资产类型等条件，统计资源原值，产值等
		//sumAssetBYGRAB();
		
	//	batchEdit();
		
//		JSONArray json = new JSONArray();
//		json.add("1233");
//		json.add("12332");
//		List<String> li = new ArrayList<>();
//		
//		for (int i = 0; i < json.size(); i++) {
//		//	li.set(i, json.getLong(i).toString());
//			li.add(json.getLong(i).toString());
//		}
//		System.out.println(li);
		
//		String s = "[123,234,254]";
//		JSONArray json = JSONArray.parseArray(s);
		
//		JSONArray json= new JSONArray();
//		json.add("123");
//		json.add("234");
//		json.add("456");
//		System.out.println(json);
//		json.remove("234");
//		System.out.println(json);
		
//		String s = "1 2 3 4 5";
//		String[] ar = s.split("");
//		System.out.println(s);
//		for (String a : ar) {
//			System.out.println(a);
//		}
		/*
		 * float a = 1802 / 100; System.out.println(a);
		 * System.out.println(Math.ceil(a));
		 */
		String i = "{}";
		JSONObject js = JSONObject.parseObject(i);
		System.out.println(js);
	}

	private static void batchEdit() throws ServerException {
		JSONArray json = new JSONArray();
		json.add("1111");
		json.add("2222");
		json.add("2222");
		assetRepository.batchEditAssetsGroups(conn, 123L, json, json);
	}

	private static void sumAssetBYGRAB() throws Exception {
		JSONArray json = new JSONArray();
		json.add("1111");
		json.add("2222");
		json.add("2222");
		assetRepository.sumAssetBYGRAB(conn, 123L, "2015", json, json, json, json);
	}

	private static void getAssetListByTypes() throws Exception {
//		JSONArray json = new JSONArray();
//		json.add("1111");
//		json.add("2222");
//		assetRepository.getAssetListByTypes(conn, 222L, json, json, json, json, json,
//				json, 10, 0);
	}

	private static void sumAssetByDstrictId() throws Exception {
		JSONArray json = new JSONArray();
		json.add("1111");
		json.add("2222");

		//assetRepository.sumAssetByDstrictId(conn, 123L, "2015", json, json, json, json, json);
	}

	private static void queryAssets() throws ServerException {
		JSONArray json = new JSONArray();
		json.add("1111");
		json.add("2222");
		JSONObject js = new JSONObject();
		js.put("roles", json);
		assetRepository.queryAssets(conn, 123L, "xxxx", new JSONArray(), js, null, null);
	}

	private static void getVotesByOrgId() throws Exception {
//		JSONArray json = new JSONArray();
//		json.add("1111");
//		json.add("2222");
//		Byte a = 0;
//		voteRepository.getVotesByOrgId(conn, 12312312L, json, a, null, null);
	}

	private static void getParticipateCount() throws ServerException {
		Long orgId = 397912000965281L;
		JSONArray json = new JSONArray();
		json.add("103");
		json.add("104");
		json.add("105");
		JSONObject js = new JSONObject();
		js.put("roles", json);
		int p = oRGUserRepository.getParticipateCount(conn, orgId, 123L, js);
		System.out.println(p);
	}
}
