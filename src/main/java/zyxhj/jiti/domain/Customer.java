package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 
 * @author JXians
 * 客服表
 */
@RDSAnnEntity(alias = "tb_ecm_customer")
public class Customer {

	//客服id
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;
	
	//客服姓名
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;
	
	//客服手机号
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String phone;
	
	//客服微信号
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String wxNumber;
	
	//客服QQ号
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String qqNumber;
	
	//客服邮箱
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String Email;
	
	//备注
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;
	
	//是否启用
	@RDSAnnField(column = RDSAnnField.BYTE)
	public Byte status;
	
}
