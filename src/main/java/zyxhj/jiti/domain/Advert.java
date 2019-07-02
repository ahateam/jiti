package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 广告
 *
 */
@RDSAnnEntity(alias = "tb_ecm_advert")
public class Advert {

	public static enum TYPE implements ENUMVALUE {
		BANNER((byte) 0, "banner"), //
		NOTICE((byte) 1, "公告嵌入广告"), //
		TEXT((byte) 2, "文本走马灯"),//
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
	 * 标题
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String title;

	/**
	 * 数据</br>
	 * JSON形式存储内容信息结构体，具体结构体视项目而定
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String data;

	/**
	 * 牛逼的JSON
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String tags;

	/**
	 * 类型
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte type;
}
