package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织权限表（静态、可添加其他权限）
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_permission")
public class ORGPermission {

	private static ORGPermission buildSysPermission(Long permissionId, String permissionName, String remark) {
		ORGPermission per = new ORGPermission();
		per.id = permissionId;
		per.name = permissionName;
		per.remark = remark;
		return per;
	}

	public static Long temp = 100L;

	public static final ORGPermission per_initiate_vote = buildSysPermission(temp++, "发起投票", "拥有发起投票的权限");
	public static final ORGPermission per_asset_management = buildSysPermission(temp++, "资产管理", "管理资产权限");
	public static final ORGPermission per_role_management = buildSysPermission(temp++, "职务管理", "管理职务权限");
	public static final ORGPermission per_examine = buildSysPermission(temp++, "审核", "上级审核权限");
	public static final ORGPermission per_feparate_family = buildSysPermission(temp++, "分户", "上级审核分户权限");
	public static final ORGPermission per_share_change = buildSysPermission(temp++, "股权变更", "上级审核股权变更权限");

	/**
	 * 权限id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	/**
	 * 权限名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	/**
	 * 备注
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;

}
