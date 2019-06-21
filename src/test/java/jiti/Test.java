package jiti;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.service.ORGService;
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

	private static ORGService or = new ORGService();

	public static void main(String[] args) throws Exception {
//
//			String phone = "[17353166886,18651813050]";
//		JSONArray json = new JSONArray();
//		json.add("18651813050");
//		json.add("17353166886");
//	//	or.SendSms(phone);
		
		
		Integer j = 3508;
		//System.out.println((i/100)+1);
		
		for(int i = 0 ; i < (j/100)+1 ; i++) {
			System.out.println(i);
		}
	}

}
