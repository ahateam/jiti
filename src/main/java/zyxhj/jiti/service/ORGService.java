package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.core.domain.LoginBo;
import zyxhj.core.domain.User;
import zyxhj.core.domain.UserSession;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.District;
import zyxhj.jiti.domain.Family;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGDistrict;
import zyxhj.jiti.domain.ORGExamine;
import zyxhj.jiti.domain.ORGLoginBo;
import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.Superior;
import zyxhj.jiti.repository.DistrictRepository;
import zyxhj.jiti.repository.FamilyRepository;
import zyxhj.jiti.repository.ORGDistrictRepository;
import zyxhj.jiti.repository.ORGExamineRepository;
import zyxhj.jiti.repository.ORGPermissionRelaRepository;
import zyxhj.jiti.repository.ORGRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.repository.SuperiorRepository;
import zyxhj.utils.CacheCenter;
import zyxhj.utils.IDUtils;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;

public class ORGService {

	private static Logger log = LoggerFactory.getLogger(ORGService.class);

	private ORGRepository orgRepository;
	private ORGUserRepository orgUserRepository;
	private UserRepository userRepository;
	private ORGExamineRepository orgExamineRepository;
	private FamilyRepository familyRepository;
	private SuperiorRepository superiorRepository;
	private ORGDistrictRepository orgDistrictRepository;
	private DistrictRepository districtRepository;
	private ORGPermissionRelaRepository orgPermissionRelaRepository;
	private ORGPermissionService orgPermissionService;

