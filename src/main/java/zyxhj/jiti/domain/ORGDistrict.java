package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织行政区归属表
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_district")
public class ORGDistrict {

	public static enum TYPE implements ENUMVALUE {
		COOPERATIVE((byte) 0, "合作社"), //
		ADMINISTRATIVEORGAN((byte) 1, "行政机构"), //
		COMPANYORGANIZATION((byte) 2, "公司机构"), //
		PERSONALORGANIZATION((byte) 3, "个人机构"), //
		FINANCIAL((byte) 4, "金融机构"), //
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

	/**
	 * 组织编号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 省级编号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long proId;

	/**
	 * 市级编号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long cityId;

	/**
	 * 区级编号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long disId;

	/**
	 * 类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;

}
