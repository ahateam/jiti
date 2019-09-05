package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORGPermission;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGPermissionRepository extends RDSRepository<ORGPermission> {

	public ORGPermissionRepository() {
		super(ORGPermission.class);
	}

	public List<ORGPermission> getPermissions(DruidPooledConnection conn, JSONArray json) throws Exception {
		if (json != null && json.size() > 0) {
			StringBuffer sb = new StringBuffer();
			EXP sql = EXP.INS();
			for (int i = 0; i < json.size(); i++) {
				sql.or(EXP.INS().key("id", json.getLong(i)));
			}
			List<Object> params = new ArrayList<Object>();
			sql.toSQL(sb, params);
			return getList(conn, sql, 512, 0);
		} else {
			return null;
		}
	}

	public List<ORGPermission> getPermissionById(DruidPooledConnection conn, JSONArray json) throws Exception {
		StringBuffer sb = new StringBuffer();
		EXP sql = EXP.INS();
		for (int i = 0; i < json.size(); i++) {
			sql.or(EXP.INS().key("id", json.get(i)));
		}
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		return getList(conn, sql, 512, 0);
	}

}
