package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 任务
 *
 */
@RDSAnnEntity(alias = "tb_ecm_notice_task")
public class NoticeTask {

	public static enum TYPE implements ENUMVALUE {
		ORG((byte) 0, "会议公告"), //
		FAMILY((byte) 1, "投票公告"), //
		OTHER((byte) 2, "其他公告"),//
		;

		private byte v;
		private String txt;

		private TYPE(Byte v, String txt) {
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

	public static enum MODE implements ENUMVALUE {
		WX((byte) 0, "微信通知"), //
		PHONENUMBER((byte) 1, "短信通知"), //
		WXANDPHONE((byte) 2, "微信+短信通知"),//
		;

		private byte v;
		private String txt;

		private MODE(Byte v, String txt) {
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
	public String title;

	/**
	 * 任务内容 1024汉字 怕不够
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String content;

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
	 * 通知方式
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte mode;

	/**
	 * 需要发送的人数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer sum;

}
