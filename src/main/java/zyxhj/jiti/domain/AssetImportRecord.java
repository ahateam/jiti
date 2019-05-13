package zyxhj.jiti.domain;

import java.util.Date;

import zyxhj.utils.api.Controller.ENUMVALUE;
import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 资产导入任务记录（计划用ots做）
 *
 */
@RDSAnnEntity(alias = "tb_ecm_asset_import_record")
public class AssetImportRecord {
	
	public static enum STATUS implements ENUMVALUE {
		UNDETECTED((byte) 0,"未检测"),
		COMPLETION((byte) 1,"已通过"),
		NOTCOMPLETION((byte) 2,"未通过"),
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
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;
	
	/**
	 * 任务id
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long taskId;
	

	/**
	 * 创建时间
	 */
	@RDSAnnField(column = RDSAnnField.TIME)
	public Date createTime;

	/**
	 * 资产原始编号
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String originId;

	/**
	 * 资产原始名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String name;

	/**
	 * 资产证件号
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String sn;

	/**
	 * 资源类型
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String resType;

	/**
	 * 资产类型（动产，不动产）
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String assetType;

	/**
	 * 构建时间
	 */
	@RDSAnnField(column = "VARCHAR(16)")
	public String buildTime;

	/**
	 * 原始价格（万元）
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double originPrice;

	/**
	 * 坐落或置放位置
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String location;

	/**
	 * 权属
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String ownership;

	/**
	 * 保管人
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String keeper;

	/**
	 * （经营属性）经营方式
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String businessMode;

	/**
	 * （经营属性）经营起止时间
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String businessTime;

	/**
	 * （经营属性）承租方或投资对象
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String holder;

	/**
	 * （经营属性）年收益，万元
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double yearlyIncome;

	/**
	 * （动产属性）规格型号
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_TITLE)
	public String specType;

	/**
	 * （不动产属性）不动产类型
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String estateType;

	/**
	 * （不动产属性）建筑面积，平方米
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double area;

	/**
	 * （不动产属性）占地面积，平方米
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double floorArea;

	/**
	 * （不动产属性）四至边界，JSONObject</br>
	 * </br>
	 * east 东</br>
	 * west 西</br>
	 * south 南</br>
	 * north 北</br>
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String boundary;

	/**
	 * （不动产属性）起点位置
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String locationStart;

	/**
	 * （不动产属性）终点位置
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String locationEnd;

	/**
	 * （不动产属性）起点坐标
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String coordinateStart;

	/**
	 * （不动产属性）终点坐标
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String coordinateEnd;

	/**
	 * （不动产属性）蓄积，立方米
	 */
	@RDSAnnField(column = RDSAnnField.DOUBLE)
	public Double accumulateStock;

	/**
	 * （不动产属性）棵
	 */
	@RDSAnnField(column = RDSAnnField.INTEGER)
	public Integer treeNumber;

	/**
	 * 图片地址列表（JSONObject）</br>
	 * </br>
	 * imgExt1 附属图片1（基础属性）</br>
	 * imgExt2 附属图片2（基础属性）</br>
	 * imgStart 起点图片（不动产属性）</br>
	 * imgEnd 终点图片（不动产属性）</br>
	 * imgFar 远景图片（不动产属性）</br>
	 * imgNear 近景图片（不动产属性）</br>
	 * imgFront 正面图片（不动产属性）</br>
	 * imgSide 侧面图片（不动产属性）</br>
	 * imgBack 背面图片（不动产属性）</br>
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String imgs;

	/**
	 * 备注
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;
	
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
