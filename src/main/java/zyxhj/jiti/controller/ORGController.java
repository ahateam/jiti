package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserGroupService;
import zyxhj.jiti.service.ORGUserRoleService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;

public class ORGController extends Controller {

	private static Logger log = LoggerFactory.getLogger(ORGController.class);

	private DataSource dsRds;
	private ORGService orgService;
	private ORGUserService orgUserService;
	private ORGUserGroupService orgUserGroupService;

	public ORGController(String node) {
		super(node);
		try {
			dsRds = DataSourceUtils.getDataSource("rdsDefault");

			orgService = Singleton.ins(ORGService.class);
			orgUserService = Singleton.ins(ORGUserService.class);

			orgUserGroupService = Singleton.ins(ORGUserGroupService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(path = "registeUser", //
			des = "创建普通用户", //
			ret = "LoginBo"//
	)
	public APIResponse registeUser(//
			@P(t = "手机号") String mobile, //
			@P(t = "姓名（实名）") String realName, //
			@P(t = "身份证号") String idNumber, //

			@P(t = "密码") String pwd //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgService.registeUser(conn, mobile, pwd, realName, idNumber));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(path = "createORG", //
			des = "创建组织", //
			ret = "所创建的对象"//
	)
	public APIResponse createORG(//
			@P(t = "创建者用户编号") Long userId, //
			@P(t = "组织名称") String name, //
			@P(t = "组织机构代码") String code, //
			@P(t = "省") String province, //
			@P(t = "市") String city, //
			@P(t = "区") String district, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数") Integer shareAmount//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgService.createORG(conn, userId, name, code, province, city,
					district, address, imgOrg, imgAuth, shareAmount));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "setORG", //
			des = "更新组织", //
			ret = "所更新的对象"//
	)
	public APIResponse setORG(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "省") String province, //
			@P(t = "市") String city, //
			@P(t = "区") String district, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数") Integer shareAmount//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			orgService.editORG(conn, orgId, province, city, district, address, imgOrg, imgAuth, shareAmount);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editORGExt", //
			des = "更新组织扩展信息", //
			ret = "所更新的对象"//
	)
	public APIResponse editORGExt(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "资金", r = false) Double capital, //
			@P(t = "负债", r = false) Double debt, //
			@P(t = "债权资金", r = false) Double receivables, //
			@P(t = "年毛收入", r = false) Double income, //
			@P(t = "分红", r = false) Double bonus, //
			@P(t = "预算", r = false) Double budget, //
			@P(t = "决算", r = false) Double financialBudget, //
			@P(t = "对外投资", r = false) Double investment, //
			@P(t = "估值", r = false) Double valuation//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			orgService.editORGExt(conn, orgId, capital, debt, receivables, income, bonus, budget, financialBudget,
					investment, valuation);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGs", //
			des = "获取全部组织列表", //
			ret = "组织对象列表"//
	)
	public APIResponse getORGs(//
			Integer count, //
			Integer offset) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getORGs(conn, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGById", //
			des = "获取组织对象", //
			ret = "组织对象"//
	)
	public APIResponse getORGById(//
			@P(t = "组织编号") Long orgId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(orgService.getORGById(conn, orgId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getUserORGs", //
			des = "获取用户的组织列表", //
			ret = "组织对象列表"//
	)
	public APIResponse getUserORGs(//
			@P(t = "用户编号") Long userId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(orgService.getUserORGs(conn, userId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createORGUser", //
			des = "创建组织用户" //
	)
	public APIResponse createORGUser(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "手机号") String mobile, //
			@P(t = "真实姓名") String realName, //
			@P(t = "身份证号") String idNumber, //
			@P(t = "地址") String address, //
			@P(t = "股权证书编号", r = false) String shareCerNo, //
			@P(t = "股权证书图片地址") String shareCerImg, //
			@P(t = "是否持证人") Boolean shareCerHolder, //
			@P(t = "股份数") Integer shareAmount, //
			@P(t = "选举权重") Integer weight, //
			@P(t = "角色（股东，董事长，经理等）") JSONArray roles, //
			@P(t = "分组") JSONArray groups, //
			@P(t = "标签，包含groups,tags,以及其它自定义分组标签列表") JSONObject tags, //
			@P(t = "户序号") String familyNumber, //
			@P(t = "户主名") String familyMaster //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			orgUserService.createORGUser(conn, orgId, mobile, realName, idNumber, address, shareCerNo, shareCerImg,
					shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editUser", //
			des = "更新用户信息", //
			ret = "更新影响记录的行数"//
	)
	public APIResponse editUser(//
			@P(t = "用户编号") Long userId, //
			@P(t = "手机号", r = false) String mobile, //
			@P(t = "真实姓名", r = false) String realName, //
			@P(t = "密码", r = false) String pwd//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			int ret = orgUserService.editUser(conn, userId, mobile, realName, pwd);
			return APIResponse.getNewSuccessResp(ret);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delORGUser", //
			des = "移除组织的用户" //
	)
	public APIResponse delORGUser(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			orgUserService.delORGUser(conn, orgId, userId);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editORGUser", //
			des = "更新组织的用户", //
			ret = "更新影响记录的行数"//
	)
	public APIResponse editORGUser(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "地址") String address, //
			@P(t = "股权证书编号", r = false) String shareCerNo, //
			@P(t = "股权证书图片地址") String shareCerImg, //
			@P(t = "是否持证人") Boolean shareCerHolder, //
			@P(t = "股份数") Integer shareAmount, //
			@P(t = "选举权重") Integer weight, //
			@P(t = "角色（股东，董事长，经理等）") JSONArray roles, //
			@P(t = "分组") JSONArray groups, //
			@P(t = "标签，包含groups,tags,以及其它自定义分组标签列表") JSONObject tags, //
			@P(t = "户序号") String familyNumber, //
			@P(t = "户主名") String familyMaster //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			int ret = orgUserService.editORGUser(conn, orgId, userId, address, shareCerNo, shareCerImg, shareCerHolder,
					shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
			return APIResponse.getNewSuccessResp(ret);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "batchEditORGUsersGroups", //
			des = "获取组织的用户" //
	)
	public APIResponse batchEditORGUsersGroups(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号列表，JSONArray格式") JSONArray userIds, //
			@P(t = "分组信息列表，JSONArray格式") JSONArray groups//

	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {

			return APIResponse.getNewSuccessResp(orgUserService.batchEditORGUsersGroups(conn, orgId, userIds, groups));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUserById", //
			des = "获取组织的用户" //
	)
	public APIResponse getORGUserById(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {

			return APIResponse.getNewSuccessResp(orgUserService.getORGUserById(conn, orgId, userId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "loginByMobileAndPwd", //
			des = "手机号密码登录", //
			ret = "LoginBO对象，包含user，session等信息"//
	)
	public APIResponse loginByMobileAndPwd(//
			@P(t = "手机号") String mobile, //
			@P(t = "密码") String pwd//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(orgService.loginByMobile(conn, mobile, pwd)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "loginByUserId", //
			des = "用户编号和密码登录", //
			ret = "LoginBO对象，包含user，session等信息"//
	)
	public APIResponse loginByUserId(//
			@P(t = "手机号") Long userId, //
			@P(t = "密码") String pwd//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(orgService.loginByUserId(conn, userId, pwd)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "loginInORG", //
			des = "登录到组织", //
			ret = "ORGLoginBo对象，包含user，session及org等信息"//
	)
	public APIResponse loginInORG(//
			@P(t = "用户编号") Long userId, //
			@P(t = "组织编号") Long orgId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(orgService.loginInORG(conn, userId, orgId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "adminLoginInORG", //
			des = "组织管理员登录到组织", //
			ret = "ORGLoginBo对象，包含user，session及org等信息"//
	)
	public APIResponse adminLoginInORG(//
			@P(t = "用户编号") Long userId, //
			@P(t = "组织编号") Long orgId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.adminLoginInORG(conn, userId, orgId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUsers", //
			des = "获取组织成员列表", //
			ret = "成员列表"//
	)
	public APIResponse getORGUsers(//
			@P(t = "组织编号") Long orgId, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getORGUsers(conn, orgId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUserByRole", //
			des = "根据角色信息获取组织成员列表", //
			ret = "成员列表"//
	)
	public APIResponse getORGUserByRole(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色权限列表,JSONArray格式", r = false) JSONArray roles, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getORGUsersByRoles(conn, orgId, roles, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUsersByGroups", //
			des = "根据分组信息获取组织成员列表", //
			ret = "成员列表"//
	)
	public APIResponse getORGUsersByGroups(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色分组,JSONArray格式", r = false) JSONArray groups, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(orgUserService.getORGUsersByGroups(conn, orgId, groups, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUsersByTags", //
			des = "根据标签信息获取组织成员列表", //
			ret = "成员列表"//
	)
	public APIResponse getORGUsersByTags(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色标签对象（默认包含groups,tags）,JSONObject格式", r = false) JSONObject tags, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getORGUsersByTags(conn, orgId, tags, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUsersLikeIDNumber", //
			des = "根据组织编号和身份证号片段（生日），模糊查询", //
			ret = "User列表"//
	)
	public APIResponse getORGUsersLikeIDNumber(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "身份证编号（片段即可），模糊查询") String idNumber, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(orgUserService.getORGUsersLikeIDNumber(conn, orgId, idNumber, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUsersLikeRealName", //
			des = "根据组织编号和用户真名片段，模糊查询", //
			ret = "User列表"//
	)
	public APIResponse getORGUsersLikeRealName(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户名（片段即可），模糊查询") String realName, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(orgUserService.getORGUsersLikeRealName(conn, orgId, realName, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "importORGUsers", //
			des = "导入组织用户列表" //
	)
	public APIResponse importORGUsers(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "excel文件url") String url//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			orgUserService.importORGUsers(conn, orgId, url);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getUsersByMobile", //
			des = "根据手机号获取用户信息列表", //
			ret = "User列表"//
	)
	public APIResponse getUsersByMobile(//
			@P(t = "手机号") String mobile, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getUsersByMobile(conn, mobile, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getSysORGUserRoles", //
			des = "获取组织系统用户角色列表", //
			ret = "UserRole列表"//
	)
	public APIResponse getSysORGUserRoles() throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ORGUserRoleService.SYS_ORG_USER_ROLE_LIST);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGUserSysTagGroups", //
			des = "获取组织系统用户标签分组列表", //
			ret = "UserTagGroup列表"//
	)
	public APIResponse getORGUserSysTagGroups() throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ORGUserGroupService.SYS_ORG_USER_TAG_GROUP_LIST);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createORGUserTagGroup", //
			des = "创建组织用户标签分组" //
	)
	public APIResponse createORGUserTagGroup(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "父节点编号") Long parentId, //
			@P(t = "分组父节点编号数组，JSONArray格式。节点顺序自顶向下") JSONArray parents, //
			@P(t = "分组关键字") String keyword, //
			String remark //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					orgUserGroupService.createTagGroup(conn, orgId, parentId, parents, keyword, remark));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editORGUserTagGroup", //
			des = "编辑组织用户标签分组" //
	)
	public APIResponse editORGUserTagGroup(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "分组编号") Long groupId, //
			@P(t = "父节点编号") Long parentId, //
			@P(t = "分组父节点编号数组，JSONArray格式。节点顺序自顶向下") JSONArray parents, //
			@P(t = "分组关键字") String keyword, //
			String remark //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					orgUserGroupService.editTagGroup(conn, orgId, groupId, parentId, parents, keyword, remark));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delORGUserTagGroupById", //
			des = "删除组织用户标签分组" //
	)
	public APIResponse delORGUserTagGroupById(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "分组编号") Long groupId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgUserGroupService.delTagGroupById(conn, groupId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getTagGroupTree", //
			des = "获取组织用户标签分组树", //
			ret = "标签分组树，JSONObject格式")
	public APIResponse getTagGroupTree(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "分组编号") Long groupId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(orgUserGroupService.getTagGroupTree(conn, orgId, groupId));
		}
	}

}
