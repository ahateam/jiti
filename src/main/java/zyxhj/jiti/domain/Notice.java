package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 公告
 */
@RDSAnnEntity(alias = "tb_ecm_notice")
public class Notice {

	public static enum TYPE implements ENUMVALUE {
		ORG((byte) 0, "会议公告"), //
		FAMILY((byte) 1, "投票公告"), //
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

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 公告名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	/**
	 * 公告内容
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String content;

	/**
	 * 公告类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;

	/**
	 * 显示结束时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date endTime;

	/**
	 * 通知人群 使用json存 按组织/分组/角色通知
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String crowd;
}
