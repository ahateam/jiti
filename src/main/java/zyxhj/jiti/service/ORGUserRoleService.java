package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.repository.ORGPermissionRelaRepository;
import zyxhj.jiti.repository.ORGUserRoleRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;

/**
 * 第三方用户自定义角色service
 *
 */
public class ORGUserRoleService {

	private static Logger log = LoggerFactory.getLogger(ORGUserRoleService.class);

	private static Cache<Long, ORGUserRole> ORG_USER_ROLE_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(1000)//
			.build();

	private ORGUserRoleRepository orgUserRoleRepository;
	private ORGPermissionRelaRepository oreOrgPermissionRelaRepository;

	public ORGUserRoleService() {
		try {
			orgUserRoleRepository = Singleton.ins(ORGUserRoleRepository.class);
			oreOrgPermissionRelaRepository = Singleton.ins(ORGPermissionRelaRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建自定义角色
	 */
	public ORGUserRole createORGUserRole(DruidPooledConnection conn, Long orgId, String name, String remark)
			throws Exception {
		ORGUserRole role = new ORGUserRole();
		role.orgId = orgId;
		role.roleId = IDUtils.getSimpleId();
		role.name = name;
		role.remark = remark;

		orgUserRoleRepository.insert(conn, role);

		return role;
	}

	/**
	 * 编辑自定义角色
	 */
	public int editORGUserRole(DruidPooledConnection conn, Long orgId, Long roleId, String name, String remark)
			throws Exception {
		ORGUserRole renew = new ORGUserRole();
		renew.name = name;
		renew.remark = remark;

		return orgUserRoleRepository.updateByANDKeys(conn, new String[] { "org_id", "role_id" },
				new Object[] { orgId, roleId }, renew, true);
	}

	public ORGUserRole getORGUserRoleById(DruidPooledConnection conn, Long orgId, Long roleId) throws Exception {
		// 先从系统缓存里取，再从缓存去，最后再查
		ORGUserRole role = ORGUserRole.SYS_ORG_USER_ROLE_MAP.get(roleId);
		if (role == null) {
			role = ORG_USER_ROLE_CACHE.getIfPresent(roleId);
			if (role == null) {
				// 从数据库中获取
				role = orgUserRoleRepository.getByANDKeys(conn, new String[] { "org_id", "role_id" },
						new Object[] { orgId, roleId });
				if (role != null) {
					// 放入缓存
					ORG_USER_ROLE_CACHE.put(roleId, role);
				}
			}
		}
		return role;
	}

	/**
	 * 获取自定义角色列表
	 */
	public List<ORGUserRole> getORGUserRoles(DruidPooledConnection conn, Long orgId) throws Exception {
		return orgUserRoleRepository.getListByKey(conn, "org_id", orgId, 512, 0);
	}

	/**
	 * 根据权限查看角色列表
	 */
	public List<ORGUserRole> getRolesByPermission(DruidPooledConnection conn, Long orgId, Long permissionId)
			throws Exception {
		List<ORGUserRole> list = new ArrayList<ORGUserRole>();
		ORGUserRole orp = new ORGUserRole();

		JSONArray jsonPer = new JSONArray();
		jsonPer.add(permissionId);
		JSONArray json = new JSONArray();
		List<ORGPermissionRel> li = oreOrgPermissionRelaRepository.getRolesId(conn, orgId, jsonPer);

		// 从MAP中查找
		for (ORGPermissionRel or : li) {
			orp = ORGUserRole.SYS_ORG_USER_ROLE_MAP.get(or.roleId);
			if (orp != null) {
				list.add(orp);
			} else {
				// 放入json中 执行完成后去数据库中获取
				json.add(or.permissionId);
			}
		}

		if (json != null && json.size() > 0) {
			List<ORGUserRole> op = orgUserRoleRepository.getListByKeyInValues(conn, "role_id", json.toArray());
			for (ORGUserRole orgPermission : op) {
				list.add(orgPermission);
			}
		}
		return list;
	}

}
