package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.AssetImportRecord;
import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.jiti.service.AssetService;
import zyxhj.jiti.service.AssetTypeService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class AssetController extends Controller {

	private static Logger log = LoggerFactory.getLogger(AssetController.class);

	private DruidDataSource dds;
	private AssetService assetService;

	public AssetController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

			assetService = Singleton.ins(AssetService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@ENUM(des = "任务状态")
	public AssetImportTask.STATUS[] taskStatus = AssetImportTask.STATUS.values();

	@ENUM(des = "任务状态")
	public AssetImportRecord.STATUS[] recordStatus = AssetImportRecord.STATUS.values();

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createAsset", //
			des = "创建资产", //
			ret = "所创建的对象"//
	)
	public APIResponse createAsset(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "资产原始编号") String originId, //
			@P(t = "资产原始名称") String name, //
			@P(t = "资产证件号", r = false) String sn, //
			@P(t = "资源类型") String resType, //
			@P(t = "资产类型（动产，不动产）") String assetType, //

			@P(t = "构建时间") String buildTime, //
			@P(t = "原始价格（万元）") Double originPrice, //
			@P(t = "坐落或置放位置") String location, //
			@P(t = "权属") String ownership, //
			@P(t = "保管人", r = false) String keeper, //

			@P(t = "（经营属性）经营方式") String businessMode, //
			@P(t = "（经营属性）经营起止时间", r = false) String businessTime, //
			@P(t = "（经营属性）承租方或投资对象", r = false) String holder, //
			@P(t = "（经营属性）年收益，万元", r = false) Double yearlyIncome, //
			@P(t = "（动产属性）规格型号", r = false) String specType, //

			@P(t = "（不动产属性）不动产类型", r = false) String estateType, //
			@P(t = "（不动产属性）建筑面积，平方米", r = false) Double area, //
			@P(t = "（不动产属性）占地面积，平方米", r = false) Double floorArea, //
			@P(t = "（不动产属性）四至边界，JSONObject{east:东,west:西,south:南,north:北}", r = false) String boundary, //
			@P(t = "（不动产属性）起点位置", r = false) String locationStart, //

			@P(t = "（不动产属性）终点位置", r = false) String locationEnd, //
			@P(t = "（不动产属性）起点坐标", r = false) String coordinateStart, //
			@P(t = "（不动产属性）终点坐标", r = false) String coordinateEnd, //
			@P(t = "（不动产属性）蓄积，立方米", r = false) Double accumulateStock, //
			@P(t = "（不动产属性）棵", r = false) Integer treeNumber, //

			@P(t = "图片地址列表（JSONObject\n\\n" + //
					"			 * imgExt1 附属图片1（基础属性）\n" + //
					"			 * imgExt2 附属图片2（基础属性）\n" + //
					"			 * imgStart 起点图片（不动产属性）\n" + //
					"			 * imgEnd 终点图片（不动产属性）\n" + //
					"			 * imgFar 远景图片（不动产属性）\n" + //
					"			 * imgNear 近景图片（不动产属性）\n" + //
					"			 * imgFront 正面图片（不动产属性）\n" + //
					"			 * imgSide 侧面图片（不动产属性）\n" + //
					"			 * imgBack 背面图片（不动产属性）", r = false) String imgs, //
			@P(t = "备注", r = false) String remark, //
			@P(t = "分组信息，JSONArray格式", r = false) JSONArray groups) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.createAsset(conn, orgId, originId, name, sn, resType,
					assetType, buildTime, originPrice, location, ownership, keeper, businessMode, businessTime, holder,
					yearlyIncome, specType, estateType, area, floorArea, boundary, locationStart, locationEnd,
					coordinateStart, coordinateEnd, accumulateStock, treeNumber, imgs, remark, groups));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editAsset", //
			des = "编辑资产", //
			ret = "所影响记录行数"//
	)
	public APIResponse editAsset(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "资产编号") Long assetId, //
			@P(t = "资产原始编号", r = false) String originId, //
			@P(t = "资产原始名称", r = false) String name, //
			@P(t = "资产证件号", r = false) String sn, //
			@P(t = "资源类型", r = false) String resType, //
			@P(t = "资产类型（动产，不动产）", r = false) String assetType, //

			@P(t = "构建时间", r = false) String buildTime, //
			@P(t = "原始价格（万元）", r = false) Double originPrice, //
			@P(t = "坐落或置放位置", r = false) String location, //
			@P(t = "权属", r = false) String ownership, //
			@P(t = "保管人", r = false) String keeper, //

			@P(t = "（经营属性）经营方式", r = false) String businessMode, //
			@P(t = "（经营属性）经营起止时间", r = false) String businessTime, //
			@P(t = "（经营属性）承租方或投资对象", r = false) String holder, //
			@P(t = "（经营属性）年收益，万元", r = false) Double yearlyIncome, //
			@P(t = "（动产属性）规格型号", r = false) String specType, //

			@P(t = "（不动产属性）不动产类型", r = false) String estateType, //
			@P(t = "（不动产属性）建筑面积，平方米", r = false) Double area, //
			@P(t = "（不动产属性）占地面积，平方米", r = false) Double floorArea, //
			@P(t = "（不动产属性）四至边界，JSONObject{east:东,west:西,south:南,north:北}", r = false) String boundary, //
			@P(t = "（不动产属性）起点位置", r = false) String locationStart, //

			@P(t = "（不动产属性）终点位置", r = false) String locationEnd, //
			@P(t = "（不动产属性）起点坐标", r = false) String coordinateStart, //
			@P(t = "（不动产属性）终点坐标", r = false) String coordinateEnd, //
			@P(t = "（不动产属性）蓄积，立方米", r = false) Double accumulateStock, //
			@P(t = "（不动产属性）棵", r = false) Integer treeNumber, //

			@P(t = "图片地址列表（JSONObject\n\\n" + //
					"			 * imgExt1 附属图片1（基础属性）\n" + //
					"			 * imgExt2 附属图片2（基础属性）\n" + //
					"			 * imgStart 起点图片（不动产属性）\n" + //
					"			 * imgEnd 终点图片（不动产属性）\n" + //
					"			 * imgFar 远景图片（不动产属性）\n" + //
					"			 * imgNear 近景图片（不动产属性）\n" + //
					"			 * imgFront 正面图片（不动产属性）\n" + //
					"			 * imgSide 侧面图片（不动产属性）\n" + //
					"			 * imgBack 背面图片（不动产属性）", r = false) String imgs, //
			@P(t = "备注", r = false) String remark, //
			@P(t = "分组信息，JSONArray格式", r = false) JSONArray groups//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.editAsset(conn, orgId, assetId, originId, name, sn,
					resType, assetType, buildTime, originPrice, location, ownership, keeper, businessMode, businessTime,
					holder, yearlyIncome, specType, estateType, area, floorArea, boundary, locationStart, locationEnd,
					coordinateStart, coordinateEnd, accumulateStock, treeNumber, imgs, remark, groups));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delAsset", //
			des = "删除资产", //
			ret = "所影响记录行数"//
	)
	public APIResponse delAsset(//
			@P(t = "资产编号") Long assetId) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.delAsset(conn, assetId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "queryAssets", //
			des = "获取资产列表", //
			ret = "资产列表"//
	)
	public APIResponse queryAssets(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "资产类型(动产,不动产)", r = false) String assetType, //
			@P(t = "分组信息，JSONArray格式", r = false) JSONArray groups, //
			@P(t = "标签信息，JSONObject格式", r = false) JSONObject tags, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(assetService.queryAssets(conn, orgId, assetType, groups, tags, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "importAssets", //
			des = "导入资产列表" //
	)
	public APIResponse importAssets(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "excel文件url") String url//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			assetService.importAssets(conn, orgId, url);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetsByGroups", //
			des = "根据分组信息获取资产列表", //
			ret = "资产列表"//
	)
	public APIResponse getAssetsByGroups(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "分组,String[]格式", r = false) JSONArray groups, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.getAssetsByGroups(conn, orgId, groups, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetsByTags", //
			des = "根据标签信息获取组织成员列表", //
			ret = "成员列表"//
	)
	public APIResponse getAssetsByTags(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色标签对象,JSONObject格式", r = false) JSONObject tags, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.getAssetsByTags(conn, orgId, tags, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "batchEditAssetsGroups", //
			des = "批量修改资产分组信息", //
			ret = "更新的记录行数"//
	)
	public APIResponse batchEditAssetsGroups(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "角色标签对象,JSONArray格式") JSONArray assetIds, //
			@P(t = "角色标签对象,JSONArray格式") JSONArray groups//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.batchEditAssetsGroups(conn, orgId, assetIds, groups));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getOriORNameBySn", //
			des = "根据证件号模糊查询对应的资产", //
			ret = "返回查询结果"//
	)
	public APIResponse getOriORNameBySn(//
			@P(t = "组织id") Long orgId, //
			@P(t = "证件号/名称") String assetNum, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.getAssetsBySn(conn, orgId, assetNum, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getOriORNameByName", //
			des = "根据名称模糊查询对应的资产", //
			ret = "返回查询结果"//
	)
	public APIResponse getOriORNameByName(//
			@P(t = "组织id") Long orgId, //
			@P(t = "证件号/名称") String assetNum, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.getAssetsByName(conn, orgId, assetNum, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetByYear", //
			des = "查询当前组织下的输入的年份下所有资产信息", //
			ret = "返回查询结果"//
	)
	public APIResponse getAssetByYear(//
			@P(t = "组织id") Long orgId, //
			@P(t = "年份") String buildTime, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.getAssetByYear(conn, orgId, buildTime, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "ORGsumAssetBYGRAB", //
			des = "按组织id 查询总的报表信息", //
			ret = "返回统计结果"//
	)
	public APIResponse ORGsumAssetBYGRAB(//
			@P(t = "组织id") Long orgId, //
			@P(t = "分组信息", r = false) JSONArray groups, //
			@P(t = "年份", r = false) String buildTime, //
			@P(t = "资源类型", r = false) JSONArray resType, //
			@P(t = "资产类型", r = false) JSONArray assetType, //
			@P(t = "经营方式", r = false) JSONArray businessMode //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					assetService.sumAssetBYGRAB(conn, orgId, buildTime, groups, resType, assetType, businessMode));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "districtCountByYear", //
			des = "按区级id 查询某一年的数据", //
			ret = "返回统计结果"//
	)
	public APIResponse districtCountByYear(//
			@P(t = "年份") String buildTime, //
			@P(t = "组织id") JSONArray orgId, //
			@P(t = "分组信息", r = false) JSONArray groups, //
			@P(t = "资源类型", r = false) JSONArray resTypes, //
			@P(t = "资产类型", r = false) JSONArray assetTypes, //
			@P(t = "经营方式", r = false) JSONArray businessModes //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.districtCountByYear(conn, buildTime, orgId, groups,
					resTypes, assetTypes, businessModes));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "districtCountByYears", //
			des = "按区级id 查询多年的数据", //
			ret = "返回统计结果"//
	)
	public APIResponse districtCountByYears(//
			@P(t = "年份") JSONArray buildTimes, //
			@P(t = "组织id") JSONArray orgIds, //
			@P(t = "分组信息", r = false) JSONArray groups, //
			@P(t = "资源类型", r = false) JSONArray resTypes, //
			@P(t = "资产类型", r = false) JSONArray assetTypes, //
			@P(t = "经营方式", r = false) JSONArray businessModes //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.districtCountByYears(conn, buildTimes, orgIds, groups,
					resTypes, assetTypes, businessModes));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetType", //
			des = "查询资产类型", //
			ret = "返回统计结果"//
	)
	public APIResponse getAssetType(//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(AssetTypeService.ASSET_TYPE_LIST);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getResType", //
			des = "查询资源类型", //
			ret = "返回统计结果"//
	)
	public APIResponse getResType(//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(AssetTypeService.RES_TYPE_LIST);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getBusinessMode", //
			des = "查询经营方式", //
			ret = "返回统计结果"//
	)
	public APIResponse getBusinessMode(//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(AssetTypeService.BUSINESS_TYPE_LIST);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetListByTypes", //
			des = "按区级id 查询数据列表", //
			ret = "返回列表"//
	)
	public APIResponse getAssetListByTypes(//
			@P(t = "组织id") JSONArray orgIds, //
			@P(t = "年份", r = false) JSONArray buildTimes, //
			@P(t = "分组信息", r = false) JSONArray groups, //
			@P(t = "资源类型", r = false) JSONArray resTypes, //
			@P(t = "资产类型", r = false) JSONArray assetTypes, //
			@P(t = "经营方式", r = false) JSONArray businessModes, //
			Integer count, Integer offset) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(assetService.getAssetListByTypes(conn, buildTimes, orgIds, groups,
					resTypes, assetTypes, businessModes, count, offset));
		}
	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "createAssetImportTask", //
