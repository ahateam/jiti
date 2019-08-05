package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import zyxhj.utils.data.EXP;

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

	/**
	 * 系统级第三方权限，会被
	 */
	private static HashMap<Long, ORGUserRole> SYS_ORG_USER_ROLE_MAP = new HashMap<>();

	public static ArrayList<ORGUserRole> SYS_ORG_USER_ROLE_LIST = new ArrayList<>();

	static {
		// 添加admin，member，股东，董事，监事等角色到系统中
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_outuser.roleId, ORGUserRole.role_outuser);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_user.roleId, ORGUserRole.role_user);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_admin.roleId, ORGUserRole.role_admin);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_shareHolder.roleId, ORGUserRole.role_shareHolder);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_shareDeputy.roleId, ORGUserRole.role_shareDeputy);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_shareFamily.roleId, ORGUserRole.role_shareFamily);

		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_director.roleId, ORGUserRole.role_director);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_dirChief.roleId, ORGUserRole.role_dirChief);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_dirVice.roleId, ORGUserRole.role_dirVice);

		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_supervisor.roleId, ORGUserRole.role_supervisor);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_supChief.roleId, ORGUserRole.role_supChief);
		SYS_ORG_USER_ROLE_MAP.put(ORGUserRole.role_supVice.roleId, ORGUserRole.role_supVice);

		Iterator<ORGUserRole> it = SYS_ORG_USER_ROLE_MAP.values().iterator();
		while (it.hasNext()) {
			SYS_ORG_USER_ROLE_LIST.add(it.next());
		}
	}

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

		return orgUserRoleRepository.update(conn,EXP.ins().key("org_id", orgId).andKey("role_id", roleId), renew, true);
	}

	public ORGUserRole getORGUserRoleById(DruidPooledConnection conn, Long orgId, Long roleId) throws Exception {
		// 先从系统缓存里取，再从缓存去，最后再查
		ORGUserRole role = SYS_ORG_USER_ROLE_MAP.get(roleId);
		if (role == null) {
			role = ORG_USER_ROLE_CACHE.getIfPresent(roleId);
			if (role == null) {
				// 从数据库中获取
				role = orgUserRoleRepository.get(conn, EXP.ins().key("org_id", orgId).andKey("role_id", roleId));
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
		return orgUserRoleRepository.getList(conn, EXP.ins().key("org_id", orgId), 512, 0);
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
			orp = SYS_ORG_USER_ROLE_MAP.get(or.roleId);
			if (orp != null) {
				list.add(orp);
			} else {
				// 放入json中 执行完成后去数据库中获取
				json.add(or.permissionId);
			}
		}
		
		if (json != null && json.size() > 0) {
			List<ORGUserRole> op = orgUserRoleRepository.getList(conn, EXP.ins().in("role_id",json.toArray()),null,null);
			for (ORGUserRole orgPermission : op) {
				list.add(orgPermission);
			}
		}
		return list;
	}

}
