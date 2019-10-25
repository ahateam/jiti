package jiti;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;
import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;

import zyxhj.core.repository.MailRepository;
import zyxhj.core.service.MailService;
import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.jiti.service.SingleCertificateTaskService;
import zyxhj.utils.IDUtils;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.TEXP;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.TSQL;
import zyxhj.utils.data.ts.TSQL.OP;
import zyxhj.utils.data.ts.TSRepository;

public class messageTest {
	private static DruidPooledConnection conn;
	private static SyncClient client;

	static {
		try {
			conn = DataSource.getDruidDataSource("rdsDefault.prop").getConnection();
			client = DataSource.getTableStoreSyncClient("tsDefault.prop");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ORGService orgService = new ORGService();
	private static ORGUserService orgUserService = new ORGUserService();
	private static SingleCertificateTaskService scftService = new SingleCertificateTaskService();

	private static MailService mailService = new MailService("node");

	@Test
	public void testMessage() {
		List<Long> idList = new ArrayList<Long>();
		for (int i = 0; i < 1000; i++) {
			idList.add(IDUtils.getSimpleId());
		}
		JSONArray ja = new JSONArray();
		for (int j = 0; j < idList.size(); j++) {
			JSONObject jo = new JSONObject();
			jo.put("id", idList.get(j));
			jo.put("read", 0);
			ja.add(jo);
		}
		System.out.println(ja.toJSONString());
	}

	@Test
	public void testmail() {
		try {
//			mailService.createMailTag(100001L,"vote");
			JSONArray receivers = new JSONArray();
			receivers.add(1101);
			JSONArray tags = new JSONArray();
			tags.add("examine");
			String sender = "userId:0111111";
			String title = "测试消息发送2";
			String text = "测试消息发送内容2";
			String action = "跳转页面地址2";
//			
//			mailService.mailSend(100001L, receivers, tags, sender, title, text, action, "1234567");
//			
			JSONArray jo = mailService.mailList(100001L, "1101", 10, 0);
			System.out.println(jo.toJSONString());
			Long maxTime = 0L;
			JSONArray a = new JSONArray();
			JSONObject maxMail = new JSONObject();
			for (int i = 0; i < jo.size(); i++) {
				JSONObject mail = JSON.parseObject(jo.getString(i));
				if (mail.getBoolean("active")) {
					System.out.println("tags");
					int tag = mail.getString("tags").indexOf("vote");
					if (tag > 0) {
						System.out.println("createTime");
						Long time = mail.getLong("creatTime");
						System.out.println(time);
						if (time != null && time > 0) {
							System.out.println("mail");
							if (time > maxTime) {
								System.out.println("mail");
								maxTime = time;
								maxMail = mail;
							}
						}
					}
				}
			}
			System.out.println(maxMail.toJSONString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testii() {
		try {
			Examine e = new Examine();
			e.id = 123456789L;
			orgUserService.sendEexamineMail(conn, 397744896824382L, 123456789L, 100L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testiiiii() {

		try {
			MailRepository m = new MailRepository();
			PrimaryKey pk1 = new PrimaryKeyBuilder().add("moduleId", 100001L ).add("receiver", 398404770986922L).add("sequenceId", PrimaryKeyValue.INF_MIN).build();
			PrimaryKey pk2 = new PrimaryKeyBuilder().add("moduleId", 100001L ).add("receiver", 398404770986922L).add("sequenceId", PrimaryKeyValue.INF_MAX).build();
			
//		BoolQuery boolQuery = new BoolQuery();
			Query query = TEXP.Match("title", "审批消息");
			SearchQuery querys = new SearchQuery();
			querys.setQuery(query);
			querys.setGetTotalCount(true);

//		SearchQuery querys = TEXP.Match("title", "审批消息").
//		TSQL ts = new TSQL();
//		ts.Match(OP.AND, "title", "审批消息");
//		ts.setGetTotalCount(true);
//		SearchQuery query = ts.build();

			JSONObject jo = m.nativeSearch(client, m.getTableName(), "index_mail", querys);
			System.out.println(jo.toJSONString());

		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
