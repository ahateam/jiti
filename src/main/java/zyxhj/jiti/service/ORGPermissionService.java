package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORGPermission;
import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.jiti.repository.ORGPermissionRelaRepository;
import zyxhj.jiti.repository.ORGPermissionRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;

/**
 * 第三方用户自定义角色service
 *
 */
public class ORGPermissionService {

	private static Logger log = LoggerFactory.getLogger(ORGPermissionService.class);

//	private static Cache<Long, ORGPermission> ORG_PERMISSION = CacheBuilder.newBuilder()//
//			.expireAfterAccess(5, TimeUnit.MINUTES)//
//			.maximumSize(1000)//
//			.build();

	/**
	 * 系统级第三方权限，会被
	 */
	private static HashMap<Long, ORGPermission> SYS_ORG_PERMISSION_MAP = new HashMap<>();

	public static ArrayList<ORGPermission> SYS_ORG_PERMISSION_LIST = new ArrayList<>();

	static {
		// 添加权限到权限系统中
		SYS_ORG_PERMISSION_MAP.put(ORGPermission.per_initiate_vote.id, ORGPermission.per_initiate_vote);
		SYS_ORG_PERMISSION_MAP.put(ORGPermission.per_asset_management.id, ORGPermission.per_asset_management);
		SYS_ORG_PERMISSION_MAP.put(ORGPermission.per_role_management.id, ORGPermission.per_role_management);
		SYS_ORG_PERMISSION_MAP.put(ORGPermission.per_examine.id, ORGPermission.per_examine);
		SYS_ORG_PERMISSION_MAP.put(ORGPermission.per_feparate_family.id, ORGPermission.per_feparate_family);
		SYS_ORG_PERMISSION_MAP.put(ORGPermission.per_share_change.id, ORGPermission.per_share_change);

		Iterator<ORGPermission> it = SYS_ORG_PERMISSION_MAP.values().iterator();
		while (it.hasNext()) {
			SYS_ORG_PERMISSION_LIST.add(it.next());
		}

	}

	private ORGPermissionRepository orgPermissionRepository;
	private ORGPermissionRelaRepository orgPermissionRelRepository;

	public ORGPermissionService() {
		try {
			orgPermissionRepository = Singleton.ins(ORGPermissionRepository.class);
			orgPermissionRelRepository = Singleton.ins(ORGPermissionRelaRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 获取所有的自定义权限列表
	 * 
	 * @return 返回权限列表
	 */
	public List<ORGPermission> getPermissions(DruidPooledConnection conn, Integer count, Integer offset)
			throws Exception {
		return orgPermissionRepository.getList(conn, count, offset);
	}

	/**
	 * 添加自定义权限
	 * 
	 * @return 权限
	 * @throws Exception
	 */
	public ORGPermission insertPermission(DruidPooledConnection conn, String permissionName, String remark)
			throws Exception {
		ORGPermission orp = new ORGPermission();
		orp.id = IDUtils.getSimpleId();
		orp.name = permissionName;
		orp.remark = remark;
		orgPermissionRepository.insert(conn, orp);
		return orp;
	}

	// 根据角色查看权限列表
	public List<ORGPermission> getPermissionsByRole(DruidPooledConnection conn, Long orgId, String roleId)
			throws Exception {
		JSONArray jsonRole = JSONArray.parseArray(roleId);
		JSONArray json = new JSONArray();
		List<ORGPermission> list = new ArrayList<ORGPermission>();
		ORGPermission orp = new ORGPermission();
		List<ORGPermissionRel> li = orgPermissionRelRepository.getPermissionsId(conn, orgId, jsonRole);
		// 从MAP中查找
		for (ORGPermissionRel or : li) {
			orp = SYS_ORG_PERMISSION_MAP.get(or.permissionId);
			if (orp != null) {
				list.add(orp);
			} else {
				json.add(or.permissionId);
			}
		}
		if (json != null && json.size() > 0) {
			List<ORGPermission> op = orgPermissionRepository.getListByKeyInValues(conn, "id", json.toArray());
			for (ORGPermission orgPermission : op) {
				list.add(orgPermission);
			}
		}
		return list;

	}

	// 给权限添加角色
	public int insertPermissionRole(DruidPooledConnection conn, Long orgId, Long permissionId, String role)
			throws Exception {

		orgPermissionRelRepository.deleteByANDKeys(conn, new String[] { "org_id", "permission_id" },
				new Object[] { orgId, permissionId });

		JSONArray json = JSONArray.parseArray(role);
		if (json.size() > 0 && json != null) {
			List<ORGPermissionRel> list = new ArrayList<ORGPermissionRel>();

			for (int i = 0; i < json.size(); i++) {
				ORGPermissionRel opr = new ORGPermissionRel();
				opr.orgId = orgId;
				opr.permissionId = permissionId;
				opr.roleId = json.getLong(i);
				list.add(opr);

			}
			return orgPermissionRelRepository.insertList(conn, list);
		} else {
			return 0;
		}
	}

	// 查看多个角色权限列表
	public List<ORGPermission> getPermissionsByRoles(DruidPooledConnection conn, Long orgId, JSONArray orgRoles)
			throws Exception {
		List<ORGPermission> list = new ArrayList<ORGPermission>();
		ORGPermission orp = new ORGPermission();

		JSONArray json = new JSONArray();
		List<ORGPermissionRel> li = orgPermissionRelRepository.getPermissionsId(conn, orgId, orgRoles);
		// 从MAP中查找
		for (ORGPermissionRel or : li) {
			orp = SYS_ORG_PERMISSION_MAP.get(or.permissionId);
			if (orp != null) {
				list.add(orp);
			} else {
				// 放入json中 执行完成后去数据库中获取
				json.add(or.permissionId);
			}
		}
		if (json != null && json.size() > 0) {
			List<ORGPermission> op = orgPermissionRepository.getListByKeyInValues(conn, "id", json.toArray());
			for (ORGPermission orgPermission : op) {
				list.add(orgPermission);
			}
		}
		return list;
	}
}
