package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.LoginBo;
import zyxhj.core.domain.User;
import zyxhj.core.domain.UserSession;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGLoginBo;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.repository.ORGRepository;
import zyxhj.jiti.repository.ORGUserRepository;
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

	public ORGService() {
		try {
			orgRepository = Singleton.ins(ORGRepository.class);
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			userRepository = Singleton.ins(UserRepository.class);

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
	public JSONObject createORG(DruidPooledConnection conn, Long userId, String name, String code, String province,
			String city, String district, String address, String imgOrg, String imgAuth, Integer shareAmount)
			throws Exception {
		ORG existORG = orgRepository.getByKey(conn, "code", code);
		if (null == existORG) {
			// 组织不存在
			ORG newORG = new ORG();

			newORG.id = IDUtils.getSimpleId();
			newORG.createTime = new Date();
			newORG.name = name;
			newORG.code = code;
			newORG.province = province;
			newORG.city = city;
			newORG.district = district;
			newORG.address = address;
			newORG.imgOrg = imgOrg;
			newORG.imgAuth = imgAuth;
			newORG.shareAmount = shareAmount;

			orgRepository.insert(conn, newORG);

			// 创建组织用户
			ORGUser orgUser = new ORGUser();
			orgUser.orgId = newORG.id;
			orgUser.userId = userId;

			JSONArray roles = new JSONArray();
			roles.add(ORGUserRole.role_admin.roleId);
			orgUser.roles = JSON.toJSONString(roles);

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
	public void editORG(DruidPooledConnection conn, Long orgId, String province, String city, String district,
			String address, String imgOrg, String imgAuth, Integer shareAmount) throws Exception {

		ORG renew = new ORG();
		renew.province = province;
		renew.city = city;
		renew.district = district;
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
	public List<ORG> getORGs(DruidPooledConnection conn, int count, int offset) throws Exception {
		return orgRepository.getList(conn, count, offset);
	}

	/**
	 * 获取全部组织列表
	 */
	public ORG getORGById(DruidPooledConnection conn, Long orgId) throws Exception {
		return orgRepository.getByKey(conn, "id", orgId);
	}

	/**
	 * 获取用户对应的组织列表 TODO 用sql重做一次，不要两次查询
	 */
	public List<ORG> getUserORGs(DruidPooledConnection conn, Long userId) throws Exception {
		List<ORGUser> ors = orgUserRepository.getListByKey(conn, "user_id", userId, 512, 0);
		if (ors == null || ors.size() == 0) {
			return new ArrayList<ORG>();
		} else {
			String[] values = new String[ors.size()];
			for (int i = 0; i < ors.size(); i++) {
				values[i] = ors.get(i).orgId.toString();
			}
			return orgRepository.getListByKeyInValues(conn, "id", values);
		}
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
		ORGUser orgUser = orgUserRepository.getByKeys(conn, new String[] { "org_id", "user_id" },
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

	public ORGLoginBo areaAdminLoginInORG(DruidPooledConnection conn, Long userId, Long orgId) throws Exception {
		// TODO xxx
		return null;
	}

}
