package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织董事表
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user")
public class ORGUser {

	/**
	 * 组织编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 用户编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;

	/**
	 * 家庭住址
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String address;

	/**
	 * 股权证号
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String shareCerNo;

	/**
	 * 股权证图片地址
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String shareCerImg;

	/**
	 * 是否持证人
	 */
	@RDSAnnField(column = RDSAnnField.BOOLEAN)
	public Boolean shareCerHolder;

	/**
	 * 股份数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer shareAmount;

	/**
	 * 投票权重
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer weight;

	/**
	 * 组织用户角色
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String roles;

	/**
	 * 标签(集体用户的角色role，分组group，以及其它职务和标签等都存放在这个字段)
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String tags;

}
