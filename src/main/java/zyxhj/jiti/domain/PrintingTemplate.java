package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 打印模板
 */
@RDSAnnEntity(alias = "tb_ecm_printing_temp")
public class PrintingTemplate {

	public static enum TYPE implements ENUMVALUE {
		SHAREWARRANT((byte) 0, "成员股权证"), //
		INITREG((byte) 1, "初始登记"), //
		EDITREG((byte) 2, "变更登记"), //
		MORTGAGE((byte) 3, "抵押情况登记"), //
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

	public static enum PAGE implements ENUMVALUE {
		LEFT((byte) 1, "左页"), //
		RIGHT((byte) 2, "右页"), //
		;

		private byte v;
		private String txt;

		private PAGE(Byte v, String txt) {
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
	 * 模板数据
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String data;

	/**
	 * 模板类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

	/**
	 * 模板页码
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte page;

}
