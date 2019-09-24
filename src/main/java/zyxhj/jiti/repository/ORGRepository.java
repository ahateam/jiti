package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORG;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGRepository extends RDSRepository<ORG> {

	public ORGRepository() {
		super(ORG.class);
	}

	public JSONArray getUserORGs(DruidPooledConnection conn, Long userId, Byte level, Integer count, Integer offset)
			throws Exception {
		// SELECT b.* FROM tb_ecm_org_user a LEFT JOIN tb_ecm_org b
		// ON a.org_id = b.id WHERE user_id = 397912180277668

		StringBuffer sb = new StringBuffer(
				"SELECT org.* FROM tb_ecm_org_user user LEFT JOIN tb_ecm_org org ON user.org_id = org.id WHERE ");
		EXP sql = EXP.INS().key("user_id",userId);
		
		if (level == ORG.LEVEL.DISTRICT.v()) {
//			sql.AND(StringUtils.join("(level = ", ORG.LEVEL.PRO.v(), " OR level = ", ORG.LEVEL.CITY.v(), " OR level = ",
//					ORG.LEVEL.DISTRICT.v(), ")"));
			sql.and(EXP.INS().orKey("level", ORG.LEVEL.PRO.v()).orKey("level",ORG.LEVEL.CITY.v()).orKey("level",ORG.LEVEL.DISTRICT.v()));
		} else {
//			sql.AND("level = ? ", level);
			sql.andKey("level", level);
		}
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb,params);
		System.out.println(sb.toString());
		return sqlGetJSONArray(conn, sb.toString(), params, count, offset);
	}

	public List<ORG> getOrgByNameAndLevel(DruidPooledConnection conn, Byte level, String orgName) throws Exception {
		return getList(conn, StringUtils.join("level = ? AND name LIKE '%", orgName, "%'"), Arrays.asList(level), null,
				null);
	}

	public List<ORG> getORGs(DruidPooledConnection conn, JSONArray json, int count, int offset) throws Exception {
//		StringBuffer sb = new StringBuffer();
		EXP sql = EXP.INS().IN("id", json);
//		for (int i = 0; i < json.size(); i++) {
//			sql.OR(StringUtils.join("id = ", json.getLong(i)));
//			sql.or(EXP.INS().key("id", json.getLong(i)));
			
//		}
		
		return getList(conn, sql, count, offset);
	}

	public List<ORG> getOrgByName(DruidPooledConnection conn, String name, Integer count, Integer offset)
			throws Exception {
		return getList(conn, StringUtils.join("name LIKE '%", name, "%'"), null, count, offset);
	}

	public List<ORG> getBankList(DruidPooledConnection conn, JSONArray json, String name, Byte type, Integer count,
			Integer offset) throws Exception {

		StringBuffer sb = new StringBuffer();
		EXP sql = EXP.INS().key("type", type);
		if (json != null && json.size() > 0) {
			EXP sqlOR = EXP.INS();
			for (int i = 0; i < json.size(); i++) {
				sqlOR.or(EXP.INS().key("id", json.getLong(i)));
			}
			sql.and(sqlOR);
		}
		if (name != null) {
//			sql.and(StringUtils.join("name LIKE '%", name, "%'"));
			sql.and(EXP.INS().LIKE("name", name));
		}
		return getList(conn, sql, count, offset);
	}
}
