package zyxhj.jiti.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.vertx.core.Vertx;
import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.jiti.domain.Family;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserImportRecord;
import zyxhj.jiti.domain.ORGUserImportTask;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.jiti.repository.FamilyRepository;
import zyxhj.jiti.repository.ORGUserImportRecordRepository;
import zyxhj.jiti.repository.ORGUserImportTaskRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.utils.CodecUtils;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.DataSource;

public class ORGUserService {

	private static Logger log = LoggerFactory.getLogger(ORGUserService.class);

	private ORGUserRepository orgUserRepository;
	private UserRepository userRepository;

	private FamilyRepository familyRepository;
	private ORGUserImportTaskRepository orgUserImportTaskRepository;
	private ORGUserImportRecordRepository orgUserImportRecordRepository;

	public ORGUserService() {
		try {
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			userRepository = Singleton.ins(UserRepository.class);

			familyRepository = Singleton.ins(FamilyRepository.class);
			orgUserImportTaskRepository = Singleton.ins(ORGUserImportTaskRepository.class);
			orgUserImportRecordRepository = Singleton.ins(ORGUserImportRecordRepository.class);
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

	/**
	 * 户缓存，缓存2分钟
	 */
	public static Cache<String, Family> FAMILY_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(30, TimeUnit.SECONDS)// 缓存对象有效时间，2天
			.maximumSize(100)//
			.build();

	private void insertORGUser(DruidPooledConnection conn, Long orgId, Long userId, String address, String shareCerNo,
			String shareCerImg, Boolean shareCerHolder, Integer shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags, String familyNumber, String familyMaster) throws Exception {
		ORGUser or = new ORGUser();
		Family fa = new Family();
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

		or.familyNumber = familyNumber;
		or.familyMaster = familyMaster;

		if (StringUtils.isNotBlank(familyNumber)) {
			// 查询户序号在family表里是否拥有 有则把usreid插入到户成员下 无则添加户

			Family fn = FAMILY_CACHE.getIfPresent(familyNumber);
			if (fn == null) {
				// 缓存中没有，从数据库中获取
				fn = familyRepository.getByANDKeys(conn, new String[] { "org_id", "family_number" },
						new Object[] { orgId, familyNumber });
				if (fn != null) {
					// 放入缓存
					FAMILY_CACHE.put(familyNumber, fn);
				}
			}

			// 从缓存和数据库都取了一遍，
			if (fn == null) {
				// 如果空需要创建
				// 添加户
				fa.id = IDUtils.getSimpleId();
				fa.orgId = orgId;
				fa.familyNumber = familyNumber;
				fa.familyMaster = familyMaster;

				// 将当前用户的id插入到户成员里
				JSONArray json = new JSONArray();
				json.add(userId);
				fa.familyMember = json.toString();
				familyRepository.insert(conn, fa);
			} else {
				// 添加到户成员下
				JSONArray json = JSONArray.parseArray(fn.familyMember);
				json.add(userId);

				fa.familyNumber = familyNumber;
				fa.familyMaster = familyMaster;
				fa.familyMember = json.toString();

				familyRepository.updateByKey(conn, "family_number", familyNumber, fa, true);

			}

		}
		orgUserRepository.insert(conn, or);
	}

	/**
	 * 创建组织用户
	 */
	public void createORGUser(DruidPooledConnection conn, Long orgId, String mobile, String realName, String idNumber,
			String address, String shareCerNo, String shareCerImg, Boolean shareCerHolder, Integer shareAmount,
			Integer weight, JSONArray roles, JSONArray groups, JSONObject tags, String familyNumber,
			String familyMaster) throws Exception {

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
					weight, roles, groups, tags, familyNumber, familyMaster);

		} else {
			// 判断ORGUser是否存在
			ORGUser existor = orgUserRepository.getByANDKeys(conn, new String[] { "org_id", "user_id" },
					new Object[] { orgId, extUser.id });
			if (null == existor) {
				// ORGUser用户不存在，直接创建

				// 写入股东信息表
				insertORGUser(conn, orgId, extUser.id, address, shareCerNo, shareCerImg, shareCerHolder, shareAmount,
						weight, roles, groups, tags, familyNumber, familyMaster);
			} else {
				// System.out
				// .println(StringUtils.join("xxxx>>orgId>", orgId, " - userId>", extUser.id, "
				// - id=", idNumber));
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
		orgUserRepository.deleteByANDKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId });
	}

