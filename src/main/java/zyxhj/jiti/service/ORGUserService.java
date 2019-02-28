package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.utils.CodecUtils;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;

public class ORGUserService {

	private static Logger log = LoggerFactory.getLogger(ORGUserService.class);

	private ORGUserRepository orgUserRepository;
	private UserRepository userRepository;

	private ORGUserRoleService orgUserRoleService;
	private ORGUserGroupService orgUserGroupService;

	public ORGUserService() {
		try {
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			userRepository = Singleton.ins(UserRepository.class);

			orgUserRoleService = Singleton.ins(ORGUserRoleService.class);
			orgUserGroupService = Singleton.ins(ORGUserGroupService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static String array2JsonString(JSONArray arr) {
		if (arr == null || arr.size() <= 0) {
			return "[]";
		} else {
			return JSON.toJSONString(arr);
		}
	}

	private String obj2JsonString(JSONObject obj) {
		if (obj == null) {
			return "{}";
		} else {
			return JSON.toJSONString(obj);
		}
	}

	private JSONArray checkRoles(JSONArray roles) throws Exception {

		// 获取当前组织的角色列表
		ArrayList<ORGUserRole> oList = ORGUserRoleService.SYS_ORG_USER_ROLE_LIST;

		JSONArray ret = new JSONArray();
		boolean exist = false;
		for (int i = 0; i < roles.size(); i++) {

			Long roleId = roles.getLong(i);

			// 先判断是否在系统角色中

			for (ORGUserRole o : oList) {
				if (o.roleId.equals(roleId)) {
					// 匹配
					ret.add(roleId);
					exist = true;
					break;
				}
			}

			// 再判断是否在自定义角色中
			// TODO 暂时不支持自定义角色
		}

		if (!exist) {
			// 什么角色都没有，则分配最低级别的用户
			ret.add(ORGUserRole.role_user.roleId);
		}

		return ret;
	}

	public static JSONArray checkGroups(DruidPooledConnection conn, Long orgId, JSONArray groups) throws Exception {

		// 获取当前组织的分组列表
		ArrayList<ORGUserTagGroup> oList = ORGUserGroupService.SYS_ORG_USER_TAG_GROUP_LIST;

		// 自定义分组列表
		List<ORGUserTagGroup> cList = Singleton.ins(ORGUserGroupService.class).getTagGroups(conn, orgId);

		JSONArray ret = new JSONArray();
		boolean exist = false;

		// System.out.println("xxx" + JSON.toJSONString(groups));
		for (int i = 0; i < groups.size(); i++) {

			String keyword = StringUtils.trim(groups.getString(i));
			// System.out.println("------------" + keyword);
			// 先判断是否在系统分组中

			for (ORGUserTagGroup o : oList) {
				if (o.keyword.equals(keyword)) {
					// 匹配
					ret.add(o.groupId);
					exist = true;
					break;
				}
			}

			// 如果没匹配到系统分组，则 再判断是否在自定义分组中
			for (ORGUserTagGroup o : cList) {
				// System.out.println(">>>>>" + keyword + " ooo " + o.keyword);
				if (o.keyword.equals(keyword)) {
					// 匹配
					ret.add(o.groupId);
					// System.out.println(">>>>>" + keyword + " + " + o.groupId);
					exist = true;
					break;
				}
			}
		}

		if (!exist) {
			// 什么分组都没有，则分配最低级别的用户
			ret.add(ORGUserTagGroup.group_undefine.groupId);
		}

		return ret;

	}

	private void insertORGUser(DruidPooledConnection conn, Long orgId, Long userId, String address, String shareCerNo,
			String shareCerImg, Boolean shareCerHolder, Integer shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags) throws Exception {
		ORGUser or = new ORGUser();
		or.orgId = orgId;
		or.userId = userId;

		or.address = address;
		or.shareCerNo = shareCerNo;
		or.shareCerImg = shareCerImg;
		or.shareCerHolder = shareCerHolder;

		or.shareAmount = shareAmount;
		or.weight = weight;

		or.roles = array2JsonString(checkRoles(roles));
		or.groups = array2JsonString(checkGroups(conn, orgId, groups));
		or.tags = obj2JsonString(tags);

		orgUserRepository.insert(conn, or);
	}

	/**
	 * 创建组织用户
	 */
	public void createORGUser(DruidPooledConnection conn, Long orgId, String mobile, String realName, String idNumber,
			String address, String shareCerNo, String shareCerImg, Boolean shareCerHolder, Integer shareAmount,
			Integer weight, JSONArray roles, JSONArray groups, JSONObject tags) throws Exception {

		User extUser = userRepository.getByKey(conn, "id_number", idNumber);
		if (null == extUser) {
			// 用户完全不存在，则User和ORGUser记录都创建

			User newUser = new User();
			newUser.id = IDUtils.getSimpleId();
			newUser.createDate = new Date();
			newUser.realName = realName;
			newUser.mobile = mobile;
			newUser.idNumber = idNumber;

			// 默认密码,身份证后6位
			newUser.pwd = idNumber.substring(idNumber.length() - 6);

			// 创建用户
			userRepository.insert(conn, newUser);

			// 写入股东信息表
			insertORGUser(conn, orgId, newUser.id, address, shareCerNo, shareCerImg, shareCerHolder, shareAmount,
					weight, roles, groups, tags);

		} else {
			// 判断ORGUser是否存在

			ORGUser existor = orgUserRepository.getByKeys(conn, new String[] { "org_id", "user_id" },
					new Object[] { orgId, extUser.id });
			if (null == existor) {
				// ORGUser用户不存在，直接创建

				// 写入股东信息表
				insertORGUser(conn, orgId, extUser.id, address, shareCerNo, shareCerImg, shareCerHolder, shareAmount,
						weight, roles, groups, tags);
			} else {
				throw new ServerException(BaseRC.ECM_ORG_USER_EXIST);
			}
		}
	}

	/**
	 * 修改用户信息，身份证信息不能修改
	 */
	public int editUser(DruidPooledConnection conn, Long userId, String mobile, String realName, String pwd)
			throws Exception {
		User renew = new User();
		renew.mobile = mobile;
		renew.realName = realName;
		renew.pwd = pwd;

		return userRepository.updateByKey(conn, "id", userId, renew, true);
	}

	/**
	 * 移除组织的用户</br>
	 * 只修改ORGUser表，不删除user本身。
	 */
	public void delORGUser(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		orgUserRepository.deleteByKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId });
	}

	/**
	 * 修改组织的用户</br>
	 * 只修改ORGUser表，不变动user本身。
	 */
	public int editORGUser(DruidPooledConnection conn, Long orgId, Long userId, String address, String shareCerNo,
			String shareCerImg, Boolean shareCerHolder, Integer shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags) throws Exception {
		ORGUser renew = new ORGUser();
		renew.address = address;
		renew.shareCerNo = shareCerNo;
		renew.shareCerImg = shareCerImg;
		renew.shareCerHolder = shareCerHolder;

		renew.shareAmount = shareAmount;
		renew.weight = weight;
		renew.roles = array2JsonString(roles);
		renew.tags = obj2JsonString(tags);

		return orgUserRepository.updateByKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId }, renew, true);
	}

	public ORGUser getORGUserById(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		return orgUserRepository.getByKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId });
	}

	/**
	 * 根据组织编号和身份证号片段（生日），模糊查询
	 */
	public List<User> getORGUsersLikeIDNumber(DruidPooledConnection conn, Long orgId, String idNumber, Integer count,
			Integer offset) throws Exception {
		return orgUserRepository.getORGUsersLikeIDNumber(conn, orgId, idNumber, count, offset);
	}

	/**
	 * 根据组织编号和身份证号片段（生日），模糊查询
	 */
	public List<User> getORGUsersLikeRealName(DruidPooledConnection conn, Long orgId, String realName, Integer count,
			Integer offset) throws Exception {
		return orgUserRepository.getORGUsersLikeRealName(conn, orgId, realName, count, offset);
	}

	private void importORGUsersImpl(DruidPooledConnection conn, Long orgId, List<List<Object>> table) {
		for (List<Object> row : table) {

			try {
				String realName = ExcelUtils.getString(row.get(0));
				String idNumber = ExcelUtils.getString(row.get(1));
				String mobile = ExcelUtils.getString(row.get(2));
				Integer shareAmount = ExcelUtils.parseInt(row.get(3));
				Integer weight = ExcelUtils.parseInt(row.get(4));

				String address = ExcelUtils.getString(row.get(5));
				Boolean shareCerHolder = ExcelUtils.parseShiFou(row.get(6));
				String shareCerNo = ExcelUtils.getString(row.get(7));

				String dutyShareholders = ExcelUtils.getString(row.get(8));
				String dutyDirectors = ExcelUtils.getString(row.get(9));
				String dutyVisors = ExcelUtils.getString(row.get(10));
				String dutyOthers = ExcelUtils.getString(row.get(11));
				String dutyAdmins = ExcelUtils.getString(row.get(12));

				String groups = ExcelUtils.getString(row.get(13));
				String tags = ExcelUtils.getString(row.get(14));

				// 合并roles
				JSONArray roles = new JSONArray();
				JSONArray temp = null;
				{
					// 股东成员职务
					// public static final String role_shareHolder = "股东";
					// public static final String role_shareDeputy = "股东代表";

					String ts = StringUtils.trim(dutyShareholders);
					if (ts.equals(ORGUserRole.role_shareHolder.name)) {
						roles.add(ORGUserRole.role_shareHolder.roleId);
					} else if (ts.equals(ORGUserRole.role_shareDeputy.name)) {
						roles.add(ORGUserRole.role_shareDeputy.roleId);
					} else {
						// 无，不加
					}
				}

				{
					// 董事会职务
					// public static final String role_director = "董事";
					// public static final String role_dirChief = "董事长";
					// public static final String role_dirVice = "副董事长";

					String ts = StringUtils.trim(dutyDirectors);
					if (ts.equals(ORGUserRole.role_director.name)) {
						roles.add(ORGUserRole.role_director.roleId);
					} else if (ts.equals(ORGUserRole.role_dirChief.name)) {
						roles.add(ORGUserRole.role_dirChief.roleId);
					} else if (ts.equals(ORGUserRole.role_dirVice.name)) {
						roles.add(ORGUserRole.role_dirVice.roleId);
					} else {
						// 无，不加
					}
				}

				{
					// 监事会职务
					// public static final String role_supervisor = "监事";
					// public static final String role_supChief = "监事长";
					// public static final String role_supVice = "副监事长";

					String ts = StringUtils.trim(dutyVisors);
					if (ts.equals(ORGUserRole.role_supervisor.name)) {
						roles.add(ORGUserRole.role_supervisor.roleId);
					} else if (ts.equals(ORGUserRole.role_supChief.name)) {
						roles.add(ORGUserRole.role_supChief.roleId);
					} else if (ts.equals(ORGUserRole.role_supVice.name)) {
						roles.add(ORGUserRole.role_supVice.roleId);
					} else {
						// 无，不加
					}
				}

				{
					// 管理角色
					// public static final String role_user = "用户";
					// public static final String role_admin = "管理员";

					temp = CodecUtils.convertCommaStringList2JSONArray(dutyAdmins);
					for (int i = 0; i < temp.size(); i++) {
						String ts = StringUtils.trim(temp.getString(i));

						if (ts.equals(ORGUserRole.role_user.name)) {
							roles.add(ORGUserRole.role_user.roleId);
						} else if (ts.equals(ORGUserRole.role_admin.name)) {
							roles.add(ORGUserRole.role_admin.roleId);
						} else {
							// 无，不加
						}
					}
				}

				{

					// TODO 这个地方要跟系统中的其它角色做匹配
					// 其它角色
					temp = CodecUtils.convertCommaStringList2JSONArray(dutyOthers);

					// for (int i = 0; i < temp.size(); i++) {
					// String ts = StringUtils.trim(temp.getString(i));
					// if (ts.equals("null") || ts.equals("无")) {
					// // 无和null，不加
					// } else {
					// roles.add(ts);
					// }
					// }
				}

				// 开始处理分组和标签

				JSONArray arrGroups = new JSONArray();

				{
					// 分组
					temp = CodecUtils.convertCommaStringList2JSONArray(groups);

					for (int i = 0; i < temp.size(); i++) {
						String ts = StringUtils.trim(temp.getString(i));
						if (ts.equals("null") || ts.equals("无")) {
							// 无和null，不加
						} else {
							arrGroups.add(ts);
						}
					}

				}

				JSONObject joTags = new JSONObject();
				JSONArray arrTags = new JSONArray();
				{
					// 标签
					temp = CodecUtils.convertCommaStringList2JSONArray(tags);

					for (int i = 0; i < temp.size(); i++) {
						String ts = StringUtils.trim(temp.getString(i));
						if (ts.equals("null") || ts.equals("无")) {
							// 无和null，不加
						} else {
							arrTags.add(ts);
						}
					}

					if (arrTags.size() > 0) {
						joTags.put("tags", arrTags);
					}
				}

				// System.out.println("----------" + JSON.toJSONString(joTags));

				createORGUser(conn, orgId, mobile, realName, idNumber, address, shareCerNo, "", shareCerHolder,
						shareAmount, weight, roles, arrGroups, joTags);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * 导入组织用户列表
	 */
	public void importORGUsers(DruidPooledConnection conn, Long orgId, String url) throws Exception {
		List<List<Object>> table = ExcelUtils.readExcelOnline(url, 1, 15, 0);

		importORGUsersImpl(conn, orgId, table);
	}

	public void importORGUsersOffline(DruidPooledConnection conn, Long orgId, String fileName) throws Exception {
		List<List<Object>> table = ExcelUtils.readExcelFile(fileName, 1, 15, 0);
		importORGUsersImpl(conn, orgId, table);
	}

	public List<User> getUsersByMobile(DruidPooledConnection conn, String mobile, Integer count, Integer offset)
			throws Exception {
		List<User> ret = userRepository.getListByKey(conn, "mobile", mobile, count, offset);
		for (User u : ret) {
			u.pwd = null;
		}
		return ret;
	}

	/**
	 * 设置用户角色
	 */
	public int setORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, JSONArray roles) throws Exception {
		return orgUserRepository.setORGUserRoles(conn, orgId, userId, roles);
	}

	/**
	 * 查询用户
	 */
	public JSONArray getORGUsers(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		List<ORGUser> ors = orgUserRepository.getListByKey(conn, "org_id", orgId, count, offset);

		return getORGUsersInfo(conn, ors);
	}

	/**
	 * 根据权限查询用户
	 */
	public JSONArray getORGUsersByRoles(DruidPooledConnection conn, Long orgId, JSONArray roles, Integer count,
			Integer offset) throws Exception {
		List<ORGUser> ors = orgUserRepository.getORGUsersByRoles(conn, orgId, roles, count, offset);

		return getORGUsersInfo(conn, ors);
	}

	/**
	 * 根据分组（groups tags等）查询用户
	 */
	public JSONArray getORGUsersByGroups(DruidPooledConnection conn, Long orgId, JSONArray groups, Integer count,
			Integer offset) throws Exception {
		List<ORGUser> ors = orgUserRepository.getORGUsersByGroups(conn, orgId, groups, count, offset);

		return getORGUsersInfo(conn, ors);
	}

	/**
	 * 根据标签（groups tags等）查询用户
	 */
	public JSONArray getORGUsersByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws Exception {
		List<ORGUser> ors = orgUserRepository.getORGUsersByTags(conn, orgId, tags, count, offset);

		return getORGUsersInfo(conn, ors);
	}

	private JSONArray getORGUsersInfo(DruidPooledConnection conn, List<ORGUser> ors) throws Exception {
		if (ors == null || ors.size() == 0) {
			return new JSONArray();
		} else {
			String[] values = new String[ors.size()];
			for (int i = 0; i < ors.size(); i++) {
				values[i] = ors.get(i).userId.toString();
			}
			List<User> us = userRepository.getListByKeyInValues(conn, "id", values);
			JSONArray ret = new JSONArray();
			for (int i = 0; i < ors.size(); i++) {
				ORGUser or = ors.get(i);
				for (int j = 0; j < us.size(); j++) {
					User u = us.get(j);
					u.pwd = null;
					if (or.userId.equals(u.id)) {
						// 找到匹配的
						JSONObject jo = new JSONObject();
						jo.put("user", u);
						jo.put("orgUser", or);
						ret.add(jo);
						break;
					}
				}
			}
			return ret;
		}
	}
}
