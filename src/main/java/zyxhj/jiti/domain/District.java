package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 行政区定义表 （省市区）
 */
@RDSAnnEntity(alias = "tb_ecm_district")
public class District {

	public static enum LEVEL implements ENUMVALUE {
		PRO((byte) 1, "省"), //
		CITY((byte) 2, "市"), //
		DISTRICT((byte) 3, "区"), //
		;

		private byte v;
		private String txt;

		private LEVEL(byte v, String txt) {
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
	 * 隶属父id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.LONG)
	public Long father;

	/**
	 * 行政区（省市区）id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	/**
	 * 等级 1为省 2为市 3为区、县
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte level;

	/**
	 * 名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

}
