package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 消息通知
 */
@RDSAnnEntity(alias = "tb_ecm_message")
public class Message {

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long taskId;

	/**
	 * 组织id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 用户id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;

	/**
	 * 微信openid
	 */
	@RDSAnnField(column = "VARCHAR(128)")
	public String openId;

	/**
	 * 用户电话号码
	 */
	@RDSAnnField(column = "VARCHAR(48)")
	public String mobile;

}
