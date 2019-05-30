package zyxhj.jiti.repository;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Family;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

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
		SQL sql = new SQL();
		sql.addEx("fa.org_id = ? ", orgId);
		sql.addEx("GROUP BY oru.family_master");
		sql.fillSQL(sb);
		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), count, offset);
	}

}