//			des = "创建资产导入任务", //
//			ret = ""//
//	)
//	public APIResponse createAssetImportTask(//
//			@P(t = "组织id") Long orgId, //
//			@P(t = "用户id") Long userId, //
//			@P(t = "任务名称") String name //
//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			assetService.createAssetImportTask(conn, orgId, userId, name);
//			return APIResponse.getNewSuccessResp();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getAssetImportTasks", //
//			des = "获取资产导入任务", //
//			ret = ""//
//	)
//	public APIResponse getAssetImportTasks(//
//			@P(t = "组织id") Long orgId, //
//			@P(t = "用户id") Long userId, //
//			Integer count, //
//			Integer offset//
//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//
//			return APIResponse.getNewSuccessResp(assetService.getAssetImportTasks(conn, orgId, userId, count, offset));
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getAssetImportTask", //
//			des = "获取当前导入任务信息", //
//			ret = ""//
//	)
//	public APIResponse getAssetImportTask(//
//			@P(t = "组织id") Long orgId, //
//			@P(t = "用户id") Long userId, //
//			@P(t = "导入任务id") Long importTaskId//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			return APIResponse.getNewSuccessResp(assetService.getAssetImportTask(conn, importTaskId, orgId, userId));
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "importAssetsRecord", //
//			des = "导入资产列表" //
//	)
//	public APIResponse importAssetsRecord(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "用户编号") Long userId, //
//			@P(t = "excel文件url") String url, //
//			@P(t = "导入任务id") Long importTaskId//
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			assetService.importAssetsRecord(conn, orgId, userId, url, importTaskId);
//			return APIResponse.getNewSuccessResp();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getAssetImportRecords", //
//			des = "获取导入资产列表", //
//			ret = "需导入的资产列表")
//	public APIResponse getAssetImportRecords(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "导入任务id") Long importTaskId, //
//			Integer count, //
//			Integer offset //
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			return APIResponse
//					.getNewSuccessResp(assetService.getAssetImportRecords(conn, orgId, importTaskId, count, offset));
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "importAsset", //
//			des = "开始导入资产列表", //
//			ret = "")
//	public APIResponse importAsset(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "导入任务id") Long importTaskId //
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//			assetService.importAsset(orgId, importTaskId);
//			return APIResponse.getNewSuccessResp();
//		}
//	}
//
//	/**
//	 * 
//	 */
//	@POSTAPI(//
//			path = "getNotcompletionRecord", //
//			des = "获取导入失败的数据", //
//			ret = "")
//	public APIResponse getNotcompletionRecord(//
//			@P(t = "组织编号") Long orgId, //
//			@P(t = "导入任务id") Long importTaskId, //
//			Integer count, //
//			Integer offset //
//	) throws Exception {
//		try (DruidPooledConnection conn = dds.getConnection()) {
//
//			return APIResponse
//					.getNewSuccessResp(assetService.getNotcompletionRecord(conn, orgId, importTaskId, count, offset));
//		}
//	}
}