	public ORGService() {
		try {
			orgRepository = Singleton.ins(ORGRepository.class);
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			userRepository = Singleton.ins(UserRepository.class);
			orgExamineRepository = Singleton.ins(ORGExamineRepository.class);
			familyRepository = Singleton.ins(FamilyRepository.class);
			superiorRepository = Singleton.ins(SuperiorRepository.class);
			orgDistrictRepository = Singleton.ins(ORGDistrictRepository.class);
			districtRepository = Singleton.ins(DistrictRepository.class);
			orgPermissionRelaRepository = Singleton.ins(ORGPermissionRelaRepository.class);
			orgPermissionService = Singleton.ins(ORGPermissionService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 从UserService拷贝过来的
	 */
	private UserSession putUserSession(Long userId, Date loginTime, String loginToken) throws Exception {

		UserSession ret = new UserSession();
		ret.userId = userId;
		ret.loginTime = loginTime;
		ret.loginToken = loginToken;

		// 先放入Session缓存，再放入存储
		CacheCenter.SESSION_CACHE.put(userId, ret);
		return ret;
	}

	/**
	 * 从UserService拷贝过来的
	 */
	private LoginBo login(DruidPooledConnection conn, User user) throws Exception {
		Date loginTime = new Date();
		UserSession userSession = putUserSession(user.id, loginTime, IDUtils.getHexSimpleId());

		LoginBo ret = new LoginBo();
		ret.id = user.id;
		ret.name = user.name;
		ret.idNumber = user.idNumber;
		ret.realName = user.realName;
		ret.nickname = user.nickname;
		ret.signature = user.signature;

		ret.idNumber = user.idNumber;
		ret.mobile = user.mobile;

		ret.roles = user.roles;

		ret.loginTime = userSession.loginTime;
		ret.loginToken = userSession.loginToken;

		return ret;
	}

	/**
	 * 从UserService拷贝过来的，然后简单修改，增加了组织信息
	 */
	private ORGLoginBo loginORG(DruidPooledConnection conn, User user, ORGUser orgUser) throws Exception {
		Date loginTime = new Date();
		UserSession userSession = putUserSession(user.id, loginTime, IDUtils.getHexSimpleId());

		ORGLoginBo ret = new ORGLoginBo();

		ret.id = user.id;
		ret.name = user.name;
		ret.idNumber = user.idNumber;
		ret.nickname = user.nickname;
		ret.signature = user.signature;
		ret.mobile = user.mobile;

		ret.roles = user.roles;

		ret.loginTime = userSession.loginTime;
		ret.loginToken = userSession.loginToken;

		ret.orgId = orgUser.orgId;
		ret.address = orgUser.address;
		ret.shareCerNo = orgUser.shareCerNo;
		ret.shareCerImg = orgUser.shareCerImg;
		ret.shareCerHolder = orgUser.shareCerHolder;

		ret.shareAmount = orgUser.shareAmount;
		ret.weight = orgUser.weight;

		ret.orgRoles = JSON.parseArray(orgUser.roles);
		ret.permissions = orgPermissionService.getPermissionsByRoles(conn, orgUser.orgId, ret.orgRoles);
		ret.orgTags = JSON.parseObject(orgUser.tags);

		return ret;
	}

	/**
	 * 普通用户注册
	 */
	public LoginBo registeUser(DruidPooledConnection conn, String mobile, String pwd, String realName, String idNumber)
			throws Exception {
		// 判断用户是否存在
		User existUser = userRepository.getByKey(conn, "mobile", mobile);
		if (null == existUser) {
			// 用户不存在
			User newUser = new User();
			newUser.id = IDUtils.getSimpleId();
			newUser.createDate = new Date();
			newUser.mobile = mobile;

			newUser.realName = realName;
			newUser.idNumber = idNumber;

			newUser.pwd = pwd;// TODO 目前是明文，需要加料传输和存储

			// 创建用户
			userRepository.insert(conn, newUser);
			newUser.pwd = null;// 抹掉密码
			return login(conn, newUser);
		} else {
			// 用户已存在
			throw new ServerException(BaseRC.USER_EXIST);
		}
	}

	/**
	 * 创建组织，按组织机构代码证排重
	 */
	public JSONObject createORG(DruidPooledConnection conn, Long orgExamineId, Long userId, String name, String code,
			String address, String imgOrg, String imgAuth, Byte level, Integer shareAmount) throws Exception {
		ORG existORG = orgRepository.getByKey(conn, "code", code);
		if (null == existORG) {
			// 组织不存在
			ORG newORG = new ORG();

			newORG.id = orgExamineId;
			newORG.createTime = new Date();
			newORG.name = name;
			newORG.code = code;
			newORG.address = address;
			newORG.imgOrg = imgOrg;
			newORG.imgAuth = imgAuth;
			newORG.shareAmount = shareAmount;
			newORG.level = level;

			// 判断为哪个等级的机构
			if (level == ORG.LEVEL.COOPERATIVE.v()) {
				newORG.type = ORG.TYPE.COOPERATIVE.v();
			} else if (level == ORG.LEVEL.PRO.v() || level == ORG.LEVEL.CITY.v() || level == ORG.LEVEL.DISTRICT.v()) {
				newORG.type = ORG.TYPE.ADMINISTRATIVEORGAN.v();
			}

			orgRepository.insert(conn, newORG);

			// 创建组织用户
			ORGUser orgUser = new ORGUser();
			orgUser.orgId = newORG.id;
			orgUser.userId = userId;
			if (level == ORG.LEVEL.PRO.v() || level == ORG.LEVEL.CITY.v() || level == ORG.LEVEL.DISTRICT.v()) {
				JSONArray roles = new JSONArray();
				roles.add(ORGUserRole.role_Administractive_admin.roleId);
				orgUser.roles = JSON.toJSONString(roles);

			} else if (level == ORG.LEVEL.COOPERATIVE.v()) {
				JSONArray roles = new JSONArray();
				roles.add(ORGUserRole.role_admin.roleId);
				orgUser.roles = JSON.toJSONString(roles);
			}

			orgUserRepository.insert(conn, orgUser);

			JSONObject ret = new JSONObject();
			ret.put("org", newORG);
			ret.put("orgUser", orgUser);

			return ret;
		} else {
			// 组织已存在
			throw new ServerException(BaseRC.ECM_ORG_EXIST);
		}

	}

	/**
	 * 更新组织信息，目前全都可以改，将来应该限定code，name等不允许更改</br>
	 * 填写空表示不更改
	 */
	public void editORG(DruidPooledConnection conn, String ogName, String code, Long orgId, String address,
			String imgOrg, String imgAuth, Integer shareAmount) throws Exception {

		ORG renew = new ORG();
		renew.name = ogName;
		renew.code = code;
		renew.address = address;
		renew.imgOrg = imgOrg;
		renew.imgAuth = imgAuth;
		renew.shareAmount = shareAmount;
		orgRepository.updateByKey(conn, "id", orgId, renew, true);
	}

	/**
	 * 编辑组织扩展信息（资金，负债，债权资金，年毛收入，分红，预算，决算，对外投资，估值）
	 */
	public void editORGExt(DruidPooledConnection conn, Long orgId, Double capital, Double debt, Double receivables,
			Double income, Double bonus, Double budget, Double financialBudget, Double investment, Double valuation)
			throws Exception {

		ORG renew = new ORG();
		renew.capital = capital;
		renew.debt = debt;
		renew.receivables = receivables;
		renew.income = income;
		renew.bonus = bonus;
		renew.budget = budget;
		renew.financialBudget = financialBudget;
		renew.investment = investment;
		renew.valuation = valuation;

		orgRepository.updateByKey(conn, "id", orgId, renew, true);
	}

	/**
	 * 获取全部组织列表
	 */
	public List<ORG> getORGs(DruidPooledConnection conn, Long superiorId, int count, int offset) throws Exception {
		JSONArray json = new JSONArray();
		List<Superior> superior = superiorRepository.getListByKey(conn, "superior_id", superiorId, null, null);
		for (Superior sup : superior) {
			json.add(sup.orgId);
		}
		return orgRepository.getORGs(conn, json, count, offset);
	}

	/**
	 * 获取组织
	 */
	public ORG getORGById(DruidPooledConnection conn, Long orgId) throws Exception {
		return orgRepository.getByKey(conn, "id", orgId);
	}

	/**
	 * 获取用户对应的组织列表
	 */
	public JSONArray getUserORGs(DruidPooledConnection conn, Long userId, Byte level, Integer count, Integer offset)
			throws Exception {
		return orgRepository.getUserORGs(conn, userId, level, count, offset);
	}

	/**
	 * 成员登录
	 * 
	 * @param mobile 电话号码
	 * @param pwd    密码
	 * @param 登录业务对象
	 */
	public LoginBo loginByMobile(DruidPooledConnection conn, String mobile, String pwd) throws Exception {
		User existUser = userRepository.getByKey(conn, "mobile", mobile);
		if (null == existUser) {
			// 用户不存在
			throw new ServerException(BaseRC.USER_NOT_EXIST);
		} else {
			// 用户已存在，匹配密码
			// TODO 目前是明文，需要加料然后匹配
			if (pwd.equals(existUser.pwd)) {

				return login(conn, existUser);
			} else {
				// 密码错误
				throw new ServerException(BaseRC.USER_PWD_ERROR);
			}
		}
	}

	/**
	 * 
	 * @param idNumber 身份证号码
	 * @param pwd      密码
	 */
	public LoginBo loginByIdNumber(DruidPooledConnection conn, String idNumber, String pwd) throws Exception {
		User existUser = userRepository.getByKey(conn, "id_number", idNumber);
		if (null == existUser) {
			// 用户不存在
			throw new ServerException(BaseRC.USER_NOT_EXIST);
		} else {
			// 用户已存在，匹配密码
			// TODO 目前是明文，需要加料然后匹配
			if (pwd.equals(existUser.pwd)) {

				return login(conn, existUser);
			} else {
				// 密码错误
				throw new ServerException(BaseRC.USER_PWD_ERROR);
			}
		}
	}

	public LoginBo loginByUserId(DruidPooledConnection conn, Long userId, String pwd) throws Exception {
		User existUser = userRepository.getByKey(conn, "id", userId);
		if (null == existUser) {
			// 用户不存在
			throw new ServerException(BaseRC.USER_NOT_EXIST);
		} else {
			// 用户已存在，匹配密码
			// TODO 目前是明文，需要加料然后匹配
			if (pwd.equals(existUser.pwd)) {

				return login(conn, existUser);
			} else {
				// 密码错误
				throw new ServerException(BaseRC.USER_PWD_ERROR);
			}
		}
	}

	/**
	 * 不够严谨的组织登录，暂时没有更好的办法
	 */
	public ORGLoginBo loginInORG(DruidPooledConnection conn, Long userId, Long orgId) throws Exception {
		ORGUser orgUser = orgUserRepository.getByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId });
		ServiceUtils.checkNull(orgUser);

		User user = userRepository.getByKey(conn, "id", userId);
		ServiceUtils.checkNull(user);

		return loginORG(conn, user, orgUser);
	}

	public ORGLoginBo adminLoginInORG(DruidPooledConnection conn, Long userId, Long orgId) throws Exception {

		ORGUser orgUser = orgUserRepository.checkORGUserRoles(conn, orgId, userId,
				new ORGUserRole[] { ORGUserRole.role_admin });

		User user = userRepository.getByKey(conn, "id", userId);
		ServiceUtils.checkNull(user);

		return loginORG(conn, user, orgUser);
	}

	// 行政机构管理员登陆
	public ORGLoginBo areaAdminLoginInORG(DruidPooledConnection conn, Long userId, Long orgId) throws Exception {
		ORGUser orgUser = orgUserRepository.checkORGUserRoles(conn, orgId, userId,
				new ORGUserRole[] { ORGUserRole.role_Administractive_admin }); // 检查权限
		User user = userRepository.getByKey(conn, "id", userId); // 获取用户信息
		ServiceUtils.checkNull(user); // 用户信息是否为空
		return loginORG(conn, user, orgUser); // 用户登录
	}

	/**
	 * 创建组织申请
	 */
	public ORGExamine createORGApply(DruidPooledConnection conn, Long userId, String name, String code, Long province,
			Long city, Long district, String address, String imgOrg, String imgAuth, Integer shareAmount, Byte level,
			Long superiorId) throws Exception {
		ORG existORG = orgRepository.getByKey(conn, "code", code);
		if (null == existORG) {
			// 组织不存在
			ORGExamine newORG = new ORGExamine();

			newORG.id = IDUtils.getSimpleId();
			newORG.createTime = new Date();
			newORG.userId = userId;
			newORG.name = name;
			newORG.code = code;
			newORG.province = province;
			newORG.city = city;
			newORG.district = district;
			newORG.address = address;
			newORG.imgOrg = imgOrg;
			newORG.imgAuth = imgAuth;
			newORG.shareAmount = shareAmount;
			newORG.level = level;
			newORG.examine = ORGExamine.STATUS.VOTING.v();

			if (superiorId == null) {
				newORG.type = ORGExamine.TYPE.INDEPENDENT.v();
				// TODO 现为直接插入数据库 平台管理出来以后使用申请方式
				createORG(conn, newORG.id, userId, name, code, address, imgOrg, imgAuth, level, shareAmount);
				// orgExamineRepository.insert(conn, newORG); //申请方式
			} else {
				newORG.type = ORGExamine.TYPE.NOTINDEPENDENT.v();
				newORG.superiorId = superiorId;
				orgExamineRepository.insert(conn, newORG);
			}

			return newORG;
		} else {
			// 组织已存在
			throw new ServerException(BaseRC.ECM_ORG_EXIST);
		}
	}

	// 修改组织申请状态 新的申请
	public ORGExamine upORGApply(DruidPooledConnection conn, Long orgExamineId, Byte examine, Long userId, String name,
			String code, Long province, Long city, Long district, String address, String imgOrg, String imgAuth,
			Integer shareAmount, Byte level, Long superiorId, Boolean updateDistrict) throws Exception {

		ORGExamine ex = new ORGExamine();
		// 是否有org
		ORG org = orgRepository.getByKey(conn, "id", orgExamineId);
		// 如果org不为空 则表示是修改提交审核
		if (org != null) {
			ex.orgId = org.id;
			// 判断是否要修改地址 如果要修改地址 则为ture 需要去表里删除对应的地址 然后再添加
			if (updateDistrict) {
				// 要修改 则需要删除以前的归属 加入新的归属
				orgDistrictRepository.deleteByKey(conn, "org_id", ex.orgId);

				// 创建组织归属
				createORGDistrict(conn, orgExamineId, province, city, district);
			}
			// 上级机构修改申请通过 则表示要修改原来的org 并将orgExamine修改为通过
			if (examine == ORGExamine.STATUS.WAITING.v()) {
				// 修改组织信息
				editORG(conn, name, code, orgExamineId, address, imgOrg, imgAuth, shareAmount);

				// 将修改申请表改为通过
				ex.examine = ORGExamine.STATUS.WAITING.v();
				orgExamineRepository.updateByKey(conn, "id", orgExamineId, ex, true);

			} else if (examine == ORGExamine.STATUS.INVALID.v()) {// 上级组织修改申请为失败

				ex.examine = ORGExamine.STATUS.INVALID.v();
				orgExamineRepository.updateByKey(conn, "id", orgExamineId, ex, true);
			}

		} else {// 如果为空 则表示为新的提交

			// 上级机构修改申请通过 则表示要修改原来的org 并将orgExamine修改为通过
			if (examine == ORGExamine.STATUS.WAITING.v()) {
				// 创建组织
				createORG(conn, orgExamineId, userId, name, code, address, imgOrg, imgAuth, level, shareAmount);

				// 创建上级关系
				addSupAndSub(conn, superiorId, orgExamineId, level);

				// 创建组织归属
				createORGDistrict(conn, orgExamineId, province, city, district);

				// 将修改申请表改为通过
				ex.examine = ORGExamine.STATUS.WAITING.v();
				orgExamineRepository.updateByKey(conn, "id", orgExamineId, ex, true);

			} else if (examine == ORGExamine.STATUS.INVALID.v()) {// 上级组织修改申请为失败

				ex.examine = ORGExamine.STATUS.INVALID.v();
				orgExamineRepository.updateByKey(conn, "id", orgExamineId, ex, true);

			}

		}

		return ex;
	}

	// 组织归属
	private void createORGDistrict(DruidPooledConnection conn, Long orgExamineId, Long province, Long city,
			Long district) throws Exception {

		ORGDistrict ord = new ORGDistrict();
		ord.id = IDUtils.getSimpleId();
		ord.orgId = orgExamineId;
		ord.proId = province;
		ord.cityId = city;
		ord.disId = district;

		ORGDistrict or = orgDistrictRepository.getByKey(conn, "org_id", orgExamineId);
		if (or == null) {
			orgDistrictRepository.insert(conn, ord);
		} else {
			orgDistrictRepository.updateByKey(conn, "org_id", orgExamineId, ord, true);
		}
	}

	// 将组织放入到关系表中
	private void addSupAndSub(DruidPooledConnection conn, Long superiorId, Long orgExamineId, Byte level)
			throws Exception {
		// 先查询是否存在 org 如果存在 直接删除 不存在 插入
		Superior su = superiorRepository.getByKey(conn, "org_id", orgExamineId);
		if (su != null) {
			superiorRepository.deleteByKey(conn, "org_id", orgExamineId);
		} else {
			Superior sup = new Superior();
			sup.superiorId = superiorId;
			sup.orgId = orgExamineId;

			superiorRepository.insert(conn, sup);
		}

	}

	// 再次提交组织申请
	public int upORGApplyAgain(DruidPooledConnection conn, Long orgExamineId, Long userId, String name, String code,
			Long province, Long city, Long district, String address, String imgOrg, String imgAuth, Integer shareAmount,
			Byte level, Long superiorId, Long orgId, Boolean updateDistrict) throws Exception {
		ORGExamine newORG = new ORGExamine();

		newORG.createTime = new Date();
		newORG.userId = userId;
		newORG.name = name;
		newORG.code = code;
		newORG.province = province;
		newORG.city = city;
		newORG.district = district;
		newORG.address = address;
		newORG.imgOrg = imgOrg;
		newORG.imgAuth = imgAuth;
		newORG.level = level;
		newORG.shareAmount = shareAmount;
		newORG.updateDistrict = updateDistrict;

		if (orgId != null) {
			newORG.orgId = orgId;
			newORG.examine = ORGExamine.STATUS.EXAMINE.v(); // 组织表下有数据 则表示需要再次审核
		} else {
			newORG.examine = ORGExamine.STATUS.VOTING.v(); // 组织表下无数据 表示当前组织审核未通过 需要再次审核的数据
		}

		if (superiorId == 1) {
			newORG.type = ORGExamine.TYPE.INDEPENDENT.v();
			newORG.superiorId = superiorId;
			return orgExamineRepository.updateByKey(conn, "id", orgExamineId, newORG, true);
		} else {
			newORG.type = ORGExamine.TYPE.NOTINDEPENDENT.v();
			newORG.superiorId = superiorId;
			return orgExamineRepository.updateByKey(conn, "id", orgExamineId, newORG, true);
		}
	}

	// 查询组织申请列表
	public List<ORGExamine> getORGExamineByStatus(DruidPooledConnection conn, Byte status, Long superiorId,
			Integer count, Integer offset) throws Exception {

		return orgExamineRepository.getListByANDKeys(conn, new String[] { "superior_id", "examine" },
				new Object[] { superiorId, status }, count, offset);
	}

	// 查询自己提交的申请
	public List<ORGExamine> getORGExamineByUser(DruidPooledConnection conn, Long userId, Integer count, Integer offset)
			throws Exception {
		return orgExamineRepository.getListByKey(conn, "user_id", userId, count, offset);
	}

	// 删除申请
	public int delORGExamine(DruidPooledConnection conn, Long examineId) throws Exception {
		ORG getOrg = orgRepository.getByKey(conn, "id", examineId);
		if (getOrg != null) {
			ORGExamine or = new ORGExamine();
			or.examine = ORGExamine.STATUS.WAITING.v();
			return orgExamineRepository.updateByKey(conn, "id", examineId, or, true);
		} else {
			return orgExamineRepository.deleteByKey(conn, "id", examineId);
		}
	}

	// 统计组织角色
	public Map<String, Integer> countRole(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		return orgUserRepository.countRole(conn, orgId, roles);
	}

	// 添加分户
	public Family createFamily(DruidPooledConnection conn, Long orgId, String familyNumber, String familyMaster)
			throws Exception {
		Family fa = new Family();
		fa.id = IDUtils.getSimpleId();
		fa.orgId = orgId;
		fa.familyNumber = familyNumber;
		fa.familyMaster = familyMaster;
		// 检查户序号是否已经存在

		// TODO orgId 希望做一个familyRepositroy的createFamily方法，然后ORGService和ORGUserService都用

		Family fn = familyRepository.getByKey(conn, "family_number", familyNumber);

		familyRepository.getByANDKeys(conn, new String[] { "org_id", "family_number" },
				new Object[] { orgId, familyNumber });
		if (fn == null) {
			familyRepository.insert(conn, fa);
			return fa;
		} else {
			// 户已存在
			throw new ServerException(BaseRC.ECM_ORG_FAMILY_EXIST);
		}
	}

	// 修改分户 TODO 未完善
	public int editFamily(DruidPooledConnection conn, Long orgId, String familyNumber, String familyMaster)
			throws Exception {
		Family fa = new Family();
		fa.orgId = orgId;
		fa.familyNumber = familyNumber;
		fa.familyMaster = familyMaster;
		// 检查户序号是否已经存在
		// Family fn = familyRepository.getByKey(conn, "family_number", familyNumber);

		return familyRepository.updateByKey(conn, "org_id", orgId, fa, true);
	}

	// 所有户列表
	public List<Family> getFamilyAll(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		return familyRepository.getListByKey(conn, "org_id", orgId, count, offset);
	}

	/**
	 * 查询省、市、区 如果father为空 说明是查询省 如果加了father 说明是查询市或者区
	 */
	public List<District> getProCityDistrict(DruidPooledConnection conn, Byte level, Long father, Integer count,
			Integer offset) throws Exception {
		if (father != null) {
			return districtRepository.getListByANDKeys(conn, new String[] { "level", "father" },
					new Object[] { level, father }, count, offset);
		} else {
			return districtRepository.getListByKey(conn, "level", level, count, offset);
		}
	}

	// 模糊查询机构
	public List<ORG> getOrgByNameAndLevel(DruidPooledConnection conn, Byte level, String orgName) throws Exception {
		return orgRepository.getOrgByNameAndLevel(conn, level, orgName);
	}

	// 查询组织地址
	public JSONObject getORGDistrict(DruidPooledConnection conn, Long orgId) throws Exception {
		ORGDistrict od = orgDistrictRepository.getByKey(conn, "org_id", orgId);
		JSONObject json = new JSONObject();
		if (od.proId != null) {
			json.put("province", districtRepository.getByKey(conn, "id", od.proId));
		}
		if (od.cityId != null) {
			json.put("city", districtRepository.getByKey(conn, "id", od.cityId));
		}
		if (od.disId != null) {
			json.put("district", districtRepository.getByKey(conn, "id", od.disId));
		}
		return json;
	}

	// 查询地址
	public JSONObject getORGDistrictByOrgApplyId(DruidPooledConnection conn, Long orgExamineId) throws Exception {
		ORGExamine ex = orgExamineRepository.getByKey(conn, "id", orgExamineId);
		JSONObject json = new JSONObject();
		if (ex.province != null) {
			json.put("province", districtRepository.getByKey(conn, "id", ex.province));
		}
		if (ex.city != null) {
			json.put("city", districtRepository.getByKey(conn, "id", ex.city));
		}
		if (ex.district != null) {
			json.put("district", districtRepository.getByKey(conn, "id", ex.district));
		}
		return json;
	}

	public List<ORG> getORGByName(DruidPooledConnection conn, String name, Integer count, Integer offset)
			throws Exception {
		return orgRepository.getOrgByName(conn, name, count, offset);
	}

	public Superior getSuperior(DruidPooledConnection conn, Long orgId) throws Exception {
		return superiorRepository.getByKey(conn, "org_id", orgId);
	}

	private static Cache<String, ORGPermissionRel> AUTH_PERMISSION_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(30, TimeUnit.SECONDS)//
			.maximumSize(1000)//
			.build();

	// 鉴定权限
	public void userAuth(DruidPooledConnection conn, Long orgId, String roles, Long permissionId) throws Exception {
		boolean check = true;
		JSONArray json = JSONArray.parseArray(roles);
		for (int i = 0; i < json.size(); i++) {
			if (json.getLong(i) == ORGUserRole.role_admin.roleId) {
				check = false;
			}
		}
		if (check) {
			boolean ro = false;
			for (int i = 0; i < json.size(); i++) {
				String ex = StringUtils.join(orgId, json.getLong(i), permissionId);
				// 从缓存里查找
				ORGPermissionRel role = AUTH_PERMISSION_CACHE.getIfPresent(ex);
				if (role == null) {
					// 缓存中没有 从数据库中查找
					ORGPermissionRel or = orgPermissionRelaRepository.getByANDKeys(conn,
							new String[] { "org_id", "role_id", "permission_id" },
							new Object[] { orgId, json.getLong(i), permissionId });
					if (or != null) {
						ro = true;
						AUTH_PERMISSION_CACHE.put(ex, or);
					}
				} else {
					ro = true;
				}
			}
			if (ro == false) {
				throw new ServerException(BaseRC.USER_NO_PERMISSION);
			}
		}
	}

	// 查询下级组织管理员
	public JSONArray getSubORGUser(DruidPooledConnection conn, Long orgId, Byte level, Integer count, Integer offset)
			throws Exception {
		return orgUserRepository.getORGAdmin(conn, orgId, level, count, offset);
	}

}
