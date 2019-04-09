package zyxhj.jiti.repository;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORG;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGRepository extends RDSRepository<ORG> {

	public ORGRepository() {
		super(ORG.class);
	}

	public JSONArray getUserORGs(DruidPooledConnection conn, Long userId, Integer count, Integer offset)
			throws Exception {
		// SELECT b.* FROM tb_ecm_org_user a LEFT JOIN tb_ecm_org b
		// ON a.org_id = b.id WHERE user_id = 397912180277668

		JSONArray sqJs = sqlGetJSONArray(conn,
				"SELECT org.* FROM tb_ecm_org_user user LEFT JOIN tb_ ecm_org org ON user.org_id = org.id WHERE user_id = ? ",
				new Object[] { userId }, count, offset);
		return sqJs;
	}

}
