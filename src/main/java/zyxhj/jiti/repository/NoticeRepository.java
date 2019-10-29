package zyxhj.jiti.repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.Notice;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class NoticeRepository extends RDSRepository<Notice> {

	public NoticeRepository() {
		super(Notice.class);
	}

	public static Cache<String, List<Notice>> NOTICE_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(30, TimeUnit.SECONDS)// 缓存对象有效时间，2天
			.maximumSize(100)//
			.build();

	public List<Notice> getNoticeByRoleGroup(DruidPooledConnection conn, Long orgId, String roles, String groups, Integer count, Integer offset)
			throws Exception {
		JSONArray role = JSONArray.parseArray(roles);
		JSONArray group = JSONArray.parseArray(groups);
		// 先从缓存里面取
		// 缓存为空 需要从数据库中获取
		List<Notice> notice = getNoticeByRG(conn, orgId, role, group, count, offset);
		return notice;

	}

	private List<Notice> getNoticeByRG(DruidPooledConnection conn, Long orgId, JSONArray role, JSONArray group, Integer count, Integer offset)
			throws Exception {
		JSONArray ja = new JSONArray();
		ja.add(orgId);
		EXP sql = EXP.INS().and(EXP.JSON_CONTAINS_KEYS(ja, "crowd", "orgId"));
		
		EXP roleEx =EXP.JSON_CONTAINS_KEYS(role, "crowd", "roles");
		sql.and(roleEx);
		
		if (group != null && group.size() > 0) {
			EXP groupEx = EXP.JSON_CONTAINS_KEYS(group, "crowd", "groups");
			sql.or(groupEx);
		}
		sql.append("ORDER BY create_time DESC");
		return getList(conn, sql, count, offset);
	}

	public List<Notice> getNotice(DruidPooledConnection conn, Long orgId,String title, Integer count, Integer offset)
			throws Exception {
		EXP sql = EXP.INS().key("org_id", orgId);
		if(!StringUtils.isBlank(title)) {
			sql.and(EXP.LIKE("title", title));
		}
		sql.append("ORDER BY create_time DESC");
		
		return getList(conn,sql, count, offset);
	}
}
