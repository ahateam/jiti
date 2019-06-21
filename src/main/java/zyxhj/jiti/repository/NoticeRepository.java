package zyxhj.jiti.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.Notice;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class NoticeRepository extends RDSRepository<Notice> {

	public NoticeRepository() {
		super(Notice.class);
	}

	public static Cache<String, List<Notice>> NOTICE_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(30, TimeUnit.SECONDS)// 缓存对象有效时间，2天
			.maximumSize(100)//
			.build();

	public List<Notice> getNoticeByRoleGroup(DruidPooledConnection conn, Long orgId, String roles, String groups)
			throws Exception {
		JSONArray role = JSONArray.parseArray(roles);
		JSONArray group = JSONArray.parseArray(groups);
		// 先从缓存里面取
			// 缓存为空 需要从数据库中获取
			List<Notice> notice = getNoticeByRG(conn, orgId, role, group);
			return notice;

	}

	private List<Notice> getNoticeByRG(DruidPooledConnection conn, Long orgId, JSONArray role, JSONArray group)
			throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx(StringUtils.join("JSON_CONTAINS(crowd,'", orgId, "','$.orgId')"));
		SQL roleEx = new SQL();
		for (int i = 0; i < role.size(); i++) {
			roleEx.OR(StringUtils.join("JSON_CONTAINS(crowd,'", role.getLong(i), "','$.roles')"));
		}
		sql.AND(roleEx);
		if (group != null) {
			SQL groupEx = new SQL();
			if (group != null && group.size() > 0) {
				for (int j = 0; j < group.size(); j++) {
					groupEx.OR(StringUtils.join("JSON_CONTAINS(crowd,'", group.getLong(j), "','$.groups')"));
				}
			}
			sql.AND(groupEx);
		}
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), new Object[] {}, 512, 0);
	}

	public List<Notice> getNotice(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx("org_id = ?", orgId);
		sql.addEx(" ORDER BY create_time DESC");
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}
}
