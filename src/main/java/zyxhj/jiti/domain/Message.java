package zyxhj.jiti.domain;

import java.util.Date;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 消息
 */
@RDSAnnEntity(alias = "tb_ecm_message")
public class Message {

	public static enum TYPE implements ENUMVALUE {
		EXAMINE((byte) 0, "审核"), //
		VOTE((byte) 1, "投票"), //
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

	public static enum STATUS implements ENUMVALUE {
		UNREAD((byte) 0, "未读"), //
		READ((byte) 1, "已读"), //
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

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	/**
	 * 组织id
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 用户id
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;
	
	/**
	 * 投票编号或审批编号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long ownerId;

	/**
	 * 标题
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String title;

	/**
	 * 内容
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String content;

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;

	/**
	 * 消息类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

	/**
	 * 审核状态
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte examineStatus;

	/**
	 * 阅读状态 已读/未读
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;
	
	/**
	 * 查看权限
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public JSONObject roles;
	

}
