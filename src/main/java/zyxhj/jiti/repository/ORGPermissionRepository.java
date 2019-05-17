package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORGPermission;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class ORGPermissionRepository extends RDSRepository<ORGPermission> {

	public ORGPermissionRepository() {
		super(ORGPermission.class);
	}

	public List<ORGPermission> getPermissions(DruidPooledConnection conn, JSONArray json) throws Exception {
		if (json != null && json.size() > 0) {
			StringBuffer sb = new StringBuffer("WHERE ");
			SQL sql = new SQL();
			for (int i = 0; i < json.size(); i++) {
				sql.OR(StringUtils.join("id = ", json.getLong(i)));
			}
			sql.fillSQL(sb);
			System.out.println(sb.toString());
			return getList(conn, sb.toString(), new Object[] {}, 512, 0);
		} else {
			return null;
		}
	}

	public List<ORGPermission> getPermissionById(DruidPooledConnection conn, JSONArray json) throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		for (int i = 0; i < json.size(); i++) {
			sql.OR(StringUtils.join("id = ", json.getLong(i)));
		}
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), new Object[] {}, 512, 0);
	}

}
