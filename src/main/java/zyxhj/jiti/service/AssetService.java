package zyxhj.jiti.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.vertx.core.Vertx;
import zyxhj.jiti.domain.Asset;
import zyxhj.jiti.domain.AssetImportRecord;
import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.jiti.repository.AssetImportRecordRepository;
import zyxhj.jiti.repository.AssetImportTaskRepository;
import zyxhj.jiti.repository.AssetRepository;
import zyxhj.utils.CodecUtils;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;

public class AssetService {

	private static Logger log = LoggerFactory.getLogger(AssetService.class);

	private AssetRepository assetRepository;
	private AssetImportTaskRepository assetImportTaskRepository;
	private AssetImportRecordRepository assetImportRecordRepository;

	public AssetService() {
		try {
			assetRepository = Singleton.ins(AssetRepository.class);
			assetImportTaskRepository = Singleton.ins(AssetImportTaskRepository.class);
			assetImportRecordRepository = Singleton.ins(AssetImportRecordRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Asset createAsset(DruidPooledConnection conn, Long orgId, //
			String originId, String name, String sn, String resType, String assetType, //
			String buildTime, Double originPrice, String location, String ownership, String keeper, //
			String businessMode, String businessTime, String holder, Double yearlyIncome, String specType, //
			String estateType, Double area, Double floorArea, String boundary, String locationStart, //
			String locationEnd, String coordinateStart, String coordinateEnd, Double accumulateStock,
			Integer treeNumber, //
			String imgs, String remark, //
			JSONArray groups) throws Exception {
		Asset a = new Asset();
		a.id = IDUtils.getSimpleId();
		a.orgId = orgId;
		a.createTime = new Date();

		a.originId = originId;
		a.name = name;
		a.sn = sn;
		a.resType = resType;
		a.assetType = assetType;

		a.buildTime = buildTime;
		a.originPrice = originPrice;
		a.location = location;
		a.ownership = ownership;
		a.keeper = keeper;

		a.businessMode = businessMode;
		a.businessTime = businessTime;
		a.holder = holder;
		a.yearlyIncome = yearlyIncome;
		a.specType = specType;

		a.estateType = estateType;
		a.area = area;
		a.floorArea = floorArea;
		a.boundary = boundary;
		a.locationStart = locationStart;

		a.locationEnd = locationEnd;
		a.coordinateStart = coordinateStart;
		a.coordinateEnd = coordinateEnd;
		a.accumulateStock = accumulateStock;
		a.treeNumber = treeNumber;

		a.imgs = imgs;
		a.remark = remark;
		a.groups = ORGUserService.array2JsonString(ORGUserService.checkGroups(conn, orgId, groups));

		assetRepository.insert(conn, a);

		return a;
	}

	public int editAsset(DruidPooledConnection conn, Long orgId, Long assetId, //
			String originId, String name, String sn, String resType, String assetType, //
			String buildTime, Double originPrice, String location, String ownership, String keeper, //
			String businessMode, String businessTime, String holder, Double yearlyIncome, String specType, //
			String estateType, Double area, Double floorArea, String boundary, String locationStart, //
			String locationEnd, String coordinateStart, String coordinateEnd, Double accumulateStock,
			Integer treeNumber, //
			String imgs, String remark, //
			JSONArray groups) throws Exception {
		Asset a = new Asset();

		a.originId = originId;
		a.name = name;
		a.sn = sn;
		a.resType = resType;
		a.assetType = assetType;

		a.buildTime = buildTime;
		a.originPrice = originPrice;
		a.location = location;
		a.ownership = ownership;
		a.keeper = keeper;

		a.businessMode = businessMode;
		a.businessTime = businessTime;
		a.holder = holder;
		a.yearlyIncome = yearlyIncome;
		a.specType = specType;

		a.estateType = estateType;
		a.area = area;
		a.floorArea = floorArea;
		a.boundary = boundary;
		a.locationStart = locationStart;

		a.locationEnd = locationEnd;
		a.coordinateStart = coordinateStart;
		a.coordinateEnd = coordinateEnd;
		a.accumulateStock = accumulateStock;
		a.treeNumber = treeNumber;

		a.imgs = imgs;
		a.remark = remark;

		a.groups = ORGUserService.array2JsonString(ORGUserService.checkGroups(conn, orgId, groups));

		return assetRepository.updateByKey(conn, "id", assetId, a, true);
	}

	public int delAsset(DruidPooledConnection conn, Long assetId) throws Exception {
		return assetRepository.deleteByKey(conn, "id", assetId);
	}

	public List<Asset> queryAssets(DruidPooledConnection conn, Long orgId, String assetType, JSONArray groups,
			JSONObject tags, Integer count, Integer offset) throws Exception {
		return assetRepository.queryAssets(conn, orgId, assetType, groups, tags, count, offset);
	}

	public void importAssets(DruidPooledConnection conn, Long orgId, String url) throws Exception {

		// 2行表头，38列，文件格式写死的
		List<List<Object>> table = ExcelUtils.readExcelOnline(url, 2, 39, 0);

		for (List<Object> row : table) {
			String originId = ExcelUtils.getString(row.get(0));
			String name = ExcelUtils.getString(row.get(1));
			String sn = ExcelUtils.getString(row.get(2));
			String resType = ExcelUtils.getString(row.get(3));
			String assetType = ExcelUtils.getString(row.get(4));

			String buildTime = ExcelUtils.getString(row.get(5));
			Double originPrice = ExcelUtils.parseDouble(row.get(6));
			String location = ExcelUtils.getString(row.get(7));
			String ownership = ExcelUtils.getString(row.get(8));
			String keeper = ExcelUtils.getString(row.get(9));

			String imgExt1 = ExcelUtils.getString(row.get(10));
			String imgExt2 = ExcelUtils.getString(row.get(11));

			String businessMode = ExcelUtils.getString(row.get(12));
			String businessTime = ExcelUtils.getString(row.get(13));
			String holder = ExcelUtils.getString(row.get(14));
			Double yearlyIncome = ExcelUtils.parseDouble(row.get(15));
			String specType = ExcelUtils.getString(row.get(16));

			String estateType = ExcelUtils.getString(row.get(17));
			Double area = ExcelUtils.parseDouble(row.get(18));
			Double floorArea = ExcelUtils.parseDouble(row.get(19));

			JSONObject b = new JSONObject();
			b.put("east", ExcelUtils.getString(row.get(20)));
			b.put("west", ExcelUtils.getString(row.get(21)));
			b.put("south", ExcelUtils.getString(row.get(22)));
			b.put("north", ExcelUtils.getString(row.get(23)));
			String boundary = JSON.toJSONString(b);

			String locationStart = ExcelUtils.getString(row.get(24));
			String locationEnd = ExcelUtils.getString(row.get(25));
			String coordinateStart = ExcelUtils.getString(row.get(26));
			String coordinateEnd = ExcelUtils.getString(row.get(27));

			String imgStart = ExcelUtils.getString(row.get(28));
			String imgEnd = ExcelUtils.getString(row.get(29));

			Double accumulateStock = ExcelUtils.parseDouble(row.get(30));
			Integer treeNumber = ExcelUtils.parseInt(row.get(31));

			String imgFar = ExcelUtils.getString(row.get(32));
			String imgNear = ExcelUtils.getString(row.get(33));
			String imgFront = ExcelUtils.getString(row.get(34));
			String imgBack = ExcelUtils.getString(row.get(35));
			String imgSide = ExcelUtils.getString(row.get(36));
			JSONObject img = new JSONObject();
			img.put("imgExt1", imgExt1);
			img.put("imgExt2", imgExt2);

			img.put("imgStart", imgStart);
			img.put("imgEnd", imgEnd);

			img.put("imgFar", imgFar);
			img.put("imgNear", imgNear);
			img.put("imgFront", imgFront);
			img.put("imgSide", imgSide);
			img.put("imgBack", imgBack);

			String imgs = JSON.toJSONString(img);
			String groups = ExcelUtils.getString(row.get(37));
			String remark = ExcelUtils.getString(row.get(38));

			JSONArray arrGroups = new JSONArray();
			JSONArray temp = CodecUtils.convertCommaStringList2JSONArray(groups);

			for (int i = 0; i < temp.size(); i++) {
				String ts = StringUtils.trim(temp.getString(i));
				if (ts.equals("null") || ts.equals("无")) {
					// 无和null，不加
				} else {
					arrGroups.add(ts);
				}
			}

			try {
				createAsset(conn, orgId, originId, name, sn, resType, //
						assetType, buildTime, originPrice, location, ownership, //
						keeper, businessMode, businessTime, holder, yearlyIncome, //
						specType, estateType, area, floorArea, boundary, //
						locationStart, locationEnd, coordinateStart, coordinateEnd, accumulateStock, //
						treeNumber, imgs, remark, arrGroups);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 根据分组查询资产
	 */
	public List<Asset> getAssetsByGroups(DruidPooledConnection conn, Long orgId, String[] groups, Integer count,
			Integer offset) throws Exception {
		return assetRepository.getAssetsByGroups(conn, orgId, groups, count, offset);
	}

	/**
	 * 根据标签查询资产
	 */
	public List<Asset> getAssetsByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws Exception {
		return assetRepository.getAssetsByTags(conn, orgId, tags, count, offset);
	}

	public int batchEditAssetsGroups(DruidPooledConnection conn, Long orgId, JSONArray assetIds, JSONArray groups)
			throws Exception {
		return assetRepository.batchEditAssetsGroups(conn, orgId, assetIds, groups);
	}

	public List<Asset> getAssetsBySn(DruidPooledConnection conn, Long orgId, String sn, Integer count, Integer offset)
			throws Exception {
		return assetRepository.getAssetsBySn(conn, orgId, sn, count, offset);
	}

	public List<Asset> getAssetsByName(DruidPooledConnection conn, Long orgId, String name, Integer count,
			Integer offset) throws Exception {
		return assetRepository.getAssetsByName(conn, orgId, name, count, offset);
	}

	public List<Asset> getAssetByYear(DruidPooledConnection conn, Long orgId, String buildTime, Integer count,
			Integer offset) throws Exception {
		return assetRepository.getAssetByYear(conn, orgId, buildTime, count, offset);
	}

	/**
	 * 根据年份，资产类型等条件，统计资源原值，产值等
	 * 
	 * @param G 分组
	 * @param R 资源类型
	 * @param A 资产类型
	 * @param B 经营方式
	 */
	public JSONArray sumAssetBYGRAB(DruidPooledConnection conn, Long orgId, String buildTime, JSONArray groups,
			JSONArray resType, JSONArray assetType, JSONArray businessMode) throws Exception {
		return assetRepository.sumAssetBYGRAB(conn, orgId, buildTime, groups, resType, assetType, businessMode);
	}

	// public JSONArray sumAssetByDstrictId(DruidPooledConnection conn,Long
	// districtId,JSONArray buildTime,JSONArray orgId, JSONArray groups, JSONArray
	// resType,
	// JSONArray assetType, JSONArray businessMode) throws Exception{
	// return assetRepository.sumAssetByDstrictId(conn,districtId,
	// buildTime,orgId,groups, resType, assetType, businessMode);
	// }

	// 区管理员统计某一年的数据
	public JSONArray districtCountByYear(DruidPooledConnection conn, String buildTime, JSONArray orgId,
			JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes) throws Exception {
		return assetRepository.districtCountByYear(conn, buildTime, orgId, groups, resTypes, assetTypes, businessModes);
	}

	// 区管理员统计多年的数据
	public JSONArray districtCountByYears(DruidPooledConnection conn, JSONArray buildTimes, JSONArray orgIds,
			JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes) throws Exception {
		return assetRepository.districtCountByYears(conn, buildTimes, orgIds, groups, resTypes, assetTypes,
				businessModes);
	}

	// 根据区id获取资产列表
	public List<Asset> getAssetListByTypes(DruidPooledConnection conn, JSONArray buildTimes, JSONArray orgIds,
			JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes, Integer count,
			Integer offset) throws Exception {
		return assetRepository.getAssetListByTypes(conn, buildTimes, orgIds, groups, resTypes, assetTypes,
				businessModes, count, offset);
	}

	// 创建资产导入任务
	public void createAssetImportTask(DruidPooledConnection conn, Long orgId, Long userId, String name)
			throws Exception {
		AssetImportTask ass = new AssetImportTask();
		ass.id = IDUtils.getSimpleId();
		ass.orgId = orgId;
		ass.userId = userId;
		ass.name = name;
		ass.createTime = new Date();
		ass.status = AssetImportTask.STATUS.WAIT.v();
		assetImportTaskRepository.insert(conn, ass);
	}

	// 查询资产导入任务
	public List<AssetImportTask> getAssetImportTasks(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {
		return assetImportTaskRepository.getListByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId }, count, offset);
	}

	// 查询资产导入任务信息
	public AssetImportTask getAssetImportTask(DruidPooledConnection conn, Long importTaskId, Long orgId, Long userId)
			throws Exception {
		return assetImportTaskRepository.getByANDKeys(conn, new String[] { "id", "org_id", "user_id" },
				new Object[] { importTaskId, orgId, userId });
	}

	// 导入任务数据
	public void importAssetsRecord(DruidPooledConnection conn, Long orgId, Long userId, String url, Long importTaskId)
			throws Exception {
		Integer sum = 0;
		JSONArray json = JSONArray.parseArray(url);
		for (int o = 0; o < json.size(); o++) {

			List<AssetImportRecord> list = new ArrayList<AssetImportRecord>();
			// 2行表头，38列，文件格式写死的
			List<List<Object>> table = ExcelUtils.readExcelOnline(json.getString(o), 2, 39, 0);
			for (List<Object> row : table) {
				AssetImportRecord asr = new AssetImportRecord();
				asr.id = IDUtils.getSimpleId();
				asr.orgId = orgId;
				asr.taskId = importTaskId;
				asr.originId = ExcelUtils.getString(row.get(0));
				asr.name = ExcelUtils.getString(row.get(1));
				asr.sn = ExcelUtils.getString(row.get(2));
				asr.assetType = ExcelUtils.getString(row.get(4));

				asr.buildTime = ExcelUtils.getString(row.get(5));
				asr.originPrice = ExcelUtils.parseDouble(row.get(6));
				asr.location = ExcelUtils.getString(row.get(7));
				asr.ownership = ExcelUtils.getString(row.get(8));
				asr.keeper = ExcelUtils.getString(row.get(9));

				asr.businessMode = ExcelUtils.getString(row.get(12));
				asr.businessTime = ExcelUtils.getString(row.get(13));
				asr.holder = ExcelUtils.getString(row.get(14));
				asr.yearlyIncome = ExcelUtils.parseDouble(row.get(15));
				asr.specType = ExcelUtils.getString(row.get(16));

				asr.estateType = ExcelUtils.getString(row.get(17));
				asr.area = ExcelUtils.parseDouble(row.get(18));
				asr.floorArea = ExcelUtils.parseDouble(row.get(19));

				JSONObject b = new JSONObject();
				b.put("east", ExcelUtils.getString(row.get(20)));
				b.put("west", ExcelUtils.getString(row.get(21)));
				b.put("south", ExcelUtils.getString(row.get(22)));
				b.put("north", ExcelUtils.getString(row.get(23)));
				asr.boundary = JSON.toJSONString(b);

				asr.locationStart = ExcelUtils.getString(row.get(24));
				asr.locationEnd = ExcelUtils.getString(row.get(25));
				asr.coordinateStart = ExcelUtils.getString(row.get(26));
				asr.coordinateEnd = ExcelUtils.getString(row.get(27));

				asr.accumulateStock = ExcelUtils.parseDouble(row.get(30));
				asr.treeNumber = ExcelUtils.parseInt(row.get(31));

				JSONObject img = new JSONObject();
				img.put("imgExt1", ExcelUtils.getString(row.get(10)));
				img.put("imgExt2", ExcelUtils.getString(row.get(11)));

				img.put("imgStart", ExcelUtils.getString(row.get(28)));
				img.put("imgEnd", ExcelUtils.getString(row.get(29)));

				img.put("imgFar", ExcelUtils.getString(row.get(32)));
				img.put("imgNear", ExcelUtils.getString(row.get(33)));
				img.put("imgFront", ExcelUtils.getString(row.get(34)));
				img.put("imgSide", ExcelUtils.getString(row.get(36)));
				img.put("imgBack", ExcelUtils.getString(row.get(35)));

				asr.imgs = JSON.toJSONString(img);
				asr.remark = ExcelUtils.getString(row.get(38));
				JSONArray arrGroups = new JSONArray();
				JSONArray temp = CodecUtils.convertCommaStringList2JSONArray(ExcelUtils.getString(row.get(37)));

				for (int i = 0; i < temp.size(); i++) {
					String ts = StringUtils.trim(temp.getString(i));
					if (ts.equals("null") || ts.equals("无")) {
						// 无和null，不加
					} else {
						arrGroups.add(ts);
					}
				}
				asr.groups = ORGUserService.array2JsonString(ORGUserService.checkGroups(conn, orgId, arrGroups));

				asr.status = AssetImportRecord.STATUS.UNDETECTED.v();
				sum++;
				list.add(asr);

				if (list.size() % 10 == 0) {
					assetImportRecordRepository.insertList(conn, list);
					list = new ArrayList<AssetImportRecord>();
				}
				if (sum == table.size()) {
					assetImportRecordRepository.insertList(conn, list);
				}
			}
		}
		assetImportTaskRepository.countImportTaskSum(conn, importTaskId, sum);

	}

	// 资产表回调页面（数据总数-数据列表-数据分页）
	public List<AssetImportRecord> getAssetImportRecords(DruidPooledConnection conn, Long orgId, Long importTaskId,
			Integer count, Integer offset) throws Exception {
		return assetImportRecordRepository.getListByANDKeys(conn, new String[] { "org_id", "task_id" },
				new Object[] { orgId, importTaskId }, count, offset);
	}

	private void imp(DruidPooledConnection conn, List<AssetImportRecord> assRec, Long orgId, Long importTaskId)
			throws Exception {

		for (AssetImportRecord assRe : assRec) {

			String originId = assRe.originId;
			String name = assRe.name;
			String sn = assRe.sn;
			String resType = assRe.resType;
			String assetType = assRe.assetType;

			String buildTime = assRe.buildTime;
			Double originPrice = assRe.originPrice;
			String location = assRe.location;
			String ownership = assRe.ownership;
			String keeper = assRe.keeper;

			String businessMode = assRe.businessMode;
			String businessTime = assRe.businessTime;
			String holder = assRe.holder;
			Double yearlyIncome = assRe.yearlyIncome;
			String specType = assRe.specType;

			String estateType = assRe.estateType;
			Double area = assRe.area;
			Double floorArea = assRe.floorArea;
			String boundary = assRe.boundary;
			String locationStart = assRe.locationStart;

			String locationEnd = assRe.locationEnd;
			String coordinateStart = assRe.coordinateStart;
			String coordinateEnd = assRe.coordinateEnd;
			Double accumulateStock = assRe.accumulateStock;
			Integer treeNumber = assRe.treeNumber;

			String imgs = assRe.imgs;
			String remark = assRe.remark;
			JSONArray groups = JSONArray.parseArray(assRe.groups);

			try {
				createAsset(conn, orgId, originId, name, sn, resType, assetType, buildTime, originPrice, location,
						ownership, keeper, businessMode, businessTime, holder, yearlyIncome, specType, estateType, area,
						floorArea, boundary, locationStart, locationEnd, coordinateStart, coordinateEnd,
						accumulateStock, treeNumber, imgs, remark, groups);
				assRe.status = AssetImportRecord.STATUS.COMPLETION.v();
				assetImportRecordRepository.updateStatus(conn, assRe.id, assRe.status);
				assetImportTaskRepository.countAssetImportCompletionTask(conn, importTaskId);
			} catch (Exception e) {
				assRe.status = AssetImportRecord.STATUS.NOTCOMPLETION.v();
				assetImportRecordRepository.updateStatus(conn, assRe.id, assRe.status);
				assetImportTaskRepository.countAssetImportNotCompletionTask(conn, importTaskId);
			}
		}
	}

	// 开始导入数据
	public void importAsset(Long orgId, Long importTaskId) throws Exception {
		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DataSource dsRds;
			DruidPooledConnection conn = null;
			try {
				dsRds = DataSourceUtils.getDataSource("rdsDefault");
				conn = (DruidPooledConnection) dsRds.openConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// 修改导入任务状态为正在导入
				AssetImportTask ass = new AssetImportTask();
				ass.status = AssetImportTask.STATUS.START.v();
				assetImportTaskRepository.updateByKey(conn, "id", importTaskId, ass, true);
				// 把数据取出进行处理
				AssetImportTask assTa = assetImportTaskRepository.getByKey(conn, "id", importTaskId);
				for (int i = 0; i < (assTa.sum / 100) + 1; i++) {
					List<AssetImportRecord> assRec = assetImportRecordRepository.getListByANDKeys(conn,
							new String[] { "org_id", "task_id", "status" },
							new Object[] { orgId, importTaskId, AssetImportRecord.STATUS.UNDETECTED.v() }, 100, 0);

						imp(conn, assRec, orgId, importTaskId);

				}
				ass.status = AssetImportTask.STATUS.END.v();
				assetImportTaskRepository.updateByKey(conn, "id", importTaskId, ass, true);

			} catch (Exception eee) {
				eee.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			future.complete("ok");
		}, res -> {
			System.out.println("The result is: " + res.result());
		});

	}

	public List<AssetImportRecord> getNotcompletionRecord(DruidPooledConnection conn, Long orgId, Long importTaskId,
			Integer count, Integer offset) throws Exception {
		return assetImportRecordRepository.getListByANDKeys(conn, new String[] { "org_id", "task_id", "status" },
				new Object[] { orgId, importTaskId, AssetImportRecord.STATUS.NOTCOMPLETION.v() }, count, offset);
	}

}
