package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Family;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class FamilyRepository extends RDSRepository<Family> {

	public FamilyRepository() {
		super(Family.class);
	}

	public JSONArray getFamilyAll(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		// SELECT * FROM tb_ecm_family fa LEFT JOIN tb_ecm_org_user oru ON
		// fa.family_number = oru.family_number
		// WHERE fa.org_id = 398977803603065 GROUP BY oru.family_master
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM tb_ecm_family fa LEFT JOIN tb_ecm_org_user oru ON fa.family_number = oru.family_number WHERE ");
		EXP sql = EXP.INS().key("fa.org_id", orgId).append("GROUP BY oru.family_master");
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		return sqlGetJSONArray(conn, sb.toString(), params, count, offset);
	}

}
