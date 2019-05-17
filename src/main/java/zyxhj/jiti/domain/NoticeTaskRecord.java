package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 微信openId
 *
 */
@RDSAnnEntity(alias = "tb_ecm_notice_task_open_id")
public class NoticeTaskRecord {
	public static enum STATUS implements ENUMVALUE {
		UNDETECTED((byte) 0 ,"未通知"),//
		SUCCESS((byte) 1, "成功"), //
		FAILURE((byte) 2, "失败"), //
		;

		private byte v;
		private String txt;

		private STATUS(Byte v, String txt) {
			this.v = v;
			this.txt = txt;
		}

		@Override
		public byte v() {
			return v;
		}

		@Override
		public String txt() {
			return txt;
		}
	}
	
	/**
	 * 任务id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long taskId;
	
	/**
	 * 用户id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;
	
	/**
	 * 组织id
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;
	
	/**
	 * 电话号码
	 */
	@RDSAnnField(column = "VARCHAR(48)")
	public String mobile;
	
	
	/**
	 * openId
	 */
	@RDSAnnField(column = "VARCHAR(128")
	public String openId;
	
	/**
	 * 发送状态
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;

}
