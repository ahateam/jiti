package zyxhj.jiti.service;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.vertx.core.Vertx;
import zyxhj.core.domain.LoginBo;
import zyxhj.core.domain.User;
import zyxhj.core.domain.UserSession;
import zyxhj.core.repository.UserRepository;
import zyxhj.custom.service.WxDataService;
import zyxhj.custom.service.WxFuncService;
import zyxhj.jiti.domain.District;
import zyxhj.jiti.domain.Family;
import zyxhj.jiti.domain.Notice;
import zyxhj.jiti.domain.NoticeTask;
import zyxhj.jiti.domain.NoticeTaskRecord;
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
import zyxhj.jiti.repository.NoticeRepository;
import zyxhj.jiti.repository.NoticeTaskRecordRepository;
import zyxhj.jiti.repository.NoticeTaskRepository;
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
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;

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
	private NoticeTaskRepository noticeTaskRepository;
	private NoticeTaskRecordRepository noticeTaskRecordRepository;
	private NoticeRepository noticeRepository;
	private WxDataService wxDataService;
	private WxFuncService wxFuncService;

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
			noticeTaskRepository = Singleton.ins(NoticeTaskRepository.class);
			noticeTaskRecordRepository = Singleton.ins(NoticeTaskRecordRepository.class);
			noticeRepository = Singleton.ins(NoticeRepository.class);
			wxDataService = Singleton.ins(WxDataService.class);
			wxFuncService = Singleton.ins(WxFuncService.class);
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
		ret.openId = user.wxOpenId;

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
		ret.groups = JSON.parseArray(orgUser.groups);
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
		User existUser = userRepository.get(conn, EXP.INS().key( "mobile", mobile));
		  
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
		ORG existORG = orgRepository.get(conn, EXP.INS().key( "code", code));
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
	
	public ORG createSubORG(DruidPooledConnection conn, Long orgExamineId, String name, String code,
			String address, String imgOrg, String imgAuth, Byte level, Integer shareAmount) throws Exception {
		ORG existORG = orgRepository.get(conn, EXP.INS().key("code", code));
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

			// 将数据添加到数据库
			orgRepository.insert(conn, newORG);
			
			return newORG;
		} else {
			// 组织已存在
			throw new ServerException(BaseRC.ECM_ORG_EXIST);
		}

	}

	/**
	 * 	创建下属组织
	 */
	public void createSubOrg(DruidPooledConnection conn, String name, String code,
			String address, String imgOrg, String imgAuth, Byte level, Integer shareAmount, Long superiorId,
			Long province, Long city, Long district) throws Exception {
		Long orgExamineId = IDUtils.getSimpleId();
		//创建组织
		createSubORG(conn, orgExamineId, name, code, address, imgOrg, imgAuth, level, shareAmount);

		// 创建上级关系
		addSupAndSub(conn, superiorId, orgExamineId, level);

		// 创建组织归属
		createORGDistrict(conn, orgExamineId, province, city, district);
		
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
		orgRepository.update(conn,EXP.INS().key("id", orgId), renew, true);
		
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

		orgRepository.update(conn,EXP.INS().key("id", orgId), renew, true);
	}

	/**
	 * 获取全部组织列表
	 */
	public List<ORG> getORGs(DruidPooledConnection conn, Long superiorId, int count, int offset) throws Exception {
		JSONArray json = new JSONArray();
		List<Superior> superior = superiorRepository.getList(conn,EXP.INS().key( "superior_id", superiorId), 512, 0);
		
		for (Superior sup : superior) {
			json.add(sup.orgId);
		}
		return orgRepository.getORGs(conn, json, count, offset);
	}

	/**
	 * 获取组织
	 */
	public ORG getORGById(DruidPooledConnection conn, Long orgId) throws Exception {
		return orgRepository.get(conn, EXP.INS().key( "id", orgId));
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
	 * @param mobile
	 *            电话号码
	 * @param pwd
	 *            密码
	 * @param 登录业务对象
	 */
	public LoginBo loginByMobile(DruidPooledConnection conn, String mobile, String pwd) throws Exception {
		User existUser = userRepository.get(conn, EXP.INS().key( "mobile", mobile));
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
	 * @param idNumber
	 *            身份证号码
	 * @param pwd
	 *            密码
	 */
	public LoginBo loginByIdNumber(DruidPooledConnection conn, String idNumber, String pwd) throws Exception {
		User existUser = userRepository.get(conn, EXP.INS().key( "id_number", idNumber));
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
		User existUser = userRepository.get(conn, EXP.INS().key( "id", userId));
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
		ORGUser orgUser = orgUserRepository.get(conn,EXP.INS().key("org_id", orgId).andKey("user_id", userId));
		ServiceUtils.checkNull(orgUser);

		User user = userRepository.get(conn, EXP.INS().key( "id", userId));
		ServiceUtils.checkNull(user);

		return loginORG(conn, user, orgUser);
	}

	public ORGLoginBo adminLoginInORG(DruidPooledConnection conn, Long userId, Long orgId) throws Exception {

		ORGUser orgUser = orgUserRepository.checkORGUserRoles(conn, orgId, userId,
				new ORGUserRole[] { ORGUserRole.role_admin });

		User user = userRepository.get(conn, EXP.INS().key( "id", userId));
		ServiceUtils.checkNull(user);

		return loginORG(conn, user, orgUser);
	}

	// 行政机构管理员登陆
	public ORGLoginBo areaAdminLoginInORG(DruidPooledConnection conn, Long userId, Long orgId) throws Exception {
		ORGUser orgUser = orgUserRepository.checkORGUserRoles(conn, orgId, userId,
				new ORGUserRole[] { ORGUserRole.role_Administractive_admin }); // 检查权限
		User user = userRepository.get(conn, EXP.INS().key( "id", userId)); // 获取用户信息
		ServiceUtils.checkNull(user); // 用户信息是否为空
		return loginORG(conn, user, orgUser); // 用户登录
	}

	/**
	 * 创建组织申请
	 */
	public ORGExamine createORGApply(DruidPooledConnection conn, Long userId, String name, String code, Long province,
			Long city, Long district, String address, String imgOrg, String imgAuth, Integer shareAmount, Byte level,
			Long superiorId) throws Exception {
		ORG existORG = orgRepository.get(conn, EXP.INS().key( "code", code));
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
		ORG org = orgRepository.get(conn, EXP.INS().key( "id", orgExamineId));
		// 如果org不为空 则表示是修改提交审核e
		if (org != null) {
			ex.orgId = org.id;
			// 判断是否要修改地址 如果要修改地址 则为ture 需要去表里删除对应的地址 然后再添加
			if (updateDistrict) {
				// 要修改 则需要删除以前的归属 加入新的归属
				orgDistrictRepository.delete(conn, EXP.INS().key("org_id", ex.orgId));
				
				// 创建组织归属
				createORGDistrict(conn, orgExamineId, province, city, district);
			}
			// 上级机构修改申请通过 则表示要修改原来的org 并将orgExamine修改为通过
			if (examine == ORGExamine.STATUS.WAITING.v()) {
				// 修改组织信息
				editORG(conn, name, code, orgExamineId, address, imgOrg, imgAuth, shareAmount);

				// 将修改申请表改为通过
				ex.examine = ORGExamine.STATUS.WAITING.v();
				orgExamineRepository.update(conn,EXP.INS().key("id", orgExamineId), ex, true);
				

			} else if (examine == ORGExamine.STATUS.INVALID.v()) {// 上级组织修改申请为失败

				ex.examine = ORGExamine.STATUS.INVALID.v();
				orgExamineRepository.update(conn,EXP.INS().key("id", orgExamineId), ex, true);
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
				orgExamineRepository.update(conn,EXP.INS().key("id", orgExamineId), ex, true);

			} else if (examine == ORGExamine.STATUS.INVALID.v()) {// 上级组织修改申请为失败

				ex.examine = ORGExamine.STATUS.INVALID.v();
				orgExamineRepository.update(conn,EXP.INS().key("id", orgExamineId), ex, true);

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

		ORGDistrict or = orgDistrictRepository.get(conn, EXP.INS().key( "org_id", orgExamineId));
		if (or == null) {
			orgDistrictRepository.insert(conn, ord);
		} else {
			orgDistrictRepository.update(conn,EXP.INS().key("org_id", orgExamineId), ord, true);
			
		}
	}

	// 将组织放入到关系表中
	private void addSupAndSub(DruidPooledConnection conn, Long superiorId, Long orgExamineId, Byte level)
			throws Exception {
		// 先查询是否存在 org 如果存在 直接删除 不存在 插入
		Superior su = superiorRepository.get(conn, EXP.INS().key( "org_id", orgExamineId));
		if (su != null) {
			superiorRepository.delete(conn, EXP.INS().key("org_id", orgExamineId));
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
			return orgExamineRepository.update(conn,EXP.INS().key("id", orgExamineId), newORG, true);
		} else {
			newORG.type = ORGExamine.TYPE.NOTINDEPENDENT.v();
			newORG.superiorId = superiorId;
			return orgExamineRepository.update(conn,EXP.INS().key("id", orgExamineId), newORG, true);
		}
	}

	// 查询组织申请列表
	public List<ORGExamine> getORGExamineByStatus(DruidPooledConnection conn, Byte status, Long superiorId,
			Integer count, Integer offset) throws Exception {

		return orgExamineRepository.getList(conn, EXP.INS().key("superior_id", superiorId).andKey("examine", status), count, offset);
		
	}

	// 查询自己提交的申请
	public List<ORGExamine> getORGExamineByUser(DruidPooledConnection conn, Long userId, Integer count, Integer offset)
			throws Exception {
		return orgExamineRepository.getList(conn,EXP.INS().key( "user_id", userId), count, offset);
	}

	// 删除申请
	public int delORGExamine(DruidPooledConnection conn, Long examineId) throws Exception {
		ORG getOrg = orgRepository.get(conn, EXP.INS().key( "id", examineId));
		if (getOrg != null) {
			ORGExamine or = new ORGExamine();
			or.examine = ORGExamine.STATUS.WAITING.v();
			return orgExamineRepository.update(conn,EXP.INS().key("id", examineId), or, true);
			
		} else {
			return orgExamineRepository.delete(conn, EXP.INS().key("id", examineId));
		}
	}

	// 统计组织角色
	public Map<String, Integer> countRole(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		return orgUserRepository.countRole(conn, orgId, roles);
	}

	// 添加分户
	public Family createFamily(DruidPooledConnection conn, Long orgId, Long familyNumber, String familyMaster)
			throws Exception {
		Family fa = new Family();
		fa.id = IDUtils.getSimpleId();
		fa.orgId = orgId;
		fa.familyNumber = familyNumber;
		fa.familyMaster = familyMaster;
		// 检查户序号是否已经存在

		// TODO orgId 希望做一个familyRepositroy的createFamily方法，然后ORGService和ORGUserService都用

		Family fn = familyRepository.get(conn, EXP.INS().key( "family_number", familyNumber));

		familyRepository.get(conn,EXP.INS().key("org_id", orgId).andKey("family_number", familyNumber));
		if (fn == null) {
			familyRepository.insert(conn, fa);
			return fa;
		} else {
			// 户已存在
			throw new ServerException(BaseRC.ECM_ORG_FAMILY_EXIST);
		}
	}

	// 修改分户 TODO 未完善
	public int editFamily(DruidPooledConnection conn, Long orgId, Long familyNumber, String familyMaster)
			throws Exception {
		Family fa = new Family();
		fa.orgId = orgId;
		fa.familyNumber = familyNumber;
		fa.familyMaster = familyMaster;
		// 检查户序号是否已经存在
		// Family fn = familyRepository.get(conn, EXP.ins().key( "family_number", familyNumber));

		return familyRepository.update(conn,EXP.INS().key("org_id", orgId), fa, true);
		
	}

	/**
	 * 查询省、市、区 如果father为空 说明是查询省 如果加了father 说明是查询市或者区
	 */
	public List<District> getProCityDistrict(DruidPooledConnection conn, Byte level, Long father, Integer count,
			Integer offset) throws Exception {
		if (father != null) {
			return districtRepository.getList(conn, EXP.INS().key("level", level).andKey("father", father), count, offset);
		} else {
			return districtRepository.getList(conn,EXP.INS().key( "level", level), count, offset);
		}
	}

	// 模糊查询机构
	public List<ORG> getOrgByNameAndLevel(DruidPooledConnection conn, Byte level, String orgName) throws Exception {
		return orgRepository.getOrgByNameAndLevel(conn, level, orgName);
	}

	// 查询组织地址
	public JSONObject getORGDistrict(DruidPooledConnection conn, Long orgId) throws Exception {
		ORGDistrict od = orgDistrictRepository.get(conn, EXP.INS().key( "org_id", orgId));
		JSONObject json = new JSONObject();
		if (od.proId != null) {
			json.put("province", districtRepository.get(conn, EXP.INS().key( "id", od.proId)));
		}
		if (od.cityId != null) {
			json.put("city", districtRepository.get(conn, EXP.INS().key( "id", od.cityId)));
		}
		if (od.disId != null) {
			json.put("district", districtRepository.get(conn, EXP.INS().key( "id", od.disId)));
		}
		return json;
	}

	// 查询地址
	public JSONObject getORGDistrictByOrgApplyId(DruidPooledConnection conn, Long orgExamineId) throws Exception {
		ORGExamine ex = orgExamineRepository.get(conn, EXP.INS().key( "id", orgExamineId));
		JSONObject json = new JSONObject();
		if (ex.province != null) {
			json.put("province", districtRepository.get(conn, EXP.INS().key( "id", ex.province)));
		}
		if (ex.city != null) {
			json.put("city", districtRepository.get(conn, EXP.INS().key( "id", ex.city)));
		}
		if (ex.district != null) {
			json.put("district", districtRepository.get(conn, EXP.INS().key( "id", ex.district)));
		}
		return json;
	}

	public List<ORG> getORGByName(DruidPooledConnection conn, String name, Integer count, Integer offset)
			throws Exception {
		return orgRepository.getOrgByName(conn, name, count, offset);
	}

	public Superior getSuperior(DruidPooledConnection conn, Long orgId) throws Exception {
		return superiorRepository.get(conn, EXP.INS().key( "org_id", orgId));
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
					ORGPermissionRel or = orgPermissionRelaRepository.get(conn,EXP.INS().key("org_id", orgId).andKey("role_id", json.getLong(i)).andKey("permission_id", permissionId));
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

	// 创建通知任务
	public NoticeTask addNoticeTask(DruidPooledConnection conn, Long orgId, Long userId, String title, String content,
			JSONObject crowd) throws Exception {
		// 添加任务信息
		NoticeTask no = new NoticeTask();
		no.id = IDUtils.getSimpleId();
		no.orgId = orgId;
		no.userId = userId;
		no.title = title;
		no.createTime = new Date();
		no.content = content;
		no.crowd = crowd.toJSONString();
		noticeTaskRepository.insert(conn, no);

		// 根据crowd查询出用户的openid 再把openid放入通知任务表中

		Integer s = noticeTaskRecordRepository.addNoticeTaskRecord(conn, orgId, no.id, crowd);
		no.sum = s;
		noticeTaskRepository.update(conn,EXP.INS().key("id", no.id), no, true);
		
		return no;
	}

	// 发布通知 微信或者短信
	public void sendingNotice(Long orgId, Long taskId, Byte type) {

		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DruidDataSource dsRds;
			DruidPooledConnection conn = null;
			try {
				dsRds = DataSource.getDruidDataSource("rdsDefault.prop");
				conn = (DruidPooledConnection) dsRds.getConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				NoticeTask notice = noticeTaskRepository.get(conn, EXP.INS().key( "id", taskId));
				for (int i = 0; i < (notice.sum / 100) + 1; i++) {
					List<NoticeTaskRecord> noticeRe = noticeTaskRecordRepository.getList(conn, EXP.INS().key("task_id", taskId).andKey("org_id", orgId).andKey("status",  NoticeTaskRecord.STATUS.UNDETECTED.v()),100, 0);
					for (NoticeTaskRecord noticeTaskRecord : noticeRe) {
						if (notice.mode == NoticeTask.MODE.WX.v()) {
							// 执行微信发送
							wxFuncService.templateMessage(wxDataService.getWxMpService(), noticeTaskRecord.openId,
									notice.title, notice.content, notice.createTime);
						} else if (notice.mode == NoticeTask.MODE.PHONENUMBER.v()) {
							// 执行短信发送
							SendSms(noticeTaskRecord.mobile, notice.content);
						} else if (notice.mode == NoticeTask.MODE.WXANDPHONE.v()) {
							// 微信和短信都发送
							wxFuncService.templateMessage(wxDataService.getWxMpService(), noticeTaskRecord.openId,
									notice.title, notice.content, notice.createTime);
							SendSms(noticeTaskRecord.mobile, notice.content);
						}
					}
				}

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

	// 创建公告
	public Notice createNotice(DruidPooledConnection conn, Long orgId, String title, String noticeContent, Byte type,
			String crowd) throws Exception {
		Notice no = new Notice();
		no.id = IDUtils.getSimpleId();
		no.orgId = orgId;
		no.title = title;
		no.content = noticeContent;
		no.type = type;
		no.crowd = crowd;
		no.createTime = new Date();
		no.endTime = new Date();
		noticeRepository.insert(conn, no);
		return no;
	}

	// 获取公告
	public List<Notice> getNotice(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		return noticeRepository.getNotice(conn, orgId, count, offset);
	}

	// 修改公告
	public Notice editNotice(DruidPooledConnection conn, Long noticeId, Long orgId, String title, String noticeContent,
			Byte type, String crowd) throws Exception {
		Notice no = new Notice();
		no.title = title;
		no.content = noticeContent;
		no.type = type;
		no.crowd = crowd;
		no.createTime = new Date();
		noticeRepository.update(conn,EXP.INS().key("id", noticeId).andKey("org_id", orgId), no, true);
		
		return no;
	}

	// 删除公告
	public int deleteNotice(DruidPooledConnection conn, Long noticeId) throws Exception {
		return noticeRepository.delete(conn, EXP.INS().key("id", noticeId));
	}

	// 用户查看公告
	public List<Notice> getNoticeByRoleGroup(DruidPooledConnection conn, Long orgId, String roles, String groups)
			throws Exception {
		return noticeRepository.getNoticeByRoleGroup(conn, orgId, roles, groups);
	}

	// 绑定微信openid
	public User bdUserOpenId(DruidPooledConnection conn, Long userId, String openId) throws Exception {
		// 将openid存入User表中
		User user = new User();
		user.wxOpenId = openId;
		userRepository.update(conn,EXP.INS().key("id", userId), user, true);
		
		return user;
	}

	// 通过用户openId进行登陆
	public User loginByOpenId(DruidPooledConnection conn, String openId) throws Exception {
		// 通过openId去数据库里匹配 如果有 则正常登陆 如果没有 则表示需要绑定
		User wxlogin = userRepository.get(conn, EXP.INS().key( "wx_open_id", openId));
		if (wxlogin == null) {
			return null;
		} else {
			return wxlogin;
		}
	}

	// 解除绑定
	public int removeOpenId(DruidPooledConnection conn, Long userId) throws Exception {

		User us = userRepository.get(conn, EXP.INS().key( "id", userId));
		// userRepository.delete(conn, EXP.ins().key("id", userId));

		// User user = new User();
		us.wxOpenId = null;
		// user.id = userId;
		// user.wxOpenId = "";

		// userRepository.insert(conn, us);
		return userRepository.update(conn,EXP.INS().key("id", userId), us, false);
	}

	// 短信群发
	public CommonResponse SendSms(String phone, String content) {
		// accessKeyId
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAIJ9mYIjuW54Cj",
				"89EMlXLsP13H8mWKIvdr4iM1OvdVxs"); // 这里需要添加主账号的accessKeyId和accessSecret
		IAcsClient client = new DefaultAcsClient(profile);
		CommonRequest request = new CommonRequest();
		// request.setProtocol(ProtocolType.HTTPS);
		request.setMethod(MethodType.POST); // 提交方式
		request.setDomain("dysmsapi.aliyuncs.com");
		request.setVersion("2017-05-25");
		request.setAction("SendSms"); // 短信发送接口
		request.putQueryParameter("RegionId", "cn-hangzhou");
		request.putQueryParameter("PhoneNumbers", phone); // 电话号码 如果要发送给多人 则使用逗号分隔 上限为1000个号码
		request.putQueryParameter("SignName", "遵义小红椒"); // 短信标签名称 需要创建审核标签
		request.putQueryParameter("TemplateCode", "SMS_165341503"); // 短信模板id
		request.putQueryParameter("TemplateParam", StringUtils.join("{\"content\":\"", content, "\"}")); // 短信模板变量值

		try {
			CommonResponse response = client.getCommonResponse(request);
			System.out.println(response.getData());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public int delSubOrg(DruidPooledConnection conn, Long id) throws ServerException {
		try {
			orgRepository.delete(conn, EXP.INS().key("id", id));
			superiorRepository.delete(conn, EXP.INS().key("org_id", id));
			orgDistrictRepository.delete(conn, EXP.INS().key("org_id", id));
			return 1;
		} catch (Exception e) {
			return 0;
		}
	}
}
