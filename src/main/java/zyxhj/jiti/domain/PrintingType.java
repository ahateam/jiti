package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织角色
 *
 */
@RDSAnnEntity(alias = "tb_ecm_printing_type")
public class PrintingType {

	private static PrintingType buildSysRole(Long temp, String printing, String printingName) {
		PrintingType ret = new PrintingType();
		ret.id = 100L;
		ret.printingId = temp;
		ret.printing = printing;
		ret.printingName = printingName;
		return ret;
	}

	// 成员股权证
	private static Long temp1 = 100L;
	public static final PrintingType share_cer_no = buildSysRole(temp1++, "shareCerNo", "股权证编号");
	public static final PrintingType org_name = buildSysRole(temp1++, "name", "合作社名称");
	public static final PrintingType org_address = buildSysRole(temp1++, "address", "合作社地址");
	public static final PrintingType org_create_time = buildSysRole(temp1++, "createTime", "成立时间");
	public static final PrintingType org_code = buildSysRole(temp1++, "code", "合作社代码");

	public static final PrintingType family_master = buildSysRole(temp1++, "familyMaster", "持证人");
	public static final PrintingType org_user_address = buildSysRole(temp1++, "address", "家庭住址");
	public static final PrintingType org_user_name = buildSysRole(temp1++, "realName", "用户姓名");
//	public static final PrintingType org_user = buildSysRole(temp1++, "orgUser", "是否成员");
	public static final PrintingType is_org_user = buildSysRole(temp1++, "isOrgUser", "是否组织成员 ");
	public static final PrintingType id_number = buildSysRole(temp1++, "idNumber", "身份证号");
	public static final PrintingType share_amount = buildSysRole(temp1++, "shareAmount", "股份数");
	public static final PrintingType remark = buildSysRole(temp1++, "remark", "备注");
	
	public static final PrintingType org_resource_shares = buildSysRole(temp1++, "resourceShares", "资源股");
	public static final PrintingType org_asset_shares = buildSysRole(temp1++, "assetShares", "资产股");
	public static final PrintingType org_family_relations = buildSysRole(temp1++, "familyRelations", "与户主关系");
	

	public static final PrintingType org_user_resource_shares = buildSysRole(temp1++, "resourceShares", "资源股");
	public static final PrintingType org_user_asset_shares = buildSysRole(temp1++, "assetShares", "资产股");
	

	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long id;

	/**
	 * 打印编号
	 */
	@RDSAnnField(column = RDSAnnField.ID)
	public Long printingId;

	/**
	 * 打印字段
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public String printing;

	/**
	 * 打印名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String printingName;

}
