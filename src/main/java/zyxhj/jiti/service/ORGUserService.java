package zyxhj.jiti.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
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
import zyxhj.custom.service.WxDataService;
import zyxhj.custom.service.WxFuncService;
import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.domain.Family;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGPermission;
import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserImportRecord;
import zyxhj.jiti.domain.ORGUserImportTask;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.jiti.repository.ExamineRepository;
import zyxhj.jiti.repository.ORGPermissionRelaRepository;
import zyxhj.jiti.repository.ORGUserImportRecordRepository;
import zyxhj.jiti.repository.ORGUserImportTaskRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.repository.ORGUserRoleRepository;
import zyxhj.utils.CodecUtils;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;

public class ORGUserService {

	private static Logger log = LoggerFactory.getLogger(ORGUserService.class);

	private ORGUserRepository orgUserRepository;
	private UserRepository userRepository;
	private ORGUserRoleRepository orgUserRoleRepository;
	private ORGUserImportTaskRepository orgUserImportTaskRepository;
	private ORGUserImportRecordRepository orgUserImportRecordRepository;
	private ExamineRepository examineRepository;
	private ORGService orgService;
	private ORGPermissionRelaRepository orgPermissionRelaRepository;
	private WxDataService wxDataService;
	private WxFuncService wxFuncService;
	private MessageService messageService;

	public ORGUserService() {
		try {
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			userRepository = Singleton.ins(UserRepository.class);

			orgUserImportTaskRepository = Singleton.ins(ORGUserImportTaskRepository.class);
			orgUserImportRecordRepository = Singleton.ins(ORGUserImportRecordRepository.class);
			examineRepository = Singleton.ins(ExamineRepository.class);
			orgService = Singleton.ins(ORGService.class);
			orgPermissionRelaRepository = Singleton.ins(ORGPermissionRelaRepository.class);
			wxDataService = Singleton.ins(WxDataService.class);
			wxFuncService = Singleton.ins(WxFuncService.class);
			messageService = Singleton.ins(MessageService.class);
			orgUserRoleRepository = Singleton.ins(ORGUserRoleRepository.class);
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
		List<ORGUserRole> oList = new ArrayList<>(ORGUserRole.SYS_ORG_USER_ROLE_MAP.values());

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
			String shareCerImg, Boolean shareCerHolder, Double shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags, Long familyNumber, String familyMaster) throws Exception {
		ORGUser or = new ORGUser();
		// Family fa = new Family();
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

		// if (familyNumber != null) {
		// // 查询户序号在family表里是否拥有 有则把usreid插入到户成员下 无则添加户
		//
		// Family fn = FAMILY_CACHE.getIfPresent(familyNumber);
		// if (fn == null) {
		//
		// fn = familyRepository.getByANDKeys(conn, new String[] { "org_id",
		// "family_number" },
		// new Object[] { orgId, familyNumber });
		// if (fn != null) {
		// FAMILY_CACHE.put(familyNumber.toString(), fn);
		// }
		//
		// }
		//
		// // 从缓存和数据库都取了一遍，
		// if (fn == null) {
		// // 如果空需要创建
		// // 添加户
		// fa.id = IDUtils.getSimpleId();
		// fa.orgId = orgId;
		// fa.familyNumber = familyNumber;
		// fa.familyMaster = familyMaster;
		//
		// // 将当前用户的id插入到户成员里
		// JSONArray json = new JSONArray();
		// json.add(userId);
		// fa.familyMember = json.toString();
		// familyRepository.insert(conn, fa);
		// } else {
		// // 添加到户成员下
		// JSONArray json = JSONArray.parseArray(fn.familyMember);
		// json.add(userId);
		// fa.familyMember = json.toString();
		// familyRepository.updateByKey(conn, "family_number", familyNumber, fa, true);
		// FAMILY_CACHE.put(familyNumber.toString(), fa);
		//
		// }
		//
		// }
		orgUserRepository.insert(conn, or);
	}

	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////

	// 修改字段后的方法----新建方法
	public void createORGUser(DruidPooledConnection conn, Long orgId, String mobile, String realName, String idNumber,
			Byte sex, String familyRelations, Double resourceShares, Double assetShares, Boolean isORGUser,
			String address, String shareCerNo, String shareCerImg, Boolean shareCerHolder, Double shareAmount,
			Integer weight, JSONArray roles, JSONArray groups, JSONObject tags, Long familyNumber, String familyMaster)
			throws Exception {
		User extUser = userRepository.get(conn, EXP.INS().key("id_number", idNumber));

		if (null == extUser) {
			// 用户完全不存在，则User和ORGUser记录都创建

			User newUser = new User();
			newUser.id = IDUtils.getSimpleId();
			newUser.createDate = new Date();
			newUser.realName = realName;
			newUser.sex = sex;
			newUser.mobile = mobile;
			newUser.idNumber = idNumber;

			// 默认密码,身份证后6位
			newUser.pwd = idNumber.substring(idNumber.length() - 6);

			// 创建用户
			userRepository.insert(conn, newUser);

			// 写入股东信息表
			insertORGUser(conn, orgId, newUser.id, sex, familyRelations, resourceShares, assetShares, isORGUser,
					address, shareCerNo, shareCerImg, shareCerHolder, shareAmount, weight, roles, groups, tags,
					familyNumber, familyMaster);

		} else {
			// 判断ORGUser是否存在
			ORGUser existor = orgUserRepository.get(conn, EXP.INS().key("org_id", orgId).andKey("user_id", extUser.id));

			if (null == existor) {
				// ORGUser用户不存在，直接创建

				// 写入股东信息表
				insertORGUser(conn, orgId, extUser.id, sex, familyRelations, resourceShares, assetShares, isORGUser,
						address, shareCerNo, shareCerImg, shareCerHolder, shareAmount, weight, roles, groups, tags,
						familyNumber, familyMaster);
			} else {
				// System.out
				// .println(StringUtils.join("xxxx>>orgId>", orgId, " - userId>", extUser.id, "
				// - id=", idNumber));
				throw new ServerException(BaseRC.ECM_ORG_USER_EXIST);
			}
		}
	}

	// 修改字段后的方法----新建方法
	private void insertORGUser(DruidPooledConnection conn, Long orgId, Long userId, Byte sex, String familyRelations,
			Double resourceShares, Double assetShares, Boolean isORGUser, String address, String shareCerNo,
			String shareCerImg, Boolean shareCerHolder, Double shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags, Long familyNumber, String familyMaster) throws Exception {
		ORGUser or = new ORGUser();
		// Family fa = new Family();
		or.orgId = orgId;
		or.userId = userId;

		or.address = address;
		or.shareCerNo = shareCerNo;
		or.shareCerImg = shareCerImg;
		or.shareCerHolder = shareCerHolder;

		or.shareAmount = shareAmount;

		or.familyRelations = familyRelations;
		or.resourceShares = resourceShares;
		or.assetShares = assetShares;
		or.isORGUser = isORGUser;

		or.weight = weight;

		or.roles = array2JsonString(checkRoles(roles));
		or.groups = array2JsonString(checkGroups(conn, orgId, groups));
		or.tags = obj2JsonString(tags);

		or.familyNumber = familyNumber;
		or.familyMaster = familyMaster;

		orgUserRepository.insert(conn, or);
	}

	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////
	/////////////////////////////////////////////////

	/**
	 * 创建组织用户
	 */
	public void createORGUser(DruidPooledConnection conn, Long orgId, String mobile, String realName, String idNumber,
			String address, String shareCerNo, String shareCerImg, Boolean shareCerHolder, Double shareAmount,
			Integer weight, JSONArray roles, JSONArray groups, JSONObject tags, Long familyNumber, String familyMaster)
			throws Exception {

		User extUser = userRepository.get(conn, EXP.INS().key("id_number", idNumber));

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
			ORGUser existor = orgUserRepository.get(conn, EXP.INS().key("org_id", orgId).andKey("user_id", extUser.id));

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

		return userRepository.update(conn, EXP.INS().key("id", userId), renew, true);

	}

	/**
	 * 移除组织的用户</br>
	 * 只修改ORGUser表，不删除user本身。
	 */
	public void delORGUser(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		orgUserRepository.delete(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId));

	}

