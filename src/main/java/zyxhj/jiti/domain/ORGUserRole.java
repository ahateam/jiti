package zyxhj.jiti.domain;

import java.util.TreeMap;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织角色
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user_role")
public class ORGUserRole {

	/**
	 * 组织编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 角色编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long roleId;

	/**
	 * 角色名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;

	/////////////////////////////////////
	/////////////////////////////////////
	/////////////////////////////////////

	private static ORGUserRole buildSysRole(Long roleId, String name, String remark) {
		ORGUserRole ret = new ORGUserRole();
		ret.orgId = 100L;// ORG组织间公用的系统权限，跨组织存在，默认orgId填写100
		ret.roleId = roleId;
		ret.name = name;
		ret.remark = remark;

		return ret;
	}

	private static Long temp = 100L;// 自增编号

	public static final ORGUserRole role_outuser = buildSysRole(temp++, "外部人员", null);// 非本组织成员100
	public static final ORGUserRole role_user = buildSysRole(temp++, "组织成员", null);//101
	public static final ORGUserRole role_admin = buildSysRole(temp++, "管理员", null);
	public static final ORGUserRole role_shareHolder = buildSysRole(temp++, "股东", null);
	public static final ORGUserRole role_shareDeputy = buildSysRole(temp++, "股东代表", null);
	public static final ORGUserRole role_shareFamily = buildSysRole(temp++, "股东户代表", null);

	public static final ORGUserRole role_director = buildSysRole(temp++, "董事", null);
	public static final ORGUserRole role_dirChief = buildSysRole(temp++, "董事长", null);
	public static final ORGUserRole role_dirVice = buildSysRole(temp++, "副董事长", null);

	public static final ORGUserRole role_supervisor = buildSysRole(temp++, "监事", null);
	public static final ORGUserRole role_supChief = buildSysRole(temp++, "监事长", null);
	public static final ORGUserRole role_supVice = buildSysRole(temp++, "副监事长", null);

	public static final ORGUserRole role_Administractive_admin = buildSysRole(temp++, "行政机构管理员", null);
	public static final ORGUserRole role_bank_admin = buildSysRole(temp++, "银行管理员", null);

	/**
	 * 系统级第三方权限，会被
	 */
	public static TreeMap<Long, ORGUserRole> SYS_ORG_USER_ROLE_MAP = new TreeMap<>();

	static {
		// 添加admin，member，股东，董事，监事等角色到系统中
//		SYS_ORG_USER_ROLE_MAP.put(role_outuser.roleId, role_outuser);
//		SYS_ORG_USER_ROLE_MAP.put(role_user.roleId, role_user);
		SYS_ORG_USER_ROLE_MAP.put(role_admin.roleId, role_admin);
		SYS_ORG_USER_ROLE_MAP.put(role_shareHolder.roleId, role_shareHolder);
		SYS_ORG_USER_ROLE_MAP.put(role_shareDeputy.roleId, role_shareDeputy);
		SYS_ORG_USER_ROLE_MAP.put(role_shareFamily.roleId, role_shareFamily);

		SYS_ORG_USER_ROLE_MAP.put(role_director.roleId, role_director);
		SYS_ORG_USER_ROLE_MAP.put(role_dirChief.roleId, role_dirChief);
		SYS_ORG_USER_ROLE_MAP.put(role_dirVice.roleId, role_dirVice);

		SYS_ORG_USER_ROLE_MAP.put(role_supervisor.roleId, role_supervisor);
		SYS_ORG_USER_ROLE_MAP.put(role_supChief.roleId, role_supChief);
		SYS_ORG_USER_ROLE_MAP.put(role_supVice.roleId, role_supVice);

		SYS_ORG_USER_ROLE_MAP.put(role_Administractive_admin.roleId, role_Administractive_admin);
		SYS_ORG_USER_ROLE_MAP.put(role_bank_admin.roleId, role_bank_admin);
	}

}
