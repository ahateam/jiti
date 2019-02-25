package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织角色
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user_role")
public class ORGUserRole {

	private static ORGUserRole buildSysRole(Long roleId, String name, String remark) {
		ORGUserRole ret = new ORGUserRole();
		ret.orgId = 100L;// ORG组织间公用的系统权限，跨组织存在，默认orgId填写100
		ret.roleId = roleId;
		ret.name = name;
		ret.remark = remark;

		return ret;
	}

	private static Long temp = 100L;// 自增编号

	public static final ORGUserRole role_user = buildSysRole(temp++, "用户", null);
	public static final ORGUserRole role_admin = buildSysRole(temp++, "管理员", null);
	public static final ORGUserRole role_shareHolder = buildSysRole(temp++, "股东", null);
	public static final ORGUserRole role_shareDeputy = buildSysRole(temp++, "股东代表", null);

	public static final ORGUserRole role_director = buildSysRole(temp++, "董事", null);
	public static final ORGUserRole role_dirChief = buildSysRole(temp++, "董事长", null);
	public static final ORGUserRole role_dirVice = buildSysRole(temp++, "副董事长", null);

	public static final ORGUserRole role_supervisor = buildSysRole(temp++, "监事", null);
	public static final ORGUserRole role_supChief = buildSysRole(temp++, "监事长", null);
	public static final ORGUserRole role_supVice = buildSysRole(temp++, "副监事长", null);

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
}
