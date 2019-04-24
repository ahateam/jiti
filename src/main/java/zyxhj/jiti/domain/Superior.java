package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 上下级关系表
 *
 */
@RDSAnnEntity(alias = "tb_ecm_superior")
public class Superior {

	public static enum TYPE implements ENUMVALUE {
		INDEPENDENT((byte) 0, "独立"), //
		MANAGEMENT((byte) 1, "非独立"), //
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

	/**
	 * 父级机构
	 */
	@RDSAnnID
	@RDSAnnField(column = "varchar(128)")
	public Long superiorId;

	/**
	 * 机构id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

//	/**
//	 * 下级id 可能为多个
//	 */
//	@RDSAnnField(column = "varchar(128)")
//	public String subId;

	/**
	 * 类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

}
