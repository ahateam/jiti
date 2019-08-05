package zyxhj.jiti.repository;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORG;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class ORGRepository extends RDSRepository<ORG> {

	public ORGRepository() {
		super(ORG.class);
	}

	public JSONArray getUserORGs(DruidPooledConnection conn, Long userId, Byte level, Integer count, Integer offset)
			throws Exception {
		// SELECT b.* FROM tb_ecm_org_user a LEFT JOIN tb_ecm_org b
		// ON a.org_id = b.id WHERE user_id = 397912180277668

		StringBuffer sb = new StringBuffer(
				"SELECT org.* FROM tb_ecm_org_user user LEFT JOIN tb_ecm_org org ON user.org_id = org.id WHERE  ");
		SQL sql = new SQL();
		sql.addEx("user_id = ?", userId);
		if (level == ORG.LEVEL.DISTRICT.v()) {
			sql.AND(StringUtils.join("(level = ", ORG.LEVEL.PRO.v(), " OR level = ", ORG.LEVEL.CITY.v(), " OR level = ",
					ORG.LEVEL.DISTRICT.v(), ")"));
		} else {
			sql.AND("level = ? ", level);
		}
		sql.fillSQL(sb);
		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public List<ORG> getOrgByNameAndLevel(DruidPooledConnection conn, Byte level, String orgName) throws Exception {
		return getList(conn, StringUtils.join("WHERE level = ? AND name LIKE '%", orgName, "%'"),
				Arrays.asList(level), null, null);
	}

	public List<ORG> getORGs(DruidPooledConnection conn, JSONArray json, int count, int offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		SQL sql = new SQL();
		for (int i = 0; i < json.size(); i++) {
			sql.OR(StringUtils.join("id = ", json.getLong(i)));
		}
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), null, count, offset);
	}

	public List<ORG> getOrgByName(DruidPooledConnection conn, String name, Integer count, Integer offset)
			throws Exception {
		return getList(conn, StringUtils.join("name LIKE '%", name, "%'"), null, count, offset);
	}

	public List<ORG> getBankList(DruidPooledConnection conn, JSONArray json, String name, Byte type, Integer count,
			Integer offset) throws Exception {

		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.AND("type = ? ", type);
		if (json != null && json.size() > 0) {
			SQL sqlOR = new SQL();
			for (int i = 0; i < json.size(); i++) {
				sqlOR.OR(StringUtils.join("id = ", json.getLong(i)));
			}
			sql.AND(sqlOR);
		}
		if (name != null) {
			sql.AND(StringUtils.join("name LIKE '%", name, "%'"));
		}
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}
}
