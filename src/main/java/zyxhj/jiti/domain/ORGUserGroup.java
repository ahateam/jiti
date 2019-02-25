package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织用户分组
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user_group")
public class ORGUserGroup {

	/**
	 * 第三方编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 分组编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long groupId;

	/**
	 * 分组名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;

}
