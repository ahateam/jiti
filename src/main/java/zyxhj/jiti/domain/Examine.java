package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 审核表
 */
@RDSAnnEntity(alias = "tb_ecm_examine")
public class Examine {

	public static enum TYPE implements ENUMVALUE {
		ORG((byte) 0, "组织申请"), //
		FAMILY((byte) 1, "分户申请"), //
		SHARE((byte) 2, "股权变更申请"),//
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
		NOEXAMINE((byte) 0, "未审核"), //
		ORGEXAMINE((byte) 1, "组织审核通过"), //
		DISEXAMINE((byte) 2, "区级审核通过"), //
		PASS((byte) 3, "审核成功"), //
		FAIL((byte) 4, "审核失败"), //
		WAITEC((byte) 5, "等待取证"), //
		TACKEC((byte) 6, "已取证"),//
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

	public static enum OPERATE implements ENUMVALUE {
		ADDFAMILY((byte) 0, "新增户"), //
		HOUSEHOLD((byte) 1, "分户"), //
		ADDFAMILYUSER((byte) 2, "新增户成员"), //
		DELFAMILYUSER((byte) 3, "移除户成员"), //
		MOVEFAMILYUSER((byte) 4, "移户"), //
		UPSHARE((byte) 5, "股权变更"), //
		
		;

		private byte v;
		private String txt;

		private OPERATE(Byte v, String txt) {
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

	public static enum TAB implements ENUMVALUE {
		REMOVE((byte) 0, "移除标记"), //
		ADD((byte) 1, "新增标记"), //
		;

		private byte v;
		private String txt;

		private TAB(Byte v, String txt) {
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
	 * 审核id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	/**
	 * 组织id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 用户id
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;

	/**
	 * 数据
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String data;

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createDate;

	/**
	 * 审核时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date examineDate;

	/**
	 * 审核类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

	/**
	 * 审核状态
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;

	/**
	 * 备注
	 */
	@RDSAnnField(column = RDSAnnField.VARCHAR)
	public String remark;

}
