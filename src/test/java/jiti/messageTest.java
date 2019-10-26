package jiti;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;
import com.alicloud.openservices.tablestore.model.search.query.BoolQuery;
import com.alicloud.openservices.tablestore.model.search.query.Query;
import com.alicloud.openservices.tablestore.model.search.sort.FieldSort;
import com.alicloud.openservices.tablestore.model.search.sort.Sort;
import com.alicloud.openservices.tablestore.model.search.sort.SortOrder;
import com.alipay.api.domain.StaffInfo;

import zyxhj.core.domain.Mail;
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

			JSONArray ja = mailService.mailList(100001L, "397652463180671", 10, 0);
			System.out.println(ja.toJSONString());
			Long maxTime = 0L;
			JSONObject maxMail = new JSONObject();
			System.out.println(ja.size());
			for (int i = 0; i < ja.size(); i++) {
				JSONObject mail = JSON.parseObject(ja.getString(i));
				if (mail.getBoolean("active")) {
					Object o = mail.get("createTime");
					if (o instanceof Long) {
						Long time = Long.parseLong(o.toString());
						if (time > maxTime) {
							System.out.println("time:" + time);
							maxTime = time;
							maxMail = mail;
							System.out.println("maxTime:" + maxTime);
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
			orgUserService.sendEexamineMail(397744896824382L, 123456789L, 100L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testiiiii() throws Exception {

		try {
			MailRepository m = new MailRepository();
			PrimaryKey pk1 = new PrimaryKeyBuilder().add("moduleId", 100001L).add("receiver", 397652463180671L)
					.add("sequenceId", 1571984941206000L).build();
			PrimaryKey pk2 = new PrimaryKeyBuilder().add("moduleId", 100001L).add("receiver", 398404770986922L)
					.add("sequenceId", PrimaryKeyValue.INF_MAX).build();

			List<Mail> mlist = m.batchGet(client, Arrays.asList(pk1));
			System.out.println(mlist.size());
			if (mlist != null && mlist.size() > 0) {
				System.out.println(123456);
				for (Mail ma : mlist) {
					System.out.println(ma.moduleId);
				}
			} else {
				System.out.println(111);
			}
//			Query query = TEXP.Match("title", "投票");
//			TEXP.Term("moduleId", 100001L);
//			Query query = TEXP.Bool(TEXP.BOOL_TYPE_AND, Arrays.asList(TEXP.Match("title", "投票"),TEXP.Term("moduleId", 100001L),TEXP.Term("receiver", 398404770986922L),TEXP.Term("sequenceId", 1571984942441000L)));
//			SearchQuery querys = new SearchQuery();
//			querys.setQuery(query);
//			querys.setSort(new Sort(Arrays.asList(new FieldSort("createTime", SortOrder.ASC))));
//			querys.setGetTotalCount(true);
//
//			JSONObject jo = m.nativeSearch(client, m.getTableName(), "index_mail", querys);
//			System.out.println(jo.toJSONString());

		} catch (ServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testListss() {
		List<Long> Longlist = new ArrayList<Long>();
		Longlist.add(1L);
		Longlist.add(2L);
		Longlist.add(3L);
		Longlist.add(4L);
		Longlist.add(5L);
		List<Long> Longlist1 = new ArrayList<Long>();
		Longlist1.add(2L);
		Longlist1.add(3L);
		Longlist1.add(5L);
		List<Long> list = new ArrayList<Long>();
		for (int i = 0; i < Longlist.size(); i++) {
			Long s = Longlist.get(i);
			for (int j = 0; j < Longlist1.size(); j++) {
				Long s1 = Longlist1.get(j);
				if(s == s1) {
					list.add(s1);
					break;
				}
			}
		}
		Longlist.removeAll(list);
		System.out.println("---------------"+Longlist.size());
		for(int i = 0; i < Longlist.size(); i ++) {
			System.out.println(Longlist.get(i));
		}

	}
}
