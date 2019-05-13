package zyxhj.jiti.domain;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 用户导入任务记录
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user_import_record")
public class ORGUserImportRecord {

	public static enum STATUS implements ENUMVALUE {
		UNDETECTED((byte) 0, "未检测"), //
		COMPLETION((byte) 1, "已通过"), //
		NOTCOMPLETION((byte) 2, "未通过"),//
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
	 * 用户编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long taskId;
	
	/**
	 * 户序号
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String familyNumber;

	/**
	 * 用户真名
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String realName;

	/**
	 * 身份证号
	 */
	@RDSAnnField(column = "VARCHAR(48)")
	public String idNumber;

	/**
	 * 手机号（索引）
	 */
	@RDSAnnField(column = "VARCHAR(48)")
	public String mobile;

	/**
	 * 股份数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer shareAmount;

	/**
	 * 投票权重
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer weight;

	/**
	 * 家庭住址
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String address;

	/**
	 * 户主名
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String familyMaster;

	/**
	 * 是否持证人
	 */
	@RDSAnnField(column = RDSAnnField.BOOLEAN)
	public Boolean shareCerHolder;

	/**
	 * 股权证号
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String shareCerNo;

	/**
	 * 组织用户角色
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String roles;

	/**
	 * 分组，怕长度不够2048
	 */
	@RDSAnnField(column = "VARCHAR(2048)")
	public String groups;

	/**
	 * 标签
	 */
	@RDSAnnField(column = RDSAnnField.JSON)
	public String tags;

	/**
	 * 状态
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;
	
	/**
	 * 错误信息
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String errorReason;
}

