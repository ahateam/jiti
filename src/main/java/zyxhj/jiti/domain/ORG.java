package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org")
public class ORG {

	public static enum TYPE implements ENUMVALUE {
		INDEPENDENT((byte) 0, "独立"), //
		NOTINDEPENDENT((byte) 1, "非独立"), //
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

	public static enum LEVEL implements ENUMVALUE {
		PRO((byte) 1, "省"), //
		CITY((byte) 2, "市"), //
		DISTRICT((byte) 3, "区"), //
		COOPERATIVE((byte) 4, "合作社"), //
		OTHER((byte) 5, "其他"), //
		;

		private byte v;
		private String txt;

		private LEVEL(Byte v, String txt) {
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
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;

	/**
	 * 名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	/**
	 * 组织机构代码
	 */
	@RDSAnnField(column = "VARCHAR(64)")
	public String code;


	/**
	 * 具体地址
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String address;

	/**
	 * 组织机构证书图片
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String imgOrg;

	/**
	 * 授权证书图片
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String imgAuth;

	/**
	 * 股份份数总数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer shareAmount;

	/**
	 * 资金
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double capital;

	/**
	 * 负债
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double debt;

	/**
	 * 债权资金
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double receivables;

	/**
	 * 年毛收入
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double income;

	/**
	 * 分红
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double bonus;

	/**
	 * 预算
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double budget;

	/**
	 * 决算
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double financialBudget;

	/**
	 * 对外投资
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double investment;

	/**
	 * 估值
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double valuation;

	/**
	 * 类型 是否有行政区管理 如没有 就是独立的组织
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

	/**
	 * 等级
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte level;
}
