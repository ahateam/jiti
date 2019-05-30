package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 任务
 *
 */
@RDSAnnEntity(alias = "tb_ecm_notice_task")
public class NoticeTask {

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

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
	 * 任务名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String name;

	/**
	 * 任务内容 1024汉字 怕不够
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;
	/**
	 * 需要通知的人群
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String crowd;
	
	/**
	 * 通知类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;
	
	/**
	 * 需要发送的人数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer sum;

}
