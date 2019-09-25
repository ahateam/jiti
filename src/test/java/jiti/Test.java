package jiti;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.controller.ORGController;
import zyxhj.jiti.controller.VersionController;
import zyxhj.jiti.controller.VoteController;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.jiti.service.VoteService;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;

public class Test {

	private static DruidPooledConnection conn;

	static {
		try {
			conn = DataSource.getDruidDataSource("rdsDefault.prop").getConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ORGService or = new ORGService();

	public static void main(String[] args) throws Exception {
		//
		// String phone = "[17353166886,18651813050]";
		// JSONArray json = new JSONArray();
		// json.add("18651813050");
		// json.add("17353166886");
		// // or.SendSms(phone);

		Integer j = 3508;
		// System.out.println((i/100)+1);

		for (int i = 0; i < (j / 100) + 1; i++) {
			System.out.println(i);
		}
	}

	@org.junit.Test
	public void testAAA() throws ServerException {

		int[] a = { 101, 102 };

		JSONArray js = (JSONArray) JSONArray.toJSON(a);
		js.add(103);
		System.out.println(js.toJSONString());

		EXP tag = testJsonContainsORKey(js, "roles", null);

		EXP tags = EXP.INS().key("org_id", 400942027079516L).and(tag);

		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		tags.toSQL(sb, params);
		System.out.println(sb.toString());

		ORGUserRepository user = new ORGUserRepository();

		List<ORGUser> userlist = user.getList(conn, tags, 10, 0);

		System.out.println(userlist.size());

	}

	public EXP testJsonContainsORKey(JSONArray tags, String column, String tagGroup) throws ServerException {

		if (tags == null && tags.size() == 0) {
			return null;
		}
		String path;
		if (tagGroup == null) {
			path = "$";
		} else {
			path = "$." + tagGroup;
		}
		EXP tag = EXP.INS();
		for (int i = 0; i < tags.size(); i++) {
			System.out.println(tags.get(i));
			tag.or(EXP.JSON_CONTAINS(column, path, tags.get(i)));
		}
		return tag;
	}

	public void testEditUserMobile() throws Exception {

		ORGController c = new ORGController("node");

		c.editUserMobile(400987736416750L, null,"");
		System.out.println();
	}
	
	@org.junit.Test
	public void testGetAppVersion() {
		VersionController ver = new VersionController("");
		System.out.println(ver.getVersion().toJSONString());
	}
}
