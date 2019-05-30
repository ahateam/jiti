package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Notice;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class NoticeRepository extends RDSRepository<Notice> {

	public NoticeRepository() {
		super(Notice.class);
	}

	public List<Notice> getNotice(DruidPooledConnection conn, Long orgId, String roles, String groups, Integer count,
			Integer offset) throws Exception {
		JSONArray role = JSONArray.parseArray(roles);
		JSONArray group = JSONArray.parseArray(groups);

		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx(StringUtils.join("JSON_CONTAINS(crowd,'", orgId, "','$.orgId')"));
		SQL roleEx = new SQL();
		for (int i = 0; i < role.size(); i++) {
			roleEx.OR(StringUtils.join("JSON_CONTAINS(crowd,'", role.getLong(i), "','$.roles')"));
		}
		SQL groupEx = new SQL();
		if (group != null && group.size() > 0) {
			for (int j = 0; j < group.size(); j++) {
				groupEx.OR(StringUtils.join("JSON_CONTAINS(crowd,'", group.getLong(j), "','$.groups')"));
			}
		}
		sql.AND(roleEx);
		sql.AND(groupEx);
		sql.fillSQL(sb);

		return getList(conn, sb.toString(), new Object[] {}, count, offset);

	}
}