	/**
	 * 修改组织的用户</br>
	 * 只修改ORGUser表，不变动user本身。
	 */
	public int editORGUser(DruidPooledConnection conn, Long orgId, Long userId, String address, String shareCerNo,
			String shareCerImg, Boolean shareCerHolder, Double shareAmount, Integer weight, JSONArray roles,
			JSONArray groups, JSONObject tags, Long familyNumber, String familyMaster) throws Exception {
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

		return orgUserRepository.update(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId), renew, true);

	}

	public ORGUser getORGUserById(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		return orgUserRepository.get(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId));
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
				String fa = ExcelUtils.getString(row.get(tt++));// 户序号

				if (StringUtils.isBlank(fa)) {
					// 户序号为空，直接跳过
					log.error("---->>户序号为空");
					continue;
				}
				Long familyNumber = Long.parseLong(fa);

				String realName = ExcelUtils.getString(row.get(tt++));
				String idNumber = ExcelUtils.getString(row.get(tt++));
				String mobile = ExcelUtils.getString(row.get(tt++));
				Double shareAmount = ExcelUtils.parseDouble(row.get(tt++));

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
		List<User> ret = userRepository.getList(conn, EXP.INS().key("mobile", mobile), count, offset);
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
		List<ORGUser> ors = orgUserRepository.getList(conn, EXP.INS().key("org_id", orgId), count, offset);

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
			List<User> us = userRepository.getList(conn, EXP.INS().IN("id", values), null, null);
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

