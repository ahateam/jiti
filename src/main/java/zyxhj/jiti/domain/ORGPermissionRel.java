package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织权限关系表
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_permission_relation")
public class ORGPermissionRel {

	/**
	 * 组织id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 角色id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long roleId;

	/**
	 * 权限id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long permissionId;
}
