package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 用户导入任务（任务批次，方便管理）
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user_import_task")
public class ORGUserImportTask {

	public static enum STATUS implements ENUMVALUE {
		START((byte) 0, "正在导入"), //
		END((byte) 1, "导入完成"), //
		WAIT((byte) 2, "等待导入"),//
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
	 * 任务名称
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String name;

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;

	/**
	 * 任务状态
	 */
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;

	/**
	 * 总数
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer sum;

	/**
	 * 完成数量
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer success;

	/**
	 * 成功数量
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer completion;

	/**
	 * 失败数量
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer notCompletion;
}