//	// 创建组织角色导入任务
//	public void createORGUserImportTask(DruidPooledConnection conn, Long orgId, Long userId, String name)
//			throws Exception {
//		ORGUserImportTask orgUserImport = new ORGUserImportTask();
//		orgUserImport.id = IDUtils.getSimpleId();
//		orgUserImport.orgId = orgId;
//		orgUserImport.userId = userId;
//		orgUserImport.name = name;
//		orgUserImport.createTime = new Date();
//		orgUserImport.sum = 0;
//		orgUserImport.completion = 0;
//		orgUserImport.notCompletion = 0;
//		orgUserImport.success = 0;
//		orgUserImport.status = AssetImportTask.STATUS.WAIT.v();
//		orgUserImportTaskRepository.insert(conn, orgUserImport);
//	}
//
//	// 查询组织用户导入任务
//	public List<ORGUserImportTask> getORGUserImportTasks(DruidPooledConnection conn, Long orgId, Long userId,
//			Integer count, Integer offset) throws Exception {
//		return orgUserImportTaskRepository.getORGUserImportTasks(conn, orgId, userId, count, offset);
//	}
//
//	// 查询组织用户导入任务信息
//	public ORGUserImportTask getORGUserImportTask(DruidPooledConnection conn, Long importTaskId, Long orgId,
//			Long userId) throws Exception {
//		return orgUserImportTaskRepository.get(conn, EXP.INS().key("id", importTaskId).andKey("org_id", orgId).andKey("user_id", userId));
//	}
//
//	public void importORGUserRecord(DruidPooledConnection conn, Long orgId, Long userId, String url, Long importTaskId)
//			throws Exception {
//		Integer sum = 0;
//		Integer err = 0;
//		JSONArray json = JSONArray.parseArray(url);
//		for (int o = 0; o < json.size(); o++) {
//			List<ORGUserImportRecord> list = new ArrayList<ORGUserImportRecord>();
//			// 1行表头，17列，文件格式写死的
//			List<List<Object>> table = ExcelUtils.readExcelOnline(json.getString(o), 1, 17, 0);
//			for (List<Object> row : table) {
//				ORGUserImportRecord orgUserImportRecord = new ORGUserImportRecord();
//				try {
//					int tt = 0;
//					String familyNumber = ExcelUtils.getString(row.get(tt++));// 户序号
//
//					if (StringUtils.isBlank(familyNumber)) {
//						// 户序号为空，直接跳过
//						log.error("---->>户序号为空");
//						continue;
//					}
//
//					orgUserImportRecord.familyNumber = Long.parseLong(familyNumber);
//					orgUserImportRecord.id = IDUtils.getSimpleId();
//					orgUserImportRecord.orgId = orgId;
//					orgUserImportRecord.userId = userId;
//					orgUserImportRecord.taskId = importTaskId;
//					orgUserImportRecord.status = ORGUserImportRecord.STATUS.UNDETECTED.v();
//					orgUserImportRecord.realName = ExcelUtils.getString(row.get(tt++));
//					orgUserImportRecord.idNumber = ExcelUtils.getString(row.get(tt++));
//					orgUserImportRecord.mobile = ExcelUtils.getString(row.get(tt++));
//					orgUserImportRecord.shareAmount = ExcelUtils.parseDouble(row.get(tt++));
//
//					orgUserImportRecord.weight = ExcelUtils.parseInt(row.get(tt++));
//					orgUserImportRecord.address = ExcelUtils.getString(row.get(tt++));
//					orgUserImportRecord.familyMaster = ExcelUtils.getString(row.get(tt++));
//					orgUserImportRecord.shareCerHolder = ExcelUtils.parseShiFou(row.get(tt++));
//					orgUserImportRecord.shareCerNo = ExcelUtils.getString(row.get(tt++));
//
//					String dutyShareholders = ExcelUtils.getString(row.get(tt++));
//					String dutyDirectors = ExcelUtils.getString(row.get(tt++));
//					String dutyVisors = ExcelUtils.getString(row.get(tt++));
//					String dutyOthers = ExcelUtils.getString(row.get(tt++));
//					String dutyAdmins = ExcelUtils.getString(row.get(tt++));
//
//					orgUserImportRecord.groups = ExcelUtils.getString(row.get(tt++));
//					orgUserImportRecord.tags = ExcelUtils.getString(row.get(tt++));
//
//					// 合并roles
//					JSONArray roles = new JSONArray();
//					JSONArray temp = null;
//					{
//						// 股东成员职务
//						String ts = StringUtils.trim(dutyShareholders);
//						if (ts.equals(ORGUserRole.role_shareHolder.name)) {
//							roles.add(ORGUserRole.role_shareHolder.roleId);// 股东
//						} else if (ts.equals(ORGUserRole.role_shareDeputy.name)) {
//							roles.add(ORGUserRole.role_shareDeputy.roleId);// 股东代表
//						} else if (ts.equals(ORGUserRole.role_shareFamily.name)) {
//							roles.add(ORGUserRole.role_shareFamily.roleId);// 股东户代表
//						} else {
//							// 无，不加
//						}
//					}
//
//					{
//						// 董事会职务
//						String ts = StringUtils.trim(dutyDirectors);
//						if (ts.equals(ORGUserRole.role_director.name)) {
//							roles.add(ORGUserRole.role_director.roleId);// 董事
//						} else if (ts.equals(ORGUserRole.role_dirChief.name)) {
//							roles.add(ORGUserRole.role_dirChief.roleId);// 董事长
//						} else if (ts.equals(ORGUserRole.role_dirVice.name)) {
//							roles.add(ORGUserRole.role_dirVice.roleId);// 副董事长
//						} else {
//							// 无，不加
//						}
//					}
//
//					{
//						// 监事会职务
//						String ts = StringUtils.trim(dutyVisors);
//						if (ts.equals(ORGUserRole.role_supervisor.name)) {
//							roles.add(ORGUserRole.role_supervisor.roleId);// 监事
//						} else if (ts.equals(ORGUserRole.role_supChief.name)) {
//							roles.add(ORGUserRole.role_supChief.roleId);// 监事长
//						} else if (ts.equals(ORGUserRole.role_supVice.name)) {
//							roles.add(ORGUserRole.role_supVice.roleId);// 副监事长
//						} else {
//							// 无，不加
//						}
//					}
//
//					{
//						// 其它管理角色
//
//						temp = CodecUtils.convertCommaStringList2JSONArray(dutyAdmins);
//						for (int i = 0; i < temp.size(); i++) {
//							String ts = StringUtils.trim(temp.getString(i));
//
//							if (ts.equals(ORGUserRole.role_user.name)) {
//								roles.add(ORGUserRole.role_user.roleId);// 用户
//							} else if (ts.equals(ORGUserRole.role_outuser.name)) {
//								roles.add(ORGUserRole.role_outuser.roleId);// 外部人员
//							} else if (ts.equals(ORGUserRole.role_admin.name)) {
//								roles.add(ORGUserRole.role_admin.roleId);// 管理员
//							} else {
//								// 无，不填默认当作用户
//								roles.add(ORGUserRole.role_user.roleId);// 用户
//							}
//						}
//					}
//
//					{
//
//						// TODO 这个地方要跟系统中的其它角色做匹配
//						// 其它角色
//						temp = CodecUtils.convertCommaStringList2JSONArray(dutyOthers);
//
//						// for (int i = 0; i < temp.size(); i++) {
//						// String ts = StringUtils.trim(temp.getString(i));
//						// if (ts.equals("null") || ts.equals("无")) {
//						// // 无和null，不加
//						// } else {
//						// roles.add(ts);
//						// }
//						// }
//					}
//
//					orgUserImportRecord.roles = roles.toString();
//
//					// 开始处理分组和标签
//
//					JSONArray arrGroups = new JSONArray();
//
//					{
//						// 分组
//						temp = CodecUtils.convertCommaStringList2JSONArray(orgUserImportRecord.groups);
//
//						for (int i = 0; i < temp.size(); i++) {
//							String ts = StringUtils.trim(temp.getString(i));
//							if (ts.equals("null") || ts.equals("无")) {
//								// 无和null，不加
//							} else {
//								arrGroups.add(ts);
//							}
//						}
//						orgUserImportRecord.groups = arrGroups.toJSONString();
//					}
//
//					JSONObject joTags = new JSONObject();
//					JSONArray arrTags = new JSONArray();
//					{
//						// 标签
//						temp = CodecUtils.convertCommaStringList2JSONArray(orgUserImportRecord.tags);
//
//						for (int i = 0; i < temp.size(); i++) {
//							String ts = StringUtils.trim(temp.getString(i));
//							if (ts.equals("null") || ts.equals("无")) {
//								// 无和null，不加
//							} else {
//								arrTags.add(ts);
//							}
//						}
//						if (arrTags.size() > 0) {
//							joTags.put("tags", arrTags);
//						}
//						orgUserImportRecord.tags = joTags.toJSONString();
//					}
//
//					// 处理idNunber为空的问题
//					if (StringUtils.isBlank(orgUserImportRecord.idNumber)) {
//						orgUserImportRecord.idNumber = StringUtils.join(orgId, "-", orgUserImportRecord.familyNumber,
//								"-", IDUtils.getHexSimpleId());
//					}
//					if (StringUtils.isBlank(orgUserImportRecord.mobile)) {
//						orgUserImportRecord.mobile = StringUtils.join(orgId, "-", orgUserImportRecord.familyNumber, "-",
//								IDUtils.getHexSimpleId());
//					}
//					sum++;
//					list.add(orgUserImportRecord);
//
//					if (list.size() == 10) {
//						orgUserImportRecordRepository.insertList(conn, list);
//						list = new ArrayList<ORGUserImportRecord>();
//					}
//					if (sum == table.size()) {
//						orgUserImportRecordRepository.insertList(conn, list);
//					}
//					Thread.sleep(5L);
//				} catch (Exception e) {
//					err++;
//					log.error(e.getMessage());
//				}
//			}
//		}
//		// 添加总数到任务表中
//		orgUserImportTaskRepository.countImportTaskSum(conn, importTaskId, sum - err);
//	}
//
//	// 组织用户回调页面
//	public List<ORGUserImportRecord> getORGUserImportRecords(DruidPooledConnection conn, Long orgId, Long importTaskId,
//			Integer count, Integer offset) throws Exception {
//		return orgUserImportRecordRepository.getList(conn,EXP.INS().key("org_id", orgId).andKey("task_id", importTaskId), count, offset);
//		
//	}
//
//	private void imp(DruidPooledConnection conn, Long orgId, List<ORGUserImportRecord> orgUserRec, Long importTaskId)
//			throws Exception {
//		for (ORGUserImportRecord orgUserImportRecord : orgUserRec) {
//			String mobile = orgUserImportRecord.mobile;
//			String realName = orgUserImportRecord.realName;
//			String idNumber = orgUserImportRecord.idNumber;
//			String address = orgUserImportRecord.address;
//			String shareCerNo = orgUserImportRecord.shareCerNo;
//			Boolean shareCerHolder = orgUserImportRecord.shareCerHolder;
//			Double shareAmount = orgUserImportRecord.shareAmount;
//			Integer weight = orgUserImportRecord.weight;
//			JSONArray roles = JSONArray.parseArray(orgUserImportRecord.roles);
//			JSONArray groups = JSONArray.parseArray(orgUserImportRecord.groups);
//			JSONObject tags = JSONObject.parseObject(orgUserImportRecord.tags);
//			Long familyNumber = orgUserImportRecord.familyNumber;
//			String familyMaster = orgUserImportRecord.familyMaster;
//
//			try {
//				createORGUser(conn, orgId, mobile, realName, idNumber, address, shareCerNo, "", shareCerHolder,
//						shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
//				orgUserImportRecord.status = ORGUserImportRecord.STATUS.COMPLETION.v();
//				// 修改导入数据状态为通过
//				orgUserImportRecordRepository.updateStatus(conn, orgUserImportRecord.id, orgUserImportRecord.status);
//				// 导入任务 成功数+1
//				orgUserImportTaskRepository.countORGUserImportCompletionTask(conn, importTaskId);
//			} catch (Exception e) {
//				orgUserImportRecord.status = ORGUserImportRecord.STATUS.NOTCOMPLETION.v();
//				// 修改导入数据状态为失败
//				orgUserImportRecordRepository.updateStatus(conn, orgUserImportRecord.id, orgUserImportRecord.status);
//				// 导入任务 失败数+1
//				orgUserImportTaskRepository.countORGUserImportNotCompletionTask(conn, importTaskId);
//			}
//		}
//	}
//
//	// 开始导入数据
//	public void importORGUser(Long orgId, Long importTaskId) throws Exception {
//
//		// 异步方法，不会阻塞
//		Vertx.vertx().executeBlocking(future -> {
//			// 下面这行代码可能花费很长时间
//			DruidDataSource dds;
//			DruidPooledConnection conn = null;
//			try {
//				dds = DataSource.getDruidDataSource("rdsDefault.prop");
//				conn = (DruidPooledConnection) dds.getConnection();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			try {
//				// 修改导入任务状态为正在导入
//				ORGUserImportTask orgUs = new ORGUserImportTask();
//				orgUs.status = ORGUserImportTask.STATUS.START.v();
//				orgUserImportTaskRepository.update(conn,EXP.INS().key("id", importTaskId), orgUs, true);
//				
//
//				// 把数据取出进行处理
//				ORGUserImportTask orgUserTa = orgUserImportTaskRepository.get(conn, EXP.INS().key( "id", importTaskId));
//				for (int i = 0; i < (orgUserTa.sum / 100) + 1; i++) {
//					// 从导入任务数据表中取出数据
//					List<ORGUserImportRecord> orgUserRec = orgUserImportRecordRepository.getList(conn,
//							EXP.INS().key("org_id", orgId).andKey("task_id", importTaskId).andKey("status",  ORGUserImportRecord.STATUS.UNDETECTED.v()), 100, 0);
//
//					System.out.println("");
//					imp(conn, orgId, orgUserRec, importTaskId);
//				}
//				// 修改导入任务的导入状态为导入完成
//				orgUs.status = ORGUserImportTask.STATUS.END.v();
//				orgUserImportTaskRepository.update(conn,EXP.INS().key("id", importTaskId), orgUs, true);
//
//			} catch (Exception eee) {
//				eee.printStackTrace();
//			} finally {
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//			}
//			future.complete("ok");
//		}, res -> {
//			System.out.println("The result is: " + res.result());
//		});
//
//	}
//
//	// 获取导入失败的组织用户
//	public List<ORGUserImportRecord> getNotcompletionRecord(DruidPooledConnection conn, Long orgId, Long importTaskId,
//			Integer count, Integer offset) throws Exception {
//		return orgUserImportRecordRepository.getList(conn,EXP.INS().key("org_id", orgId).andKey("task_id", importTaskId).andKey("status", ORGUserImportRecord.STATUS.NOTCOMPLETION.v()), count, offset);
//	}

	// 创建行政机构管理员
	public void createORGAdmin(DruidPooledConnection conn, Long orgId, Byte level, String idNumber, String mobile,
			String realName) throws Exception {
		createORGUserAdmin(conn, orgId, mobile, realName, idNumber, level);
	}

	// 创建组织用户管理员
	private void createORGUserAdmin(DruidPooledConnection conn, Long orgId, String mobile, String realName,
			String idNumber, Byte level) throws Exception {
		User extUser = userRepository.get(conn, EXP.INS().key("id_number", idNumber));
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
			ORGUser existor = orgUserRepository.get(conn, EXP.INS().key("org_id", orgId).andKey("user_id", extUser.id));
			if (null == existor) {
				// ORGUser用户不存在，直接创建

				// 写入股东信息表
				insertORGUserAdmin(conn, orgId, extUser.id, level);
			} else {
				throw new ServerException(BaseRC.ECM_ORG_USER_EXIST);
			}
		}
	}

	// 添加组织管理员
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

	// 删除组织管理员
	public int delORGUserAdmin(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		return orgUserRepository.delete(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId));
	}

	// 修改组织管理员
	public int editORGAdmin(DruidPooledConnection conn, Long orgId, Long userId, String idNumber, String mobile,
			String realName) throws Exception {

		User u = new User();
		u.idNumber = idNumber;
		u.mobile = mobile;
		u.realName = realName;
		return userRepository.update(conn, EXP.INS().key("id", userId), u, true);

	}

	// 所有户列表
	public JSONArray getFamilyAll(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		return orgUserRepository.getFamilyAll(conn, orgId, count, offset);
	}

	// 模糊查询户主名
	public List<ORGUser> getFamilyByFamilyMaster(DruidPooledConnection conn, Long orgId, String master, Integer count,
			Integer offset) throws Exception {
		return orgUserRepository.getFamilyByFamilyMaster(conn, orgId, master, count, offset);
	}

	// 根据户序号查询
	public List<ORGUser> getFamilyByFamilyNumber(DruidPooledConnection conn, Long orgId, Long number, Integer count,
			Integer offset) throws Exception {
		return orgUserRepository.getFamilyByFamilyNumber(conn, orgId, number, count, offset);
	}

	// 根据股权证号查询户
	public List<ORGUser> getFamilyByshare(DruidPooledConnection conn, Long orgId, String share, Integer count,
			Integer offset) throws Exception {
		return orgUserRepository.getFamilyByshare(conn, orgId, share, count, offset);
	}

	// 根据户序号查询户下所有人
	public JSONArray getFamilyUserByFamilyNumber(DruidPooledConnection conn, Long orgId, Long familyNumber)
			throws Exception {
		return orgUserRepository.getFamilyUserBYFamilyId(conn, orgId, familyNumber);
	}

	// 创建户审核任务
	public Examine createExamine(DruidPooledConnection conn, Long orgId, String data, Byte type, String remark)
			throws Exception {
		Examine examine = new Examine();
		examine.id = IDUtils.getSimpleId();
		examine.orgId = orgId;
		examine.data = data;
		examine.createDate = new Date();
		examine.examineDate = new Date();
		examine.type = type;
		if (type == Examine.TYPE.FAMILY.v()) {
			ORGPermissionRel orgPer = orgPermissionRelaRepository.get(conn,
					EXP.INS().andKey("org_id", orgId).andKey("permission_id", ORGPermission.per_feparate_family.id));
			if (orgPer != null) {
				examine.status = Examine.STATUS.NOEXAMINE.v();
				// 给审核人员发送通知
				examineMessage(conn, orgId, examine, ORGPermission.per_feparate_family.id);

				// 平台内消息通知
				message(conn, data, ORGPermission.per_feparate_family.name, examine.status);

			} else {
				examine.status = Examine.STATUS.ORGEXAMINE.v();
				message(conn, data, ORGPermission.per_feparate_family.name, examine.status);
			}

		} else if (type == Examine.TYPE.SHARE.v()) {
			ORGPermissionRel orgPer = orgPermissionRelaRepository.get(conn,
					EXP.INS().key("org_id", orgId).andKey("permission_id", ORGPermission.per_share_change.id));
			if (orgPer != null) {
				examine.status = Examine.STATUS.NOEXAMINE.v();
				// 给审核人员发送通知
				examineMessage(conn, orgId, examine, ORGPermission.per_share_change.id);

				shareMessage(conn, data, ORGPermission.per_share_change.name, examine.status);
			} else {
				examine.status = Examine.STATUS.ORGEXAMINE.v();
				shareMessage(conn, data, ORGPermission.per_share_change.name, examine.status);
			}
		} else if (type == Examine.TYPE.ORG.v()) {
			examine.status = Examine.STATUS.NOEXAMINE.v();
		}

		examine.remark = remark;
		examineRepository.insert(conn, examine);

		return examine;

	}

	private void shareMessage(DruidPooledConnection conn, String data, String perName, Byte examineStatus)
			throws Exception {
		JSONObject jo = JSONObject.parseObject(data);
		JSONArray oldDatas = jo.getJSONArray("oldData");
		for (int i = 0; i < oldDatas.size(); i++) {
			JSONObject userInfo = oldDatas.getJSONObject(i);
			JSONObject user = userInfo.getJSONObject("user");
			Long userId = user.getLong("id");
			JSONObject orgUser = userInfo.getJSONObject("orgUser");
			Long orgId = orgUser.getLong("orgId");
			messageService.createExamineMessage(conn, orgId, userId, perName, data, examineStatus);
		}
	}

	private void message(DruidPooledConnection conn, String data, String perName, Byte examineStatus) throws Exception {

		JSONObject jo = JSONObject.parseObject(data);
		JSONObject ext = jo.getJSONObject("ext");
		Byte familyOperate = ext.getByte("familyOperate");
		JSONArray oldDatas = jo.getJSONArray("oldData");
		if (familyOperate == Examine.OPERATE.MOVEFAMILYUSER.v()) {
			for (int i = 0; i < oldDatas.size(); i++) {
				JSONArray oldData = oldDatas.getJSONArray(i);
				messageService.createExamineMessages(conn, oldData, perName, data, examineStatus);
			}
		} else if (familyOperate == Examine.OPERATE.ADDFAMILY.v()) {

		} else {
			messageService.createExamineMessages(conn, oldDatas, perName, data, examineStatus);
		}

	}

	// 发送微信通知
	private void examineMessage(DruidPooledConnection conn, Long orgId, Examine examine, Long permissionId)
			throws Exception {
		JSONObject jo = JSONObject.parseObject(examine.data);
		JSONArray json = new JSONArray();
		String type = "";
		String familyMaster = "";
		// 获取org信息
		ORG or = orgService.getORGById(conn, orgId);
		if (examine.status == Examine.STATUS.NOEXAMINE.v()) {
			List<ORGPermissionRel> orgPermission = orgPermissionRelaRepository.getList(conn,
					EXP.INS().key("org_id", orgId).andKey("permission_id", permissionId), 64, 0);
			for (ORGPermissionRel orgPermissionRel : orgPermission) {
				json.add(orgPermissionRel.roleId);
			}

			JSONArray user = orgUserRepository.getUserByRoles(conn, orgId, json);
			for (int i = 0; i < user.size(); i++) {
				JSONObject userInfo = user.getJSONObject(i);
				String openId = userInfo.getString("wxOpenId");
				if (openId == null) {
					continue;
				}
				if (permissionId == ORGPermission.per_feparate_family.id) {
					JSONObject ext = jo.getJSONObject("ext");
					Byte familyOperate = ext.getByte("familyOperate");
					if (familyOperate == Examine.OPERATE.ADDFAMILY.v()) {
						type = Examine.OPERATE.ADDFAMILY.txt();
						JSONArray newDatas = jo.getJSONArray("newData");
						JSONArray newData = JSONArray.parseArray(newDatas.getString(0));
						JSONObject familyInfo = newData.getJSONObject(0);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.HOUSEHOLD.v()) {
						type = Examine.OPERATE.HOUSEHOLD.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = data.getJSONObject(i);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.ADDFAMILYUSER.v()) {
						type = Examine.OPERATE.ADDFAMILYUSER.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = data.getJSONObject(i);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.DELFAMILYUSER.v()) {
						type = Examine.OPERATE.DELFAMILYUSER.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = data.getJSONObject(i);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.MOVEFAMILYUSER.v()) {
						type = Examine.OPERATE.MOVEFAMILYUSER.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = (data.getJSONArray(0)).getJSONObject(i);

						familyMaster = familyInfo.getString("familyMaster");
					}
				} else if (permissionId == ORGPermission.per_share_change.id) {
					type = Examine.OPERATE.UPSHARE.txt();
					JSONArray newDatas = jo.getJSONArray("newData");
					JSONObject newData = newDatas.getJSONObject(0);
					JSONObject orgUserInfo = newData.getJSONObject("user");
					familyMaster = orgUserInfo.getString("realName");
				}
				wxFuncService.examineMessage(wxDataService.getWxMpService(), openId, or.name, familyMaster, type,
						examine.createDate);
			}
			messageService.createExamineMessages(conn, user, type, examine.data, examine.status);
		}

	}

	// 根据审核类型，审核状态查询某个组织的审核
	public List<Examine> getExamine(DruidPooledConnection conn, Long orgId, Byte type, Byte status, Integer count,
			Integer offset) throws Exception {
		return examineRepository.getList(conn,
				EXP.INS().key("org_id", orgId).andKey("type", type).andKey("status", status), count, offset);
	}

	// 根据用户审核权限查看审核 用户只能看到自己的审核 拥有审核权限的查看所有审核
	public List<Examine> getExamineByPer(DruidPooledConnection conn, Long orgId, Long userId, String permissionIds,
			Byte type, Byte status, Integer count, Integer offset) throws Exception {
		// ORGPermissionRel permissionRela =
		// orgPermissionRelaRepository.getPermissionRela(conn, orgId, roles,
		// ORGPermission.per_examine.id);
		JSONArray json = JSONArray.parseArray(permissionIds);
		boolean check = false;
		for (int i = 0; i < json.size(); i++) {
			if (type == Examine.TYPE.FAMILY.v()) {
				if (json.getLong(i) == ORGPermission.per_feparate_family.id) {
					check = true;
				}
			} else if (type == Examine.TYPE.SHARE.v()) {
				if (json.getLong(i) == ORGPermission.per_share_change.id) {
					check = true;
				}
			}
		}
		if (check) {
			// 有权限 获取审核列表
			if (status == null) {
				status = 0;
			}
			return examineRepository.getList(conn,
					EXP.INS().key("org_id", orgId).andKey("type", type).andKey("status", status), count, offset);
		} else {
			// 用户查看自己的审核 TODO 有问题
			return examineRepository.getExamineLikeUserId(conn, orgId, userId, count, offset);
		}
	}

	// 修改审核
	public Examine editExamine(DruidPooledConnection conn, Long examineId, Long orgId, Byte status) throws Exception {
		Examine examine = examineRepository.get(conn, EXP.INS().key("id", examineId));
		Examine ex = new Examine();
		if (status == Examine.STATUS.ORGEXAMINE.v()) {
			// 组织审核
			ex.examineDate = new Date();
			ex.status = Examine.STATUS.ORGEXAMINE.v();
			examineRepository.update(conn, EXP.INS().key("id", examineId).andKey("org_id", orgId), ex, true);

			message(conn, examine.data, ORGPermission.per_feparate_family.name, ex.status);
			return ex;
		} else if (status == Examine.STATUS.DISEXAMINE.v() || status == Examine.STATUS.WAITEC.v()) {

			// TODO 目前没加事务，有隐患

			// 开始审核
			examine(conn, examineId, orgId);

			// 区级审核
			ex.examineDate = new Date();
			ex.status = status;
			examineRepository.update(conn, EXP.INS().key("id", examineId).andKey("org_id", orgId), ex, true);
			message(conn, examine.data, ORGPermission.per_feparate_family.name, ex.status);
			return ex;
		} else {
			// 审核失败
			ex.examineDate = new Date();
			ex.status = Examine.STATUS.FAIL.v();
			examineRepository.update(conn, EXP.INS().key("id", examineId).andKey("org_id", orgId), ex, true);
			message(conn, examine.data, ORGPermission.per_feparate_family.name, ex.status);
			return ex;
		}
	}

	// 审核 添加户/分户/新增户成员/删除户成员/移户操作
	private JSONArray examine(DruidPooledConnection conn, Long examineId, Long orgId) throws Exception {
		// 先从数据库拿出审核数据
		Examine ex = examineRepository.get(conn, EXP.INS().key("id", examineId));
		JSONObject jsonObj = JSONObject.parseObject(ex.data); // 转换为JSONObject数组
		JSONObject ext = jsonObj.getJSONObject("ext");
		Byte familyOperate = ext.getByte("familyOperate");

		// 根据操作执行对应的方法
		if (familyOperate == Examine.OPERATE.ADDFAMILY.v()) {
			// 执行添加户操作
			return createFamily(conn, jsonObj, orgId, ex.id);
		} else if (familyOperate == Examine.OPERATE.HOUSEHOLD.v()) {
			// 执行分户操作
			return household(conn, jsonObj, orgId, ex.id);
		} else if (familyOperate == Examine.OPERATE.ADDFAMILYUSER.v()) {
			// 执行添加户成员操作
			editfamilyuser(conn, jsonObj, orgId);
			return null;
		} else if (familyOperate == Examine.OPERATE.DELFAMILYUSER.v()) {
			// 执行移除户成员操作
			editfamilyuser(conn, jsonObj, orgId);
			return null;
		} else if (familyOperate == Examine.OPERATE.MOVEFAMILYUSER.v()) {
			// 执行移户操作
			movefamilyuser(conn, jsonObj, orgId);
			return null;
		} else {
			return null;
		}
	}

	// 移户操作
	private void movefamilyuser(DruidPooledConnection conn, JSONObject jsonObj, Long orgId) throws Exception {
		JSONObject ext = jsonObj.getJSONObject("ext");
		String editHouseholder = ext.getString("editHouseholder");
		// 移户 一边增加人员一边删除人员 先去遍历删除人员 再去执行添加人员
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		// 先遍历删除
		for (int i = 0; i < newDatas.size(); i++) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
			for (int j = 0; j < newData.size(); j++) {
				JSONObject jo = newData.getJSONObject(j);
				// 获取到删除的标记
				Byte userTab = jo.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.REMOVE.v()) {
					// 移除户成员
					Long or = jo.getLong("orgId");
					Long userId = jo.getLong("userId");
					orgUserRepository.delete(conn, EXP.INS().key("org_id", or).andKey("user_id", userId));
				} else {
					continue;
				}
			}
		}

		// 遍历新增
		for (int i = 0; i < newDatas.size(); i++) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
			for (int j = 0; j < newData.size(); j++) {
				JSONObject jo = newData.getJSONObject(j);
				// 获取到新增的标记
				Byte userTab = jo.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.ADD.v()) {
					Long or = jo.getLong("orgId");
					String mobile = jo.getString("mobile");
					String realName = jo.getString("realName");
					String idNumber = jo.getString("idNumber");
					String address = jo.getString("address");
					String shareCerNo = jo.getString("shareCerNo");
					String shareCerImg = jo.getString("shareCerImg");
					Boolean shareCerHolder = jo.getBoolean("shareCerHolder");
					Double shareAmount = jo.getDouble("shareAmount");
					Integer weight = jo.getInteger("weight");
					JSONArray roles = jo.getJSONArray("roles");
					JSONArray groups = jo.getJSONArray("groups");
					Long familyNumber = jo.getLong("familyNumber");
					String familyMaster = jo.getString("familyMaster");
					JSONObject tags = jo.getJSONObject("tags");
					createORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo, shareCerImg,
							shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
				} else {
					continue;
				}
			}
		}

		// editHouseholder修改户主不为空 则表示需要修改户主
		if (StringUtils.isNotBlank(editHouseholder)) {
			for (int i = 0; i < newDatas.size(); i++) {
				// 获取到新数据里每一个户
				JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
				JSONObject jo = newData.getJSONObject(0);
				Long or = jo.getLong("orgId");
				Long familyNumber = jo.getLong("familyNumber");
				String familyMaster = jo.getString("familyMaster");
				ORGUser orgUser = new ORGUser();
				orgUser.familyMaster = familyMaster;
				orgUserRepository.update(conn, EXP.INS().key("org_id", or).andKey("family_number", familyNumber),
						orgUser, true);

			}
		}

	}

	// 修改户成员操纵 添加/删除
	private void editfamilyuser(DruidPooledConnection conn, JSONObject jsonObj, Long orgId) throws Exception {
		JSONObject ext = jsonObj.getJSONObject("ext");
		String editHouseholder = ext.getString("editHouseholder");
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		for (int i = 0; i < newDatas.size(); i++) {
			JSONArray newData = newDatas.getJSONArray(i);
			for (int j = 0; j < newData.size(); j++) {
				JSONObject json = newData.getJSONObject(j);
				// 取到用户标记
				Byte userTab = json.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.ADD.v()) {
					Long or = json.getLong("orgId");
					String mobile = json.getString("mobile");
					String realName = json.getString("realName");
					String idNumber = json.getString("idNumber");
					String address = json.getString("address");
					String shareCerNo = json.getString("shareCerNo");
					String shareCerImg = json.getString("shareCerImg");
					Boolean shareCerHolder = json.getBoolean("shareCerHolder");
					Double shareAmount = json.getDouble("shareAmount");
					Integer weight = json.getInteger("weight");
					JSONArray roles = json.getJSONArray("roles");
					JSONArray groups = json.getJSONArray("groups");
					Long familyNumber = json.getLong("familyNumber");
					String familyMaster = json.getString("familyMaster");
					JSONObject tags = json.getJSONObject("tags");
					createORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo, shareCerImg,
							shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
				} else if (userTab != null && userTab == Examine.TAB.REMOVE.v()) {
					// 移除户成员
					Long or = json.getLong("orgId");
					Long userId = json.getLong("userId");
					orgUserRepository.delete(conn, EXP.INS().key("org_id", or).andKey("user_id", userId));
				} else {
					continue;
				}
			}
		}

		if (StringUtils.isNotBlank(editHouseholder)) {
			JSONArray js = newDatas.getJSONArray(0);
			// 获取到新数据里每一个户
			JSONObject jo = js.getJSONObject(0);
			Long or = jo.getLong("orgId");
			Long familyNumber = jo.getLong("familyNumber");
			String familyMaster = jo.getString("familyMaster");
			ORGUser orgUser = new ORGUser();
			orgUser.familyMaster = familyMaster;
			orgUserRepository.update(conn, EXP.INS().key("org_id", or).andKey("family_number", familyNumber), orgUser,
					true);
		}

	}

	// 分户
	private JSONArray household(DruidPooledConnection conn, JSONObject jsonObj, Long orgId, Long examineId)
			throws Exception {
		JSONObject ext = jsonObj.getJSONObject("ext");
		String editHouseholder = ext.getString("editHouseholder");
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		JSONArray editData = new JSONArray();
		// 遍历分户新数据
		for (int i = 0; i < newDatas.size(); i++) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
			JSONArray js = new JSONArray();
			// 计算户序号最大值
			Long max = 0L;
			String master = "";
			// 遍历第一户的数据
			for (int j = 0; j < newData.size(); j++) {
				JSONObject jo = newData.getJSONObject(j);
				// 先判断此用户标记 0 为移除户成员 1为新增的户成员
				Byte userTab = jo.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.ADD.v()) {
					// 添加户成员
					Long or = jo.getLong("orgId");
					String mobile = jo.getString("mobile");
					String realName = jo.getString("realName");
					String idNumber = jo.getString("idNumber");
					String address = jo.getString("address");
					String shareCerNo = jo.getString("shareCerNo");
					String shareCerImg = jo.getString("shareCerImg");
					Boolean shareCerHolder = jo.getBoolean("shareCerHolder");
					Double shareAmount = jo.getDouble("shareAmount");
					Integer weight = jo.getInteger("weight");
					JSONArray roles = jo.getJSONArray("roles");
					JSONArray groups = jo.getJSONArray("groups");
					Long familyNumber = jo.getLong("familyNumber");
					String familyMaster = jo.getString("familyMaster");
					if (familyNumber == null) {
						if (familyMaster.equals(master)) {
							familyNumber = max;
						} else {
							master = familyMaster;
							ORGUser maxFam = orgUserRepository.maxFamilyNumber(conn, orgId);
							max = maxFam.familyNumber + 1;
							familyNumber = max;
						}
					}
					JSONObject tags = jo.getJSONObject("tags");
					createORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo, shareCerImg,
							shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
					jo.put("familyNumber", familyNumber);
					js.add(jo);
				} else if (userTab != null && userTab == Examine.TAB.REMOVE.v()) {
					// 移除户成员
					Long or = jo.getLong("orgId");
					Long userId = jo.getLong("userId");
					orgUserRepository.delete(conn, EXP.INS().key("org_id", or).andKey("user_id", userId));
					js.add(jo);
				} else {
					js.add(jo);
					continue;
				}

			}
			editData.add(js);
		}
		jsonObj.put("newData", editData);
		Examine ex = new Examine();
		ex.data = jsonObj.toJSONString();
		examineRepository.update(conn, EXP.INS().key("id", examineId), ex, true);

		// editHouseholder修改户主不为空 则表示需要修改户主
		if (StringUtils.isNotBlank(editHouseholder)) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(0));
			JSONObject jo = newData.getJSONObject(0);
			Long or = jo.getLong("orgId");
			Long familyNumber = jo.getLong("familyNumber");
			String familyMaster = jo.getString("familyMaster");
			ORGUser orgUser = new ORGUser();
			orgUser.familyMaster = familyMaster;
			orgUserRepository.update(conn, EXP.INS().key("org_id", or).andKey("family_number", familyNumber), orgUser,
					true);
		}
		return editData;

	}

	// 新增户
	private JSONArray createFamily(DruidPooledConnection conn, JSONObject jsonObj, Long orgId, Long examineId)
			throws Exception {
		// 获取组织户序号 并加上1
		ORGUser faNum = orgUserRepository.maxFamilyNumber(conn, orgId);
		Long maxNum = faNum.familyNumber + 1;// TODO 线程不安全，目前不解决
		JSONArray addNewData = new JSONArray();
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		for (int i = 0; i < newDatas.size(); i++) {
			JSONArray newData = newDatas.getJSONArray(i);
			JSONArray addFamily = new JSONArray();
			for (int j = 0; j < newData.size(); j++) {
				JSONObject json = newData.getJSONObject(j);
				Long or = json.getLong("orgId");
				String mobile = json.getString("mobile");
				String realName = json.getString("realName");
				String idNumber = json.getString("idNumber");
				String address = json.getString("address");
				String shareCerNo = json.getString("shareCerNo");
				String shareCerImg = json.getString("shareCerImg");
				Boolean shareCerHolder = json.getBoolean("shareCerHolder");
				Double shareAmount = json.getDouble("shareAmount");
				Integer weight = json.getInteger("weight");
				JSONArray roles = json.getJSONArray("roles");
				JSONArray groups = json.getJSONArray("groups");
				Long familyNumber = maxNum;
				String familyMaster = json.getString("familyMaster");
				JSONObject tags = json.getJSONObject("tags");
				json.put("familyNumber", maxNum);
				createORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo, shareCerImg, shareCerHolder,
						shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
				addFamily.add(json);
			}
			addNewData.add(addFamily);
		}
		jsonObj.put("newData", addNewData);
		Examine ex = new Examine();
		ex.data = jsonObj.toJSONString();
		examineRepository.update(conn, EXP.INS().key("id", examineId), ex, true);

		return addNewData;
	}

	// 区级请求本组织下其他组织提交的审核
	public List<Examine> getExamineByDisId(DruidPooledConnection conn, Long districtId, Byte type, Byte status,
			Integer count, Integer offset) throws Exception {
		List<ORG> orgs = orgService.getORGs(conn, districtId, 512, 0);
		return examineRepository.getExamineByORGIds(conn, orgs, type, status, count, offset);

	}

	// 设置股权证号
	public void setShareCerNo(DruidPooledConnection conn, Long orgId, Long examineId, Long familyNumber,
			String shareCerNo) throws Exception {
		// 判断股权证号是否存在
		ORGUser orgUser = orgUserRepository.get(conn,
				EXP.INS().key("org_id", orgId).andKey("share_cer_no", shareCerNo));
		if (orgUser == null) {
			// 表示无此股权证
			ORGUser or = new ORGUser();
			or.shareCerNo = shareCerNo;
			orgUserRepository.update(conn, EXP.INS().key("org_id", orgId).andKey("family_number", familyNumber), or,
					true);
			// 将股权证放入newData数据中
			Examine ex = examineRepository.get(conn, EXP.INS().key("id", examineId));
			JSONObject jsob = JSONObject.parseObject(ex.data);
			JSONArray json = jsob.getJSONArray("newData");
			JSONArray editNewDatas = new JSONArray();
			for (int i = 0; i < json.size(); i++) {
				JSONArray newDatas = JSONArray.parseArray(json.getString(i));
				JSONArray editNewData = new JSONArray();
				for (int j = 0; j < newDatas.size(); j++) {
					JSONObject jo = newDatas.getJSONObject(j);
					Long org = jo.getLong("orgId");
					Long fam = jo.getLong("familyNumber");
					if (org.longValue() == orgId.longValue() && fam.longValue() == familyNumber.longValue()) {
						jo.put("shareCerNo", shareCerNo);
						editNewData.add(jo);
					} else {
						editNewData.add(jo);
					}
				}
				editNewDatas.add(editNewData);
			}
			jsob.put("newData", editNewDatas);
			Examine e = new Examine();
			e.data = jsob.toJSONString();
			examineRepository.update(conn, EXP.INS().key("id", examineId), e, true);

		} else {
			// 表示有股权证号存在
			throw new ServerException(BaseRC.REPOSITORY_SQL_EXECUTE_ERROR, "shareCerNo in org");
		}

	}

	// 修改状态
	public void editExamineStatus(DruidPooledConnection conn, Long examineId, Byte status) throws Exception {
		Examine ex = new Examine();
		ex.status = status;
		examineRepository.update(conn, EXP.INS().key("id", examineId), ex, true);

		Examine examine = examineRepository.get(conn, EXP.INS().key("id", examineId));
		if (examine.type == Examine.TYPE.FAMILY.v()) {
			message(conn, examine.data, ORGPermission.per_feparate_family.name, status);
		} else if (examine.type == Examine.TYPE.SHARE.v()) {
			shareMessage(conn, examine.data, ORGPermission.per_share_change.name, status);
		} else {
			message(conn, examine.data, ORGPermission.per_examine.name, status);
		}
	}

	// 查询股权证号是否已经存在 0 表示不存在 1 表示已经存在
	public int getFamilyByshareCerNo(DruidPooledConnection conn, Long orgId, String shareCerNo) throws Exception {
		// 判断股权证号是否存在
		ORGUser orgUser = orgUserRepository.get(conn,
				EXP.INS().key("org_id", orgId).andKey("share_cer_no", shareCerNo));
		if (orgUser == null) {
			return 0;
		} else {
			return 1;
		}

	}

	// 股权证号审核
	public Examine examineShareCerNo(DruidPooledConnection conn, Long orgId, Long examineId, Byte status)
			throws Exception {
		Examine exa = examineRepository.get(conn, EXP.INS().key("id", examineId));
		JSONObject jsonObj = JSONObject.parseObject(exa.data); // 转换为JSONObject数组

		Examine ex = new Examine();
		if (status == Examine.STATUS.ORGEXAMINE.v()) {
			// 组织审核
			ex.examineDate = new Date();
			ex.status = Examine.STATUS.ORGEXAMINE.v();
			examineRepository.update(conn, EXP.INS().key("id", examineId).andKey("org_id", orgId), ex, true);

			shareMessage(conn, exa.data, ORGPermission.per_share_change.name, ex.status);
			return ex;
		} else if (status == Examine.STATUS.DISEXAMINE.v()) {
			// 拿到数据
			// JSONObject jsonO = jsonObj.getJSONObject("newData");
			JSONArray json = jsonObj.getJSONArray("newData");
			for (int i = 0; i < json.size(); i++) {
				JSONObject jo = json.getJSONObject(i);
				JSONObject orU = jo.getJSONObject("orgUser");
				Long or = orU.getLong("orgId");
				Long userId = orU.getLong("userId");
				Long familyNumber = orU.getLong("familyNumber");
				String shareCerNo = orU.getString("shareCerNo");
				Double shareAmount = orU.getDouble("shareAmount");
				ORGUser orgUser = new ORGUser();
				orgUser.shareCerNo = shareCerNo;
				orgUser.shareAmount = shareAmount;
				orgUserRepository.update(conn,
						EXP.INS().key("org_id", or).andKey("user_id", userId).andKey("family_number", familyNumber),
						orgUser, true);
			}
			// 区级审核
			ex.examineDate = new Date();
			ex.status = Examine.STATUS.DISEXAMINE.v();
			examineRepository.update(conn, EXP.INS().key("id", examineId).andKey("org_id", orgId), ex, true);

			shareMessage(conn, exa.data, ORGPermission.per_share_change.name, ex.status);
			return ex;

		} else {
			// 审核失败
			ex.examineDate = new Date();
			ex.status = Examine.STATUS.FAIL.v();
			examineRepository.update(conn, EXP.INS().key("id", examineId).andKey("org_id", orgId), ex, true);
			shareMessage(conn, exa.data, ORGPermission.per_share_change.name, ex.status);
			return ex;
		}
	}

	// 删除审核
	public int delExamine(DruidPooledConnection conn, Long examineId) throws Exception {
		Examine ex = examineRepository.get(conn, EXP.INS().key("id", examineId));
		if (ex.status <= Examine.STATUS.DISEXAMINE.v()) {
			return examineRepository.delete(conn, EXP.INS().key("id", examineId));
		} else {
			return 0;
		}
	}

	// 获取用户
	public int getOrgUser(DruidPooledConnection conn, Long orgId, String idNumber) throws Exception {
		return orgUserRepository.getOrgUser(conn, orgId, idNumber);
	}

	public User editUserMobile(DruidPooledConnection conn, Long userId, String mobile) throws ServerException {
		User renew = new User();

		if (mobile == null) {
			renew.mobile = "";
		} else {
			List<User> ulist = userRepository.getList(conn, EXP.INS().key("mobile", mobile), 10, 0, "mobile");
			if (ulist.size() > 0 && ulist != null) {
				return null;
			}
			renew.mobile = mobile;
		}

		int ret = userRepository.update(conn, EXP.INS().key("id", userId), renew, true);


		return userRepository.get(conn, EXP.INS().key("id", userId));

	}

	public JSONObject getCountsByRole(DruidPooledConnection conn, Long orgId, JSONArray roles)
			throws Exception {
		JSONObject jo = new JSONObject();
		for(int i = 0; i < roles.size(); i++) {
			int count = orgUserRepository.getCountByRole(conn, orgId, roles.getLong(i));
			jo.put(roles.getLong(i).toString(), count);
		}
		return jo;
	}

}
