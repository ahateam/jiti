package zyxhj.jiti.domain;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织角色
 *
 */
@RDSAnnEntity(alias = "tb_ecm_asset_type")
public class AssetType {

	private static AssetType buildSysRole(Long typeId, String name, String remark) {
		AssetType ret = new AssetType();
		ret.assetId = 100L;
		ret.typeId = typeId;
		ret.name = name;
		ret.remark = remark;

		return ret;
	}

	//资产类型
	private static Long temp1 = 100L;// 自增编号

	public static final AssetType assetType_movables = buildSysRole(temp1++, "动产", null);
	public static final AssetType assetType_immovables = buildSysRole(temp1++, "不动产", null);
	
	//资源类型
	private static Long temp2 = 200L;
	
	public static final AssetType resType_woodland = buildSysRole(temp2++, "林地", null);
	public static final AssetType resType_plough = buildSysRole(temp2++, "耕地", null);
	public static final AssetType resType_ntslss = buildSysRole(temp2++, "农田水利设施用地", null);
	public static final AssetType mountain_pond = buildSysRole(temp2++, "山塘", null);
	public static final AssetType  resType_ditch = buildSysRole(temp2++, "水沟", null);
	public static final AssetType  pump_house = buildSysRole(temp2++, "泵房", null);
	public static final AssetType  resType_highway = buildSysRole(temp2++, "公路", null);
	public static final AssetType  house_vacant = buildSysRole(temp2++, "房屋空地", null);
	public static final AssetType  office_room = buildSysRole(temp2++, "办公房", null);
	
	
	//经营方式
	private static Long temp3 = 300L;
	public static final AssetType businessType_rent = buildSysRole(temp3++, "出租经营", null);
	public static final AssetType  businessType_ownchoice = buildSysRole(temp3++, "集体自主经营", null);
	public static final AssetType  businessType_idle = buildSysRole(temp3++, "闲置", null);
	public static final AssetType  public_welfare = buildSysRole(temp3++, "公益性用途", null);
	
	

	/**
	 * 资产编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long assetId;

	/**
	 * 类型编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long typeId;

	/**
	 * 类型名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;
}
