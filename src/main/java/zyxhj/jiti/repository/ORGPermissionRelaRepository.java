package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class ORGPermissionRelaRepository extends RDSRepository<ORGPermissionRel> {

	public ORGPermissionRelaRepository() {
		super(ORGPermissionRel.class);
	}

	public List<ORGPermissionRel> getPermissionsId(DruidPooledConnection conn, Long orgId, JSONArray orgRoles)
			throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx("org_id = ? ", orgId);
		if (orgRoles != null && orgRoles.size() > 0) {
			SQL sqlEx = new SQL();
			for (int i = 0; i < orgRoles.size(); i++) {
				sqlEx.OR(StringUtils.join("role_id = ", orgRoles.getLong(i)));
			}
			sql.AND(sqlEx);
		}
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), sql.getParams(), 512, 0);
	}

	public List<ORGPermissionRel> getRolesId(DruidPooledConnection conn, Long orgId, JSONArray permissionId)
			throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx("org_id = ? ", orgId);
		if (permissionId != null && permissionId.size() > 0) {
			SQL sqlEx = new SQL();
			for (int i = 0; i < permissionId.size(); i++) {
				sqlEx.OR(StringUtils.join("permission_id = ", permissionId.getLong(i)));
			}
			sql.AND(sqlEx);
		}
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), sql.getParams(), 512, 0);
	}

}