	/**
	 * 修改组织的用户</br>
	 * 只修改ORGUser表，不变动user本身。
	 */
	public int editORGUser(DruidPooledConnection conn, Long orgId, Long userId, String address, String shareCerNo,
			String shareCerImg, Boolean shareCerHolder, Integer shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags, String familyNumber, String familyMaster) throws Exception {
		ORGUser renew = new ORGUser();
		renew.address = address;
		renew.shareCerNo = shareCerNo;
		renew.shareCerImg = shareCerImg;
		renew.shareCerHolder = shareCerHolder;

		renew.shareAmount = shareAmount;
		renew.weight = weight;
		renew.roles = array2JsonString(roles);
		renew.tags = obj2JsonString(tags);
		renew.groups = array2JsonString(groups);

		renew.familyNumber = familyNumber;
		renew.familyMaster = familyMaster;

		return orgUserRepository.updateByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId }, renew, true);

	}

	public ORGUser getORGUserById(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		return orgUserRepository.getByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId });
	}

	/**
	 * 根据组织编号和身份证号片段（生日），模糊查询
	 */
	public JSONArray getORGUsersLikeIDNumber(DruidPooledConnection conn, Long orgId, String idNumber, Integer count,
			Integer offset) throws Exception {
		List<User> users = orgUserRepository.getORGUsersLikeIDNumber(conn, orgId, idNumber, count, offset);
		return getORGUsersInfoByUsers(conn, orgId, users);
		// return orgUserRepository.getORGUsersLikeIDNumber(conn, orgId, idNumber,
		// count, offset);
	}

	/**
	 * 根据组织编号和身份证号片段（生日），模糊查询
	 */
	public JSONArray getORGUsersLikeRealName(DruidPooledConnection conn, Long orgId, String realName, Integer count,
			Integer offset) throws Exception {
		List<User> users = orgUserRepository.getORGUsersLikeRealName(conn, orgId, realName, count, offset);
		return getORGUsersInfoByUsers(conn, orgId, users);

		// return orgUserRepository.getORGUsersLikeRealName(conn, orgId, realName,
		// count, offset);
	}

	private void importORGUsersImpl(DruidPooledConnection conn, Long orgId, List<List<Object>> table) {
		for (List<Object> row : table) {

			try {
				int tt = 0;
				String familyNumber = ExcelUtils.getString(row.get(tt++));// 户序号

				if (StringUtils.isBlank(familyNumber)) {
					// 户序号为空，直接跳过
					log.error("---->>户序号为空");
					continue;
				}

				String realName = ExcelUtils.getString(row.get(tt++));
				String idNumber = ExcelUtils.getString(row.get(tt++));
				String mobile = ExcelUtils.getString(row.get(tt++));
				Integer shareAmount = ExcelUtils.parseInt(row.get(tt++));

				Integer weight = ExcelUtils.parseInt(row.get(tt++));
				String address = ExcelUtils.getString(row.get(tt++));
				String familyMaster = ExcelUtils.getString(row.get(tt++));
				Boolean shareCerHolder = ExcelUtils.parseShiFou(row.get(tt++));
				String shareCerNo = ExcelUtils.getString(row.get(tt++));

				String dutyShareholders = ExcelUtils.getString(row.get(tt++));
				String dutyDirectors = ExcelUtils.getString(row.get(tt++));
				String dutyVisors = ExcelUtils.getString(row.get(tt++));
				String dutyOthers = ExcelUtils.getString(row.get(tt++));
				String dutyAdmins = ExcelUtils.getString(row.get(tt++));

				String groups = ExcelUtils.getString(row.get(tt++));
				String tags = ExcelUtils.getString(row.get(tt++));

				// 合并roles
				JSONArray roles = new JSONArray();
				JSONArray temp = null;
				{
					// 股东成员职务
					String ts = StringUtils.trim(dutyShareholders);
					if (ts.equals(ORGUserRole.role_shareHolder.name)) {
						roles.add(ORGUserRole.role_shareHolder.roleId);// 股东
					} else if (ts.equals(ORGUserRole.role_shareDeputy.name)) {
						roles.add(ORGUserRole.role_shareDeputy.roleId);// 股东代表
					} else if (ts.equals(ORGUserRole.role_shareFamily.name)) {
						roles.add(ORGUserRole.role_shareFamily.roleId);// 股东户代表
					} else {
						// 无，不加
					}
				}

				{
					// 董事会职务
					String ts = StringUtils.trim(dutyDirectors);
					if (ts.equals(ORGUserRole.role_director.name)) {
						roles.add(ORGUserRole.role_director.roleId);// 董事
					} else if (ts.equals(ORGUserRole.role_dirChief.name)) {
						roles.add(ORGUserRole.role_dirChief.roleId);// 董事长
					} else if (ts.equals(ORGUserRole.role_dirVice.name)) {
						roles.add(ORGUserRole.role_dirVice.roleId);// 副董事长
					} else {
						// 无，不加
					}
				}

				{
					// 监事会职务
					String ts = StringUtils.trim(dutyVisors);
					if (ts.equals(ORGUserRole.role_supervisor.name)) {
						roles.add(ORGUserRole.role_supervisor.roleId);// 监事
					} else if (ts.equals(ORGUserRole.role_supChief.name)) {
						roles.add(ORGUserRole.role_supChief.roleId);// 监事长
					} else if (ts.equals(ORGUserRole.role_supVice.name)) {
						roles.add(ORGUserRole.role_supVice.roleId);// 副监事长
					} else {
						// 无，不加
					}
				}

				{
					// 其它管理角色

					temp = CodecUtils.convertCommaStringList2JSONArray(dutyAdmins);
					for (int i = 0; i < temp.size(); i++) {
						String ts = StringUtils.trim(temp.getString(i));

						if (ts.equals(ORGUserRole.role_user.name)) {
							roles.add(ORGUserRole.role_user.roleId);// 用户
						} else if (ts.equals(ORGUserRole.role_outuser.name)) {
							roles.add(ORGUserRole.role_outuser.roleId);// 外部人员
						} else if (ts.equals(ORGUserRole.role_admin.name)) {
							roles.add(ORGUserRole.role_admin.roleId);// 管理员
						} else {
							// 无，不填默认当作用户
							roles.add(ORGUserRole.role_user.roleId);// 用户
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

				// 处理idNunber为空的问题
				if (StringUtils.isBlank(idNumber)) {
					idNumber = StringUtils.join(orgId, "-", familyNumber, "-", IDUtils.getHexSimpleId());
				}
				if (StringUtils.isBlank(mobile)) {
					mobile = StringUtils.join(orgId, "-", familyNumber, "-", IDUtils.getHexSimpleId());
				}

				createORGUser(conn, orgId, mobile, realName, idNumber, address, shareCerNo, "", shareCerHolder,
						shareAmount, weight, roles, arrGroups, joTags, familyNumber, familyMaster);
				Thread.sleep(5L);
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	/**
	 * 导入组织用户列表
	 */
	public void importORGUsers(DruidPooledConnection conn, Long orgId, String url) throws Exception {
		List<List<Object>> table = ExcelUtils.readExcelOnline(url, 1, 17, 0);

		importORGUsersImpl(conn, orgId, table);
	}

	public void importORGUsersOffline(DruidPooledConnection conn, Long orgId, String fileName) throws Exception {
		List<List<Object>> table = ExcelUtils.readExcelFile(fileName, 1, 17, 0);
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
	public JSONArray getORGUsersByRoles(DruidPooledConnection conn, Long orgId, String[] roles, Integer count,
			Integer offset) throws Exception {
		List<ORGUser> ors = orgUserRepository.getORGUsersByRoles(conn, orgId, roles, count, offset);

		return getORGUsersInfo(conn, ors);
	}

	/**
	 * 根据分组（groups tags等）查询用户
	 */
	public JSONArray getORGUsersByGroups(DruidPooledConnection conn, Long orgId, String[] groups, Integer count,
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

	private JSONArray getORGUsersInfoByUsers(DruidPooledConnection conn, Long orgId, List<User> ors) throws Exception {
		if (ors == null || ors.size() == 0) {
			return new JSONArray();
		} else {
			Object[] values = new Object[ors.size()];
			for (int i = 0; i < ors.size(); i++) {
				values[i] = ors.get(i).id;
			}

			List<ORGUser> us = orgUserRepository.getORGUsersInfoByUsers(conn, orgId, values);

			JSONArray ret = new JSONArray();
			for (int i = 0; i < ors.size(); i++) {
				User u = ors.get(i);
				for (int j = 0; j < us.size(); j++) {
					ORGUser or = us.get(j);
					u.pwd = null;
					if (u.id.equals(or.userId)) {
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

	/**
	 * 根据ORGUser列表，进一步查询并返回User信息
	 */
	private JSONArray getORGUsersInfo(DruidPooledConnection conn, List<ORGUser> ors) throws Exception {
		if (ors == null || ors.size() == 0) {
			return new JSONArray();
		} else {
			Object[] values = new Object[ors.size()];
			for (int i = 0; i < ors.size(); i++) {
				values[i] = ors.get(i).userId;
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

	public void batchEditORGUsersGroups(DruidPooledConnection conn, Long orgId, JSONArray userIds, Long groups)
			throws Exception {
		orgUserRepository.batchEditORGUsersGroups(conn, orgId, userIds, groups);
	}

	// 创建组织角色导入任务
	public void createORGUserImportTask(DruidPooledConnection conn, Long orgId, Long userId, String name)
			throws Exception {
		ORGUserImportTask orgUserImport = new ORGUserImportTask();
		orgUserImport.id = IDUtils.getSimpleId();
		orgUserImport.orgId = orgId;
		orgUserImport.userId = userId;
		orgUserImport.name = name;
		orgUserImport.createTime = new Date();
		orgUserImport.sum = 0;
		orgUserImport.completion = 0;
		orgUserImport.notCompletion = 0;
		orgUserImport.success = 0;
		orgUserImport.status = AssetImportTask.STATUS.WAIT.v();
		orgUserImportTaskRepository.insert(conn, orgUserImport);
	}

	// 查询组织用户导入任务
	public List<ORGUserImportTask> getORGUserImportTasks(DruidPooledConnection conn, Long orgId, Long userId,
			Integer count, Integer offset) throws Exception {
		return orgUserImportTaskRepository.getORGUserImportTasks(conn, orgId, userId, count, offset);
	}

	// 查询组织用户导入任务信息
	public ORGUserImportTask getORGUserImportTask(DruidPooledConnection conn, Long importTaskId, Long orgId,
			Long userId) throws Exception {
		return orgUserImportTaskRepository.getByANDKeys(conn, new String[] { "id", "org_id", "user_id" },
				new Object[] { importTaskId, orgId, userId });
	}

	public void importORGUserRecord(DruidPooledConnection conn, Long orgId, Long userId, String url, Long importTaskId)
			throws Exception {
		Integer sum = 0;
		JSONArray json = JSONArray.parseArray(url);
		for (int o = 0; o < json.size(); o++) {
			List<ORGUserImportRecord> list = new ArrayList<ORGUserImportRecord>();
			// 1行表头，17列，文件格式写死的
			List<List<Object>> table = ExcelUtils.readExcelOnline(json.getString(o), 1, 17, 0);
			for (List<Object> row : table) {
				ORGUserImportRecord orgUserImportRecord = new ORGUserImportRecord();
				try {
					int tt = 0;
					orgUserImportRecord.familyNumber = ExcelUtils.getString(row.get(tt++));// 户序号

					if (StringUtils.isBlank(orgUserImportRecord.familyNumber)) {
						// 户序号为空，直接跳过
						log.error("---->>户序号为空");
						continue;
					}
					orgUserImportRecord.id = IDUtils.getSimpleId();
					orgUserImportRecord.orgId = orgId;
					orgUserImportRecord.userId = userId;
					orgUserImportRecord.taskId = importTaskId;
					orgUserImportRecord.status = ORGUserImportRecord.STATUS.UNDETECTED.v();
					orgUserImportRecord.realName = ExcelUtils.getString(row.get(tt++));
					orgUserImportRecord.idNumber = ExcelUtils.getString(row.get(tt++));
					orgUserImportRecord.mobile = ExcelUtils.getString(row.get(tt++));
					orgUserImportRecord.shareAmount = ExcelUtils.parseInt(row.get(tt++));

					orgUserImportRecord.weight = ExcelUtils.parseInt(row.get(tt++));
					orgUserImportRecord.address = ExcelUtils.getString(row.get(tt++));
					orgUserImportRecord.familyMaster = ExcelUtils.getString(row.get(tt++));
					orgUserImportRecord.shareCerHolder = ExcelUtils.parseShiFou(row.get(tt++));
					orgUserImportRecord.shareCerNo = ExcelUtils.getString(row.get(tt++));

					String dutyShareholders = ExcelUtils.getString(row.get(tt++));
					String dutyDirectors = ExcelUtils.getString(row.get(tt++));
					String dutyVisors = ExcelUtils.getString(row.get(tt++));
					String dutyOthers = ExcelUtils.getString(row.get(tt++));
					String dutyAdmins = ExcelUtils.getString(row.get(tt++));

					orgUserImportRecord.groups = ExcelUtils.getString(row.get(tt++));
					orgUserImportRecord.tags = ExcelUtils.getString(row.get(tt++));

					// 合并roles
					JSONArray roles = new JSONArray();
					JSONArray temp = null;
					{
						// 股东成员职务
						String ts = StringUtils.trim(dutyShareholders);
						if (ts.equals(ORGUserRole.role_shareHolder.name)) {
							roles.add(ORGUserRole.role_shareHolder.roleId);// 股东
						} else if (ts.equals(ORGUserRole.role_shareDeputy.name)) {
							roles.add(ORGUserRole.role_shareDeputy.roleId);// 股东代表
						} else if (ts.equals(ORGUserRole.role_shareFamily.name)) {
							roles.add(ORGUserRole.role_shareFamily.roleId);// 股东户代表
						} else {
							// 无，不加
						}
					}

					{
						// 董事会职务
						String ts = StringUtils.trim(dutyDirectors);
						if (ts.equals(ORGUserRole.role_director.name)) {
							roles.add(ORGUserRole.role_director.roleId);// 董事
						} else if (ts.equals(ORGUserRole.role_dirChief.name)) {
							roles.add(ORGUserRole.role_dirChief.roleId);// 董事长
						} else if (ts.equals(ORGUserRole.role_dirVice.name)) {
							roles.add(ORGUserRole.role_dirVice.roleId);// 副董事长
						} else {
							// 无，不加
						}
					}

					{
						// 监事会职务
						String ts = StringUtils.trim(dutyVisors);
						if (ts.equals(ORGUserRole.role_supervisor.name)) {
							roles.add(ORGUserRole.role_supervisor.roleId);// 监事
						} else if (ts.equals(ORGUserRole.role_supChief.name)) {
							roles.add(ORGUserRole.role_supChief.roleId);// 监事长
						} else if (ts.equals(ORGUserRole.role_supVice.name)) {
							roles.add(ORGUserRole.role_supVice.roleId);// 副监事长
						} else {
							// 无，不加
						}
					}

					{
						// 其它管理角色

						temp = CodecUtils.convertCommaStringList2JSONArray(dutyAdmins);
						for (int i = 0; i < temp.size(); i++) {
							String ts = StringUtils.trim(temp.getString(i));

							if (ts.equals(ORGUserRole.role_user.name)) {
								roles.add(ORGUserRole.role_user.roleId);// 用户
							} else if (ts.equals(ORGUserRole.role_outuser.name)) {
								roles.add(ORGUserRole.role_outuser.roleId);// 外部人员
							} else if (ts.equals(ORGUserRole.role_admin.name)) {
								roles.add(ORGUserRole.role_admin.roleId);// 管理员
							} else {
								// 无，不填默认当作用户
								roles.add(ORGUserRole.role_user.roleId);// 用户
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

					orgUserImportRecord.roles = roles.toString();

					// 开始处理分组和标签

					JSONArray arrGroups = new JSONArray();

					{
						// 分组
						temp = CodecUtils.convertCommaStringList2JSONArray(orgUserImportRecord.groups);

						for (int i = 0; i < temp.size(); i++) {
							String ts = StringUtils.trim(temp.getString(i));
							if (ts.equals("null") || ts.equals("无")) {
								// 无和null，不加
							} else {
								arrGroups.add(ts);
							}
						}
						orgUserImportRecord.groups = arrGroups.toJSONString();
					}

					JSONObject joTags = new JSONObject();
					JSONArray arrTags = new JSONArray();
					{
						// 标签
						temp = CodecUtils.convertCommaStringList2JSONArray(orgUserImportRecord.tags);

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
						orgUserImportRecord.tags = joTags.toJSONString();
					}

					// 处理idNunber为空的问题
					if (StringUtils.isBlank(orgUserImportRecord.idNumber)) {
						orgUserImportRecord.idNumber = StringUtils.join(orgId, "-", orgUserImportRecord.familyNumber,
								"-", IDUtils.getHexSimpleId());
					}
					if (StringUtils.isBlank(orgUserImportRecord.mobile)) {
						orgUserImportRecord.mobile = StringUtils.join(orgId, "-", orgUserImportRecord.familyNumber, "-",
								IDUtils.getHexSimpleId());
					}
					sum++;
					list.add(orgUserImportRecord);

					if (list.size() % 10 == 0) {
						orgUserImportRecordRepository.insertList(conn, list);
						list = new ArrayList<ORGUserImportRecord>();
					}
					if (sum == table.size()) {
						orgUserImportRecordRepository.insertList(conn, list);
					}
					Thread.sleep(5L);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
		// 添加总数到任务表中
		orgUserImportTaskRepository.countImportTaskSum(conn, importTaskId, sum);
	}

	// 组织用户回调页面
	public List<ORGUserImportRecord> getORGUserImportRecords(DruidPooledConnection conn, Long orgId, Long importTaskId,
			Integer count, Integer offset) throws Exception {
		return orgUserImportRecordRepository.getListByANDKeys(conn, new String[] { "org_id", "task_id" },
				new Object[] { orgId, importTaskId }, count, offset);
	}

	private void imp(DruidPooledConnection conn, Long orgId, List<ORGUserImportRecord> orgUserRec, Long importTaskId)
			throws Exception {
		for (ORGUserImportRecord orgUserImportRecord : orgUserRec) {
			String mobile = orgUserImportRecord.mobile;
			String realName = orgUserImportRecord.realName;
			String idNumber = orgUserImportRecord.idNumber;
			String address = orgUserImportRecord.address;
			String shareCerNo = orgUserImportRecord.shareCerNo;
			Boolean shareCerHolder = orgUserImportRecord.shareCerHolder;
			Integer shareAmount = orgUserImportRecord.shareAmount;
			Integer weight = orgUserImportRecord.weight;
			JSONArray roles = JSONArray.parseArray(orgUserImportRecord.roles);
			JSONArray groups = JSONArray.parseArray(orgUserImportRecord.groups);
			JSONObject tags = JSONObject.parseObject(orgUserImportRecord.tags);
			String familyNumber = orgUserImportRecord.familyNumber;
			String familyMaster = orgUserImportRecord.familyMaster;

			try {
				createORGUser(conn, orgId, mobile, realName, idNumber, address, shareCerNo, "", shareCerHolder,
						shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
				orgUserImportRecord.status = ORGUserImportRecord.STATUS.COMPLETION.v();
				// 修改导入数据状态为通过
				orgUserImportRecordRepository.updateStatus(conn, orgUserImportRecord.id, orgUserImportRecord.status);
				// 导入任务 成功数+1
				orgUserImportTaskRepository.countORGUserImportCompletionTask(conn, importTaskId);
			} catch (Exception e) {
				orgUserImportRecord.status = ORGUserImportRecord.STATUS.NOTCOMPLETION.v();
				// 修改导入数据状态为失败
				orgUserImportRecordRepository.updateStatus(conn, orgUserImportRecord.id, orgUserImportRecord.status);
				// 导入任务 失败数+1
				orgUserImportTaskRepository.countORGUserImportNotCompletionTask(conn, importTaskId);
			}
		}
	}

	// 开始导入数据
	public void importORGUser(Long orgId, Long importTaskId) throws Exception {

		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DruidDataSource dds;
			DruidPooledConnection conn = null;
			try {
				dds = DataSource.getDruidDataSource("rdsDefault.prop");
				conn = (DruidPooledConnection) dds.getConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// 修改导入任务状态为正在导入
				ORGUserImportTask orgUs = new ORGUserImportTask();
				orgUs.status = ORGUserImportTask.STATUS.START.v();
				orgUserImportTaskRepository.updateByKey(conn, "id", importTaskId, orgUs, true);

				// 把数据取出进行处理
				ORGUserImportTask orgUserTa = orgUserImportTaskRepository.getByKey(conn, "id", importTaskId);
				for (int i = 0; i < (orgUserTa.sum / 100) + 1; i++) {
					// 从导入任务数据表中取出数据
					List<ORGUserImportRecord> orgUserRec = orgUserImportRecordRepository.getListByANDKeys(conn,
							new String[] { "org_id", "task_id", "status" },
							new Object[] { orgId, importTaskId, ORGUserImportRecord.STATUS.UNDETECTED.v() }, 100, 0);

					System.out.println("");
					imp(conn, orgId, orgUserRec, importTaskId);
				}
				// 修改导入任务的导入状态为导入完成
				orgUs.status = ORGUserImportTask.STATUS.END.v();
				orgUserImportTaskRepository.updateByKey(conn, "id", importTaskId, orgUs, true);

			} catch (Exception eee) {
				eee.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			future.complete("ok");
		}, res -> {
			System.out.println("The result is: " + res.result());
		});

	}

	// 获取导入失败的组织用户
	public List<ORGUserImportRecord> getNotcompletionRecord(DruidPooledConnection conn, Long orgId, Long importTaskId,
			Integer count, Integer offset) throws Exception {
		return orgUserImportRecordRepository.getListByANDKeys(conn, new String[] { "org_id", "task_id", "status" },
				new Object[] { orgId, importTaskId, ORGUserImportRecord.STATUS.NOTCOMPLETION.v() }, count, offset);
	}

	// 创建行政机构管理员
	public void createORGAdmin(DruidPooledConnection conn, Long orgId, Byte level, String idNumber, String mobile,
			String realName) throws Exception {
		createORGUserAdmin(conn, orgId, mobile, realName, idNumber, level);
	}

	private void createORGUserAdmin(DruidPooledConnection conn, Long orgId, String mobile, String realName,
			String idNumber, Byte level) throws Exception {
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
			insertORGUserAdmin(conn, orgId, newUser.id, level);

		} else {
			// 判断ORGUser是否存在
			ORGUser existor = orgUserRepository.getByANDKeys(conn, new String[] { "org_id", "user_id" },
					new Object[] { orgId, extUser.id });
			if (null == existor) {
				// ORGUser用户不存在，直接创建

				// 写入股东信息表
				insertORGUserAdmin(conn, orgId, extUser.id, level);
			} else {
				throw new ServerException(BaseRC.ECM_ORG_USER_EXIST);
			}
		}
	}

	private void insertORGUserAdmin(DruidPooledConnection conn, Long orgId, Long userId, Byte level) throws Exception {
		ORGUser or = new ORGUser();
		or.orgId = orgId;
		or.userId = userId;
		JSONArray json = new JSONArray();
		if (level == ORG.LEVEL.COOPERATIVE.v()) {
			json.add(ORGUserRole.role_admin.roleId);
			or.roles = json.toString();
		} else if (level == ORG.LEVEL.CITY.v() || level == ORG.LEVEL.PRO.v() || level == ORG.LEVEL.DISTRICT.v()) {
			json.add(ORGUserRole.role_Administractive_admin.roleId);
			or.roles = json.toString();
		}
		orgUserRepository.insert(conn, or);
	}

	public int delORGUserAdmin(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		return orgUserRepository.deleteByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId });
	}

	public int editORGAdmin(DruidPooledConnection conn, Long orgId, Long userId, String idNumber, String mobile,
			String realName) throws Exception {

		User u = new User();
		u.idNumber = idNumber;
		u.mobile = mobile;
		u.realName = realName;
		return userRepository.updateByKey(conn, "id", userId, u, true);

	}

}
