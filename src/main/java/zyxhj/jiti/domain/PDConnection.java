package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 流程定义关联表
 * @author JXians
 *
 */
@RDSAnnEntity(alias = "tb_ecm_pd_connection")
public class PDConnection {
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;
	

	//组织编号
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;
	
	//流程定义编号
	@RDSAnnField(column = RDSAnnField.ID)
	public Long pdId;
	
	//备注
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;
	
}
