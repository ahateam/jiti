package zyxhj.jiti.controller;

import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.User;
import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.domain.Notice;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGExamine;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.service.MessageService;
import zyxhj.jiti.service.ORGPermissionService;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserGroupService;
import zyxhj.jiti.service.ORGUserRoleService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class ORGController extends Controller {

	private static Logger log = LoggerFactory.getLogger(ORGController.class);

	private DruidDataSource dds;
	private ORGService orgService;
	private ORGUserService orgUserService;
	private ORGUserGroupService orgUserGroupService;
	private ORGPermissionService orgPermissionService;
	private ORGUserRoleService orgUserRoleService;
	private MessageService messageService;

	public ORGController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

			orgService = Singleton.ins(ORGService.class);
			orgUserService = Singleton.ins(ORGUserService.class);

			orgUserGroupService = Singleton.ins(ORGUserGroupService.class);
			orgPermissionService = Singleton.ins(ORGPermissionService.class);
			orgUserRoleService = Singleton.ins(ORGUserRoleService.class);
			messageService = Singleton.ins(MessageService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@ENUM(des = "投票状态")
	public ORGExamine.STATUS[] voteStatus = ORGExamine.STATUS.values();

	@ENUM(des = "类型")
	public ORG.TYPE[] orgType = ORG.TYPE.values();

	@ENUM(des = "等级划分")
	public ORG.LEVEL[] orgLevel = ORG.LEVEL.values();

	@ENUM(des = "审批类型")
	public Examine.TYPE[] exType = Examine.TYPE.values();

	@ENUM(des = "审批状态")
	public Examine.STATUS[] exStatus = Examine.STATUS.values();

	@ENUM(des = "户操作")
	public Examine.OPERATE[] operate = Examine.OPERATE.values();

	@ENUM(des = "用户标记")
	public Examine.TAB[] tab = Examine.TAB.values();

	@ENUM(des = "公告类型")
	public Notice.TYPE[] type = Notice.TYPE.values();

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
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.registeUser(conn, mobile, pwd, realName, idNumber));
		}
	}

	// /**
	// *
	// */
	// @POSTAPI(path = "createORG", //
	// des = "创建组织", //
	// ret = "所创建的对象"//
	// )
	// public APIResponse createORG(//
	// @P(t = "创建者用户编号") Long userId, //
	// @P(t = "组织名称") String name, //
	// @P(t = "组织机构代码") String code, //
	// @P(t = "省") String province, //
	// @P(t = "市") String city, //
	// @P(t = "区") String district, //
	// @P(t = "街道地址") String address, //
	// @P(t = "组织机构证书图片地址", r = false) String imgOrg, //
	// @P(t = "组织授权证书图片地址", r = false) String imgAuth, //
	// @P(t = "总股份数") Integer shareAmount//
	// ) throws Exception {
	// try (DruidPooledConnection conn = (DruidPooledConnection)
	// dsRds.openConnection()) {
	// return APIResponse.getNewSuccessResp(orgService.createORG(conn, userId, name,
	// code, province, city,
	// district, address, imgOrg, imgAuth, shareAmount));
	// }
	// }

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
			@P(t = "组织名称") String orgName, //
			@P(t = "组织机构代码", r = false) String code, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数") Integer shareAmount//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			orgService.editORG(conn, code, orgName, orgId, address, imgOrg, imgAuth, shareAmount);
			return APIResponse.getNewSuccessResp();
		}
	}

	@POSTAPI(path = "createSubOrg", //
			des = "创建下级组织机构" //
	)
	public void createSubOrg(//
			@P(t = "组织名称") String name, //
			@P(t = "组织机构代码") String code, //
			@P(t = "省", r = false) Long province, //
			@P(t = "市", r = false) Long city, //
			@P(t = "区", r = false) Long district, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数", r = false) Integer shareAmount, //
			@P(t = "等级") Byte level, //
			@P(t = "上级组织id", r = false) Long superiorId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			orgService.createSubOrg(conn, name, code, address, imgOrg, imgAuth, level, shareAmount, superiorId,
					province, city, district);
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
			des = "获取下级组织列表", //
			ret = "组织对象列表"//
	)
	public APIResponse getORGs(//
			@P(t = "上级编号") Long superiorId, //
			Integer count, //
			Integer offset) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getORGs(conn, superiorId, count, offset));
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
			@P(t = "用户编号") Long userId, //
			@P(t = "等级") Byte level, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(orgService.getUserORGs(conn, userId, level, count, offset)));
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
			@P(t = "股份数") Double shareAmount, //
			@P(t = "选举权重") Integer weight, //
			@P(t = "角色（股东，董事长，经理等）") JSONArray roles, //
			@P(t = "分组") JSONArray groups, //
			@P(t = "标签，包含groups,tags,以及其它自定义分组标签列表") JSONObject tags, //
			@P(t = "户序号") Long familyNumber, //
			@P(t = "户主名") String familyMaster //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
			int ret = orgUserService.editUser(conn, userId, mobile, realName, pwd);
			return APIResponse.getNewSuccessResp(ret);
		}
	}
	
	/**
	 * 
	 */
	@POSTAPI(//
			path = "editUserMobile", //
			des = "用户绑定/解绑手机号", //
			ret = "更新影响记录的行数"//
	)
	public APIResponse editUserMobile(
			@P(t = "用户编号") Long userId, //
			@P(t = "手机号,当手机号为null时为解除绑定", r = false) String mobile //
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			User u = orgUserService.editUserMobile(conn, userId, mobile);
			if(u!=null) {
				return APIResponse.getNewSuccessResp(u);
			}
			return APIResponse.getNewSuccessResp(0);
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
			@P(t = "是否持证人", r = false) Boolean shareCerHolder, //
			@P(t = "股份数", r = false) Double shareAmount, //
			@P(t = "选举权重") Integer weight, //
			@P(t = "角色（股东，董事长，经理等）") JSONArray roles, //
			@P(t = "分组") JSONArray groups, //
			@P(t = "标签，包含groups,tags,以及其它自定义分组标签列表") JSONObject tags, //
			@P(t = "户序号", r = false) Long familyNumber, //
			@P(t = "户主名", r = false) String familyMaster //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.editORGUser(conn, orgId, userId, address, shareCerNo,
					shareCerImg, shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber, familyMaster));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "batchEditORGUsersGroups", //
			des = "修改组织用户的分组" //
	)
	public APIResponse batchEditORGUsersGroups(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号列表，JSONArray格式") JSONArray userIds, //
			@P(t = "分组信息列表 长整形") Long groups//

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			System.out.println("123456");
			orgUserService.batchEditORGUsersGroups(conn, orgId, userIds, groups);
			return APIResponse.getNewSuccessResp();
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
		try (DruidPooledConnection conn = dds.getConnection()) {

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
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(orgService.loginByMobile(conn, mobile, pwd)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "loginByIdNumber", //
			des = "身份证号密码登录", //
			ret = "LoginBO对象，包含user，session等信息"//
	)
	public APIResponse loginByIdNumber(//
			@P(t = "身份证号") String idNumber, //
			@P(t = "密码") String pwd//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.loginByIdNumber(conn, idNumber, pwd)));
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.adminLoginInORG(conn, userId, orgId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "areaAdminLoginInORG", //
			des = "行政机构管理员登陆", //
			ret = "ORGLoginBo对象，包含user，session及org等信息"//
	)
	public APIResponse areaAdminLoginInORG(//
			@P(t = "用户编号") Long userId, //
			@P(t = "组织编号") Long orgId//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.areaAdminLoginInORG(conn, userId, orgId)));
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
			@P(t = "角色权限列表,String[]格式 String[1,2,3]", r = false) String[] roles, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
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
			@P(t = "角色分组,int[]格式", r = false) String[] groups, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(new ArrayList<>(ORGUserRole.SYS_ORG_USER_ROLE_MAP.values()));
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
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
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserGroupService.getTagGroupTree(conn, orgId, groupId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(path = "createORGApply", //
			des = "创建组织申请", //
			ret = "所创建的对象"//
	)
	public APIResponse createORGApply(//
			@P(t = "创建者用户编号") Long userId, //
			@P(t = "组织名称") String name, //
			@P(t = "组织机构代码") String code, //
			@P(t = "省", r = false) Long province, //
			@P(t = "市", r = false) Long city, //
			@P(t = "区", r = false) Long district, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数") Integer shareAmount, //
			@P(t = "等级", r = false) Byte level, //
			@P(t = "上级组织id", r = false) Long superiorId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.createORGApply(conn, userId, name, code, province, city,
					district, address, imgOrg, imgAuth, shareAmount, level, superiorId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(path = "upORGApply", //
			des = "修改组织申请", //
			ret = "所创建的对象"//
	)
	public APIResponse upORGApply(//
			@P(t = "组织id") Long orgExamineId, //
			@P(t = "创建者用户编号") Long userId, //
			@P(t = "组织名称") String name, //
			@P(t = "组织机构代码") String code, //
			@P(t = "省", r = false) Long province, //
			@P(t = "市", r = false) Long city, //
			@P(t = "区", r = false) Long district, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数", r = false) Integer shareAmount, //
			@P(t = "申请状态") Byte examine, //
			@P(t = "等级") Byte level, //
			@P(t = "上级组织id", r = false) Long superiorId, //
			@P(t = "组织id") Boolean updateDistrict //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgService.upORGApply(conn, orgExamineId, examine, userId, name, code, province,
							city, district, address, imgOrg, imgAuth, shareAmount, level, superiorId, updateDistrict));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(path = "oRGApplyAgain", //
			des = "修改组织申请", //
			ret = "所创建的对象"//
	)
	public APIResponse oRGApplyAgain(//
			@P(t = "组织申请id") Long orgExamineId, //
			@P(t = "创建者用户编号") Long userId, //
			@P(t = "组织名称") String name, //
			@P(t = "组织机构代码") String code, //
			@P(t = "省", r = false) Long province, //
			@P(t = "市", r = false) Long city, //
			@P(t = "区", r = false) Long district, //
			@P(t = "街道地址") String address, //
			@P(t = "组织机构证书图片地址", r = false) String imgOrg, //
			@P(t = "组织授权证书图片地址", r = false) String imgAuth, //
			@P(t = "总股份数", r = false) Integer shareAmount, //
			@P(t = "等级") Byte level, //
			@P(t = "上级组织id", r = false) Long superiorId, //
			@P(t = "组织id", r = false) Long orgId, //
			@P(t = "是否修改地址") Boolean updateDistrict //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					orgService.upORGApplyAgain(conn, orgExamineId, userId, name, code, province, city, district,
							address, imgOrg, imgAuth, shareAmount, level, superiorId, orgId, updateDistrict));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGExamine", //
			des = "查询组织申请列表", //
			ret = "返回查询值")
	public APIResponse getORGExamine(//
			@P(t = "区编号") Long areaId, //
			@P(t = "状态") Byte examine, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgService.getORGExamineByStatus(conn, examine, areaId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGExamineByUser", //
			des = "查看用户自己的申请", //
			ret = "返回查询值")
	public APIResponse getORGExamineByUser(//
			@P(t = "用户编号") Long userId, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getORGExamineByUser(conn, userId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delORGExamine", //
			des = "删除申请", //
			ret = "")
	public APIResponse delORGExamine(//
			@P(t = "申请编号") Long examineId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.delORGExamine(conn, examineId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "countRole", //
			des = "统计组织角色", //
			ret = "返回角色统计值")
	public APIResponse countRole(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色编号  使用JSONArray数组存") JSONArray roles //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.countRole(conn, orgId, roles));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createFamily", //
			des = "添加户", //
			ret = "返回添加信息")
	public APIResponse createFamily(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "户序号") Long familyNumber, //
			@P(t = "户主名") String familyMaster //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.createFamily(conn, orgId, familyNumber, familyMaster));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editFamily", //
			des = "修改户", //
			ret = "返回修改信息")
	public APIResponse editFamily(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "户序号") Long familyNumber, //
			@P(t = "户主名") String familyMaster //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.editFamily(conn, orgId, familyNumber, familyMaster));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getProCityDistrict", //
			des = "查询省、市、区", //
			ret = "返回查询信息")
	public APIResponse getProCityDistrict(//
			@P(t = "等级") Byte level, //
			@P(t = "父id") Long father, //
			Integer count, //
			Integer offset

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getProCityDistrict(conn, level, father, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getOrgByNameAndLevel", //
			des = "模糊查询机构", //
			ret = "返回查询信息")
	public APIResponse getOrgByNameAndLevel(//
			@P(t = "等级") Byte level, //
			@P(t = "需要查询的名称") String orgName //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getOrgByNameAndLevel(conn, level, orgName));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGDistrict", //
			des = "查询组织地址", //
			ret = "返回查询信息")
	public APIResponse getORGDistrict(//
			@P(t = "组织id") Long orgId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getORGDistrict(conn, orgId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGDistrictByOrgApplyId", //
			des = "查询申请地址", //
			ret = "返回查询信息")
	public APIResponse getORGDistrictByOrgApplyId(//
			@P(t = "组织id") Long orgExamineId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getORGDistrictByOrgApplyId(conn, orgExamineId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getSuperior", //
			des = "获取上级id", //
			ret = "返回查询信息")
	public APIResponse getSuperior(//
			@P(t = "组织id") Long orgId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getSuperior(conn, orgId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPermission", //
			des = "获取权限列表", //
			ret = "返回权限列表")
	public APIResponse getPermission(//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(ORGPermissionService.SYS_ORG_PERMISSION_LIST);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getRolesByPermission", //
			des = "获取某个权限下的角色列表", //
			ret = "返回角色列表")
	public APIResponse getRolesByPermission(//
			@P(t = "组织id") Long orgId, //
			@P(t = "权限id") Long permissionId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserRoleService.getRolesByPermission(conn, orgId, permissionId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPermissionsByRole", //
			des = "获取某个角色下的权限列表", //
			ret = "返回权限列表")
	public APIResponse getPermissionsByRole(//
			@P(t = "组织id") Long orgId, //
			@P(t = "角色id") String roleId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgPermissionService.getPermissionsByRole(conn, orgId, roleId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "insertPermissionRole", //
			des = "给某个权限添加角色", //
			ret = "返回影响记录数")
	public APIResponse insertPermissionRole(//
			@P(t = "组织id") Long orgId, //
			@P(t = "权限id") Long permissionId, //
			@P(t = "角色id String[] 例[1,2,3,4]") String role //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgPermissionService.insertPermissionRole(conn, orgId, permissionId, role));
		}
	}

//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "createORGUserImportTask", //
//			des = "创建组织用户导入任务", //
//			ret = ""//
//	)
//	public APIResponse createORGUserImportTask(//
//			@P(t = "组织id") Long orgId, //
//			@P(t = "用户id") Long userId, //
//			@P(t = "任务名称") String name //
//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			orgUserService.createORGUserImportTask(conn, orgId, userId, name);
//			return APIResponse.getNewSuccessResp();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getORGUserImportTasks", //
//			des = "获取组织用户导入任务", //
//			ret = ""//
//	)
//	public APIResponse getORGUserImportTasks(//
//			@P(t = "组织id") Long orgId, //
//			@P(t = "用户id") Long userId, //
//			Integer count, //
//			Integer offset//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			return APIResponse
//					.getNewSuccessResp(orgUserService.getORGUserImportTasks(conn, orgId, userId, count, offset));
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getORGUserImportTask", //
//			des = "获取当前导入任务信息", //
//			ret = ""//
//	)
//	public APIResponse getORGUserImportTask(//
//			@P(t = "组织id") Long orgId, //
//			@P(t = "用户id") Long userId, //
//			@P(t = "导入任务id") Long importTaskId//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			return APIResponse
//					.getNewSuccessResp(orgUserService.getORGUserImportTask(conn, importTaskId, orgId, userId));
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "importORGUserRecord", //
//			des = "导入组织用户列表" //
//	)
//	public APIResponse importORGUserRecord(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "用户编号") Long userId, //
//			@P(t = "excel文件url") String url, //
//			@P(t = "导入任务id") Long importTaskId//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			orgUserService.importORGUserRecord(conn, orgId, userId, url, importTaskId);
//			return APIResponse.getNewSuccessResp();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getORGUserImportRecords", //
//			des = "获取导入组织用户列表", //
//			ret = "需导入的组织用户列表")
//	public APIResponse getORGUserImportRecords(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "导入任务id") Long importTaskId, //
//			Integer count, //
//			Integer offset //
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			return APIResponse.getNewSuccessResp(
//					orgUserService.getORGUserImportRecords(conn, orgId, importTaskId, count, offset));
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "importORGUser", //
//			des = "开始导入组织用户列表", //
//			ret = "")
//	public APIResponse importORGUser(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "导入任务id") Long importTaskId //
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			orgUserService.importORGUser(orgId, importTaskId);
//			return APIResponse.getNewSuccessResp();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getNotcompletionRecord", //
//			des = "获取导入失败的组织用户", //
//			ret = ""//
//	)
//	public APIResponse getNotcompletionRecord(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "导入任务id") Long importTaskId, //
//			Integer count, //
//			Integer offset //
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			return APIResponse
//					.getNewSuccessResp(orgUserService.getNotcompletionRecord(conn, orgId, importTaskId, count, offset));
//		}
//	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getSubORGUser", //
			des = "获取组织管理员信息", //
			ret = ""//
	)
	public APIResponse getSubORGUser(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "组织级别") Byte level, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getSubORGUser(conn, orgId, level, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createORGAdmin", //
			des = "创建组织管理员", //
			ret = ""//
	)

	public APIResponse createORGAdmin(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "组织等级 1 2 3 行政管理员  4 合作社管理员") Byte level, //
			@P(t = "身份证号码") String idNumber, //
			@P(t = "电话号码") String mobile, //
			@P(t = "真实姓名") String realName //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			orgUserService.createORGAdmin(conn, orgId, level, idNumber, mobile, realName);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delORGUserAdmin", //
			des = "获取组织管理员信息", //
			ret = ""//
	)
	public APIResponse delORGUserAdmin(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "组织编号") Long userId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.delORGUserAdmin(conn, orgId, userId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editORGAdmin", //
			des = "修改组织管理员信息", //
			ret = ""//
	)
	public APIResponse editORGAdmin(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "身份证号码") String idNumber, //
			@P(t = "电话号码") String mobile, //
			@P(t = "真实姓名") String realName //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse
					.getNewSuccessResp(orgUserService.editORGAdmin(conn, orgId, userId, idNumber, mobile, realName));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "addNoticeTask", //
			des = "创建通知任务", //
			ret = "返回任务信息"//
	)
	public APIResponse addNoticeTask(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "任务名称") String taskName, //
			@P(t = "通知类容") String remark, //
			@P(t = "需要发送的人群  {roles:{[xxx,xxx,xx]}}") JSONObject crowd //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgService.addNoticeTask(conn, orgId, userId, taskName, remark, crowd));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getFamilyAll", //
			des = "所有户列表", //
			ret = "返回查询信息")
	public APIResponse getFamilyAll(//
			@P(t = "组织编号", r = false) Long orgId, //
			@P(t = "查询数", r = false) Integer count, //
			@P(t = "开始位置", r = false) Integer offset) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getFamilyAll(conn, orgId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getFamilyByFamilyMaster", //
			des = "根据户主名查询户列表", //
			ret = "返回查询信息"//
	)
	public APIResponse getFamilyByFamilyMaster(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "查询的名称") String content, //
			Integer count, //
			Integer offset //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgUserService.getFamilyByFamilyMaster(conn, orgId, content, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getFamilyByshare", //
			des = "根据股权证号查询户", //
			ret = "返回查询信息"//
	)
	public APIResponse getFamilyByshare(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "查询的名称") String content, //
			Integer count, //
			Integer offset //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getFamilyByshare(conn, orgId, content, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getFamilyByFamilyNumber", //
			des = "根据户序号查询户列表", //
			ret = "返回查询信息"//
	)
	public APIResponse getFamilyByFamilyNumber(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "户序号") Long content, //
			Integer count, //
			Integer offset //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgUserService.getFamilyByFamilyNumber(conn, orgId, content, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getFamilyUserByFamilyNumber", //
			des = "根据户序号查询户下成员列表", //
			ret = "返回查询信息"//
	)
	public APIResponse getFamilyUserByFamilyNumber(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "户序号") Long familyNumber //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getFamilyUserByFamilyNumber(conn, orgId, familyNumber));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createExamine", //
			des = "创建审核", //
			ret = "返回创建信息"//
	)
	public APIResponse createExamine(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "修改数据  {oldData:[....],newData:[.....]}") String data, //
			@P(t = "审核类型") Byte type, //
			@P(t = "备注", r = false) String remark //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.createExamine(conn, orgId, data, type, remark));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getExamine", //
			des = "获取审核", //
			ret = "返回审核表信息"//
	)
	public APIResponse getExamine(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "类型") Byte type, //
			@P(t = "状态") Byte status, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getExamine(conn, orgId, type, status, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editExamine", //
			des = "审核", //
			ret = ""//
	)
	public APIResponse editExamine(//
			@P(t = "任务编号") Long examineId, //
			@P(t = "组织编号") Long orgId, //
			@P(t = "状态") Byte status //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.editExamine(conn, examineId, orgId, status));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getExamineByDisId", //
			des = "区级请求本组织下其他组织提交的审核", //
			ret = "未审核列表"//
	)
	public APIResponse getExamineByDisId(//
			@P(t = "任务编号") Long districtId, //
			@P(t = "类型") Byte type, //
			@P(t = "状态") Byte status, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgUserService.getExamineByDisId(conn, districtId, type, status, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "setShareCerNo", //
			des = "设置股权证号", //
			ret = ""//
	)
	public APIResponse setShareCerNo(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "任务编号") Long examineId, //
			@P(t = "户序号") Long familyNumber, //
			@P(t = "股权证号") String shareCerNo //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			orgUserService.setShareCerNo(conn, orgId, examineId, familyNumber, shareCerNo);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editExamineStatus", //
			des = "设置审核状态", //
			ret = ""//
	)
	public APIResponse editExamineStatus(//
			@P(t = "任务编号") Long examineId, //
			@P(t = "状态") Byte status //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			orgUserService.editExamineStatus(conn, examineId, status);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delExamine", //
			des = "删除审核", //
			ret = "返回1表示删除成功   返回0表示删除失败"//
	)
	public APIResponse delExamine(//
			@P(t = "任务编号") Long examineId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(orgUserService.delExamine(conn, examineId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getFamilyByshareCerNo", //
			des = "查询股权证号是否已经存在", //
			ret = "0表示股权证号不存在  1表示已经存在"//
	)
	public APIResponse getFamilyByshareCerNo(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "股权证号") String shareCerNo //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getFamilyByshareCerNo(conn, orgId, shareCerNo));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "examineShareCerNo", //
			des = "股权证号审核", //
			ret = "返回审核状态"//
	)
	public APIResponse examineShareCerNo(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "任务编号") Long examineId, //
			@P(t = "状态") Byte status //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(orgUserService.examineShareCerNo(conn, orgId, examineId, status));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createNotice", //
			des = "创建公告", //
			ret = "返回创建内容"//
	)
	public APIResponse createNotice(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "公告名称") String noticeTitle, //
			@P(t = "公告内容") String noticeContent, //
			@P(t = "类型") Byte type, //
			@P(t = "人群  例:{orgId:[],roles:[],groups:[]}") String crowd //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(orgService.createNotice(conn, orgId, noticeTitle, noticeContent, type, crowd));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editNotice", //
			des = "修改公告", //
			ret = "返回修改内容"//
	)
	public APIResponse editNotice(//
			@P(t = "组织编号") Long noticeId, //
			@P(t = "组织编号") Long orgId, //
			@P(t = "公告名称") String noticeTitle, //
			@P(t = "公告内容") String noticeContent, //
			@P(t = "类型") Byte type, //
			@P(t = "人群  例:{orgId:[],roles:[],groups:[]}") String crowd //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					orgService.editNotice(conn, noticeId, orgId, noticeTitle, noticeContent, type, crowd));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getNotice", //
			des = "查询公告", //
			ret = "返回查询内容"//
	)
	public APIResponse getNotice(//
			@P(t = "组织编号") Long orgId, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getNotice(conn, orgId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "deleteNotice", //
			des = "删除公告", //
			ret = ""//
	)
	public APIResponse deleteNotice(//
			@P(t = "组织编号") Long noticeId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.deleteNotice(conn, noticeId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getNoticeByRoleGroup", //
			des = "用户获取公告信息", //
			ret = "返回公告信息"//
	)
	public APIResponse getNoticeByRoleGroup(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色编号 [102,103,104]") String roles, //
			@P(t = "分组编号 [1111111,555555,111]") String groups //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.getNoticeByRoleGroup(conn, orgId, roles, groups));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getOrgUser", //
			des = "查看当前用户是否已经在此组织下", //
			ret = "0表示不存在 1表示已存在"//
	)
	public APIResponse getOrgUser(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "身份证号码") String idNumber //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgUserService.getOrgUser(conn, orgId, idNumber));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getExamineByPer", //
			des = "根据权限查询审核列表", //
			ret = "审核列表"//
	)
	public APIResponse getExamineByPer(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "用户角色") String permissionIds, //
			@P(t = "查询的审核类型") Byte type, //
			@P(t = "审核状态", r = false) Byte status, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					orgUserService.getExamineByPer(conn, orgId, userId, permissionIds, type, status, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "bdUserOpenId", //
			des = "绑定微信", //
			ret = ""//
	)
	public APIResponse bdUserOpenId(//
			@P(t = "用户编号") Long userId, //
			@P(t = "微信id") String openId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.bdUserOpenId(conn, userId, openId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "loginByOpenId", //
			des = "微信登陆", //
			ret = ""//
	)
	public APIResponse loginByOpenId(//
			@P(t = "微信id") String openId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.loginByOpenId(conn, openId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "removeOpenId", //
			des = "解除绑定", //
			ret = ""//
	)
	public APIResponse removeOpenId(//
			@P(t = "用户id") Long userId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.removeOpenId(conn, userId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getMessageByUserId", //
			des = "获取用户的消息", //
			ret = ""//
	)
	public APIResponse getMessageByUserId(//
			@P(t = "组织id") Long orgId, //
			@P(t = "用户id") Long userId, //
			Integer count, //
			Integer offset//

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(messageService.getMessageByUserId(conn, orgId, userId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "countMessageByUserId", //
			des = "统计用户有多少条消息未读", //
			ret = ""//
	)
	public APIResponse countMessageByUserId(//
			@P(t = "组织id") Long orgId, //
			@P(t = "用户id") Long userId, //
			Integer count, //
			Integer offset//

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(messageService.countMessageByUserId(conn, orgId, userId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editMessageStatus", //
			des = "修改消息状态为已读", //
			ret = ""//
	)
	public APIResponse editMessageStatus(//
			@P(t = "消息id") Long messageId //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			messageService.editMessageStatus(conn, messageId);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delSubOrg", des = "移除下级组织机构", ret = "")
	public APIResponse delSubOrg(@P(t = "组织id") Long orgId) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(orgService.delSubOrg(conn, orgId));
		}
	}

}
