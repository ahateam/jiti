package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

@RDSAnnEntity(alias = "tb_ecm_single_certificate_task")
public class SingleCertificateTask  {
	
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	@RDSAnnField(column = RDSAnnField.ID)
	public Long userId;

	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String title;

	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;

	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer totalNumber;
	
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String fileUrl;
	
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;
	
	public static Byte STATUS_FAIL = 2;//上传失败
	public static Byte STATUS_SUCCESS = 1;//上传成功
	public static Byte STATUS_IN = 0;//上传中

}
