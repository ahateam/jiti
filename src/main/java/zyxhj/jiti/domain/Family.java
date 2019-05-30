package zyxhj.jiti.domain;


import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 分户
 *
 */
@RDSAnnEntity(alias = "tb_ecm_family")
public class Family {

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;
	
	//组织编号
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 户序号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long familyNumber;

	/**
	 * 户主名
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String familyMaster;
	
	/**
	 * 家庭成员
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String familyMember;
	
}
