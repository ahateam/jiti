package zyxhj.jiti.domain;

import com.alibaba.fastjson.JSONArray;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 
 * @author JXians 反馈表
 */
@RDSAnnEntity(alias = "tb_ecm_feedback")
public class Feedback {

	// 反馈id
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	// 反馈者编号(用户编号)
	@RDSAnnField(column = RDSAnnField.ID)
	public Long fbUserId;

	// 反馈内容
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String feedbackContent;

	// 手机号
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String phone;

}
