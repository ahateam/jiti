package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 用户审批记录表
 * @author JXians
 *
 */

@RDSAnnEntity(alias = "tb_ecm_user_examine_record")
public class UserExamineRecord {

	// 组织编号
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long Id;
	
	// 用户编号
	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;

	// 组织编号
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;
	
	// 流程实例编号
	@RDSAnnField(column = RDSAnnField.ID)
	public Long processId;
	
	//操作类型
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String action;
	
	//操作时间
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public Date operatingTime;
	
	//备注
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;
	
}
