package zyxhj.jiti.repository;

import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGPermissionRelaRepository extends RDSRepository<ORGPermissionRel> {

	public ORGPermissionRelaRepository() {
		super(ORGPermissionRel.class);
	}

	public List<ORGPermissionRel> getPermissionsId(DruidPooledConnection conn, Long orgId, JSONArray orgRoles)
			throws Exception {
//		SQL sql = new SQL();
//		sql.addEx("org_id = ? ", orgId);
		EXP sql = EXP.INS().key("org_id", orgId);
		if (orgRoles != null && orgRoles.size() > 0) {
//			SQL sqlEx = new SQL();
			EXP sqlEx = EXP.INS();
			for (int i = 0; i < orgRoles.size(); i++) {
//				sqlEx.OR(StringUtils.join("role_id = ", orgRoles.getLong(i)));
				sqlEx.or(EXP.INS().key("role_id", orgRoles.getLong(i)));
			}
			sql.and(sqlEx);
		}
		return getList(conn, sql, 512, 0);
	}

	public List<ORGPermissionRel> getRolesId(DruidPooledConnection conn, Long orgId, JSONArray permissionId)
			throws Exception {
		EXP sql = EXP.INS().key("org_id", orgId);
		if (permissionId != null && permissionId.size() > 0) {
			EXP sqlEx = EXP.INS();
			for (int i = 0; i < permissionId.size(); i++) {
				sqlEx.or(EXP.INS().key("permission_id", permissionId.getLong(i)));
			}
			sql.and(sqlEx);
		}
		return getList(conn, sql, 512, 0);
	}

	public ORGPermissionRel getPermissionRela(DruidPooledConnection conn, Long orgId, String roles, Long permissionId)
			throws Exception {
		JSONArray role = JSONArray.parseArray(roles);
		EXP sql = EXP.INS().key("org_id", orgId);
		
		EXP sqlEx = EXP.INS();
		if (role != null && role.size() > 0) {
			for (int i = 0; i < role.size(); i++) {
				sqlEx.or(EXP.INS().key("role_id", role.getLong(i)));
			}
			sql.and(sqlEx);
			sql.andKey("permission_id", permissionId);
		} else {
			return null;
		}
		return this.get(conn, sql);
	}

}
