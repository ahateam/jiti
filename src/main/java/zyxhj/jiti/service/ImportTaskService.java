package zyxhj.jiti.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.Direction;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;

import io.vertx.core.Vertx;
import zyxhj.core.domain.ImportTask;
import zyxhj.core.domain.ImportTempRecord;
import zyxhj.core.repository.ImportTaskRepository;
import zyxhj.core.repository.ImportTempRecordRepository;
import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.repository.AssetImportTaskRepository;
import zyxhj.utils.CodecUtils;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.ts.ColumnBuilder;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.RowChangeBuilder;
import zyxhj.utils.data.ts.TSQL;
import zyxhj.utils.data.ts.TSQL.OP;
import zyxhj.utils.data.ts.TSRepository;

public class ImportTaskService {

	private static Logger log = LoggerFactory.getLogger(ImportTaskService.class);

	private ImportTaskRepository taskRepository;
	private ImportTempRecordRepository tempRecordRepository;
	private AssetImportTaskRepository assetImportTaskRepository;
	private AssetService assetService;
	private ORGUserService orgUserService;

	public ImportTaskService() {
		try {

			taskRepository = Singleton.ins(ImportTaskRepository.class);
			tempRecordRepository = Singleton.ins(ImportTempRecordRepository.class);
			assetImportTaskRepository = Singleton.ins(AssetImportTaskRepository.class);
			assetService = Singleton.ins(AssetService.class);
			orgUserService = Singleton.ins(ORGUserService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void createImportTask(DruidPooledConnection conn, String title, Long orgId, Long userId, Byte type)
			throws Exception {
		ImportTask imp = new ImportTask();
		if (type == 0) {
			imp.origin = "user";
		} else if (type == 1) {
			imp.origin = "asset";
		}
		imp.id = IDUtils.getSimpleId();
		imp.orgId = orgId;
		imp.title = title;
		imp.userId = userId;
		imp.createTime = new Date();
		imp.startTime = new Date();
		imp.finishTime = new Date();
		imp.amount = 0;
		imp.completedCount = 0;
		imp.successCount = 0;
		imp.failureCount = 0;
		imp.status = ImportTask.STATUS.WAITING.v();
		taskRepository.insert(conn, imp);
	}

	// 查询组织导入
	public List<ImportTask> getListImportTask(DruidPooledConnection conn, Long orgId, Byte type, Integer count,
			Integer offset) throws Exception {
		return taskRepository.getListImportTask(conn, orgId, type, count, offset);
	}

	/**
	 * 导入到临时表
	 * 
	 * @param importTaskId 导入id
	 * @param skipRowCount 第几行开始
	 * @param colCount     总列数
	 */
	public void importRecord(SyncClient client, DruidPooledConnection conn, Long orgId, Long userId, String url,
			Long importTaskId, Integer skipRowCount, Integer colCount) throws Exception {

		PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId).addAutoIncermentKey("recordId").build();
		JSONArray json = JSONArray.parseArray(url);
		Integer count = 0;// 总条数
		List<List<Object>> table = null;
		for (int o = 0; o < json.size(); o++) {
			table = ExcelUtils.readExcelOnline(json.getString(o), skipRowCount, colCount, 0);

			List<List<Column>> batchRows = new ArrayList<>();
			for (List<Object> row : table) {
				ColumnBuilder cb = new ColumnBuilder();
				cb.add("orgId", orgId);
				cb.add("status", (long) ImportTempRecord.STATUS.PENDING.v());
				for (int i = 0; i < colCount; i++) {
					cb.add(StringUtils.join("Col", i), ExcelUtils.getString(row.get(i)));
				}

				count++;
				List<Column> list = cb.build();

				batchRows.add(list);

				if (batchRows.size() >= 10) {
					RowChangeBuilder rcb = new RowChangeBuilder();
					for (List<Column> cc : batchRows) {
						rcb.put(tempRecordRepository.getTableName(), pk, cc, true);
					}

					TSRepository.nativeBatchWrite(client, rcb.build());
					batchRows.clear();
				}
			}

			if (batchRows.size() > 0) {
				RowChangeBuilder rcb = new RowChangeBuilder();
				for (List<Column> cc : batchRows) {
					rcb.put(tempRecordRepository.getTableName(), pk, cc, true);
				}

				TSRepository.nativeBatchWrite(client, rcb.build());
				batchRows.clear();
			}
		}

		// 修改导入任务总数
		ImportTask imp = new ImportTask();
		imp.amount = count;
		imp.startTime = new Date();
		imp.status = ImportTask.STATUS.FILE_READY.v();
		taskRepository.update(conn, EXP.INS().key("id", importTaskId), imp, true);

	}

	// 开始导入用户
	public void importOrgUser(Long importTaskId) throws Exception {

		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DruidDataSource dds;
			DruidPooledConnection conn = null;
			SyncClient client = null;
			try {
				dds = DataSource.getDruidDataSource("rdsDefault.prop");
				conn = (DruidPooledConnection) dds.getConnection();
				client = DataSource.getTableStoreSyncClient("tsDefault.prop");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				ImportTask task = taskRepository.get(conn, EXP.INS().key("id", importTaskId));
				Integer amount = task.amount;
				Integer offset = 0;
				for (int k = 0; k < amount / 100 + 1; k++) {
					// 根据taskid去获取导入表
					JSONArray listImportTemp = getListImportTemp(client, importTaskId, 100, offset);
					// 遍历获取到的数据
					for (int i = 0; i < listImportTemp.size(); i++) {
						//// 将数据处理后放入到集合中
						int co = 0;// 定义一个变量来循环
						// 修改导入任务为正在导入
						ImportTask ta = new ImportTask();
						ta.status = ImportTask.STATUS.PROGRESSING.v();
						taskRepository.update(conn, EXP.INS().key("id", importTaskId), ta, true);

						// 获取导入数据
						JSONObject data = JSONObject.parseObject(listImportTemp.getString(i));
						Long orgId = data.getLong("orgId");
						Long recordId = data.getLong("recordId");

						String fa = data.getString(StringUtils.join("Col", co++)); // 户序号

						if (StringUtils.isBlank(fa)) {
							// 户序号为空，直接跳过
							log.error("---->>户序号为空");
							continue;
						}
						Long familyNumber = Long.parseLong(fa);

						String realName = data.getString(StringUtils.join("Col", co++));
						String idNumber = data.getString(StringUtils.join("Col", co++));

						// 性别
						Byte sex = 0;
						if (data.getString(StringUtils.join("Col", co++)).equals("女")) {
							sex = 1;
						}
						System.out.println(sex);

						// 与户主关系
						String familyRelations = data.getString(StringUtils.join("Col", co++));

						String mobile = data.getString(StringUtils.join("Col", co++));
						Double shareAmount = data.getDouble(StringUtils.join("Col", co++));

						// 资源股
						Double resourceShares = data.getDouble(StringUtils.join("Col", co++));
						// 资产股
						Double assetShares = data.getDouble(StringUtils.join("Col", co++));
						// 是否为组织成员
						Boolean isORGUser = true;
						if (data.getString(StringUtils.join("Col", co++)).equals("否")) {
							isORGUser = false;
						}

						Integer weight = data.getInteger(StringUtils.join("Col", co++));
						String address = data.getString(StringUtils.join("Col", co++));
						String familyMaster = data.getString(StringUtils.join("Col", co++));
						Boolean shareCerHolder = false;
						if (data.getString(StringUtils.join("Col", co++)).equals("是")) {
							shareCerHolder = true;
						}
						String shareCerNo = data.getString(StringUtils.join("Col", co++));

						String dutyShareholders = data.getString(StringUtils.join("Col", co++));
						String dutyDirectors = data.getString(StringUtils.join("Col", co++));
						String dutyVisors = data.getString(StringUtils.join("Col", co++));
						String dutyOthers = data.getString(StringUtils.join("Col", co++));
						String dutyAdmins = data.getString(StringUtils.join("Col", co++));

						String groups = data.getString(StringUtils.join("Col", co++));
						String tags = data.getString(StringUtils.join("Col", co++));

						// 合并roles
						JSONArray roles = new JSONArray();
						JSONArray temp = null;
						{
							// 股东成员职务
							String ts = StringUtils.trim(dutyShareholders);
							if (ts.equals(ORGUserRole.role_shareHolder.name)) {
								roles.add(ORGUserRole.role_shareHolder.roleId);// 股东
							} else if (ts.equals(ORGUserRole.role_shareDeputy.name)) {
								roles.add(ORGUserRole.role_shareDeputy.roleId);// 股东代表
							} else if (ts.equals(ORGUserRole.role_shareFamily.name)) {
								roles.add(ORGUserRole.role_shareFamily.roleId);// 股东户代表
							} else {
								// 无，不加
							}
						}

						{
							// 董事会职务
							String ts = StringUtils.trim(dutyDirectors);
							if (ts.equals(ORGUserRole.role_director.name)) {
								roles.add(ORGUserRole.role_director.roleId);// 董事
							} else if (ts.equals(ORGUserRole.role_dirChief.name)) {
								roles.add(ORGUserRole.role_dirChief.roleId);// 董事长
							} else if (ts.equals(ORGUserRole.role_dirVice.name)) {
								roles.add(ORGUserRole.role_dirVice.roleId);// 副董事长
							} else {
								// 无，不加
							}
						}

						{
							// 监事会职务
							String ts = StringUtils.trim(dutyVisors);
							if (ts.equals(ORGUserRole.role_supervisor.name)) {
								roles.add(ORGUserRole.role_supervisor.roleId);// 监事
							} else if (ts.equals(ORGUserRole.role_supChief.name)) {
								roles.add(ORGUserRole.role_supChief.roleId);// 监事长
							} else if (ts.equals(ORGUserRole.role_supVice.name)) {
								roles.add(ORGUserRole.role_supVice.roleId);// 副监事长
							} else {
								// 无，不加
							}
						}

						{
							// 其它管理角色
							temp = CodecUtils.convertCommaStringList2JSONArray(dutyAdmins);
							for (int j = 0; j < temp.size(); j++) {
								String ts = StringUtils.trim(temp.getString(j));

								if (ts.equals(ORGUserRole.role_user.name)) {
									roles.add(ORGUserRole.role_user.roleId);// 用户
								} else if (ts.equals(ORGUserRole.role_outuser.name)) {
									roles.add(ORGUserRole.role_outuser.roleId);// 外部人员
								} else if (ts.equals(ORGUserRole.role_admin.name)) {
									roles.add(ORGUserRole.role_admin.roleId);// 管理员
								} else {
									// 无，不填默认当作用户
									roles.add(ORGUserRole.role_user.roleId);// 用户
								}
							}
						}

						{

							// TODO 这个地方要跟系统中的其它角色做匹配
							// 其它角色
							temp = CodecUtils.convertCommaStringList2JSONArray(dutyOthers);

							// for (int i = 0; i < temp.size(); i++) {
							// String ts = StringUtils.trim(temp.getString(i));
							// if (ts.equals("null") || ts.equals("无")) {
							// // 无和null，不加
							// } else {
							// roles.add(ts);
							// }
							// }
						}

						// 开始处理分组和标签

						JSONArray arrGroups = new JSONArray();

						{
							// 分组
							temp = CodecUtils.convertCommaStringList2JSONArray(groups);

							for (int j = 0; j < temp.size(); j++) {
								String ts = StringUtils.trim(temp.getString(j));
								if (ts.equals("null") || ts.equals("无")) {
									// 无和null，不加
								} else {
									arrGroups.add(ts);
								}
							}

						}

						JSONObject joTags = new JSONObject();
						JSONArray arrTags = new JSONArray();
						{
							// 标签
							temp = CodecUtils.convertCommaStringList2JSONArray(tags);

							for (int j = 0; j < temp.size(); j++) {
								String ts = StringUtils.trim(temp.getString(j));
								if (ts.equals("null") || ts.equals("无")) {
									// 无和null，不加
								} else {
									arrTags.add(ts);
								}
							}

							if (arrTags.size() > 0) {
								joTags.put("tags", arrTags);
							}
						}

						// System.out.println("----------" + JSON.toJSONString(joTags));

						// 处理idNunber为空的问题
						if (StringUtils.isBlank(idNumber)) {
							idNumber = StringUtils.join(orgId, "-", fa, "-", IDUtils.getHexSimpleId());
						}
						if (StringUtils.isBlank(mobile)) {
							mobile = StringUtils.join(orgId, "-", fa, "-", IDUtils.getHexSimpleId());
						}

						try {
							orgUserService.createORGUser(conn, orgId, mobile, realName, idNumber, sex, familyRelations,
									resourceShares, assetShares, isORGUser, address, shareCerNo, "", shareCerHolder,
									shareAmount, weight, roles, arrGroups, joTags, familyNumber, familyMaster);
							// 修改资产状态为成功
							PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId)
									.add("recordId", recordId).build();
							ColumnBuilder cb = new ColumnBuilder();
							cb.add("status", (long) ImportTempRecord.STATUS.SUCCESS.v());
							List<Column> columns = cb.build();
							TSRepository.nativeUpdate(client, tempRecordRepository.getTableName(), pk, true, columns);

							taskRepository.countORGUserImportCompletionTask(conn, importTaskId);
						} catch (Exception e) {
							PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId)
									.add("recordId", recordId).build();
							ColumnBuilder cb = new ColumnBuilder();
							cb.add("status", (long) ImportTempRecord.STATUS.FAILURE.v());
							cb.add("result", e.getMessage());
							List<Column> columns = cb.build();
							TSRepository.nativeUpdate(client, tempRecordRepository.getTableName(), pk, true, columns);
							taskRepository.countORGUserImportNotCompletionTask(conn, importTaskId);
						}
					}
					offset = offset + 100;
				}
				// 执行完成 修改任务表里成功与失败数量
				ImportTask imp = new ImportTask();
				imp.finishTime = new Date();
				imp.status = ImportTask.STATUS.COMPLETED.v();
				taskRepository.update(conn, EXP.INS().key("id", importTaskId), imp, true);
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

	// 开始导入资产
	public void importAsset(Long importTaskId) throws Exception {

		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DruidDataSource dds;
			DruidPooledConnection conn = null;
			SyncClient client = null;
			try {
				dds = DataSource.getDruidDataSource("rdsDefault.prop");
				conn = (DruidPooledConnection) dds.getConnection();
				client = DataSource.getTableStoreSyncClient("tsDefault.prop");
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {

				// 修改导入任务为正在导入
				ImportTask imp = new ImportTask();
				imp.status = ImportTask.STATUS.PROGRESSING.v();
				taskRepository.update(conn, EXP.INS().key("id", importTaskId), imp, true);

				// 根据taskid去获取导入表
				ImportTask task = taskRepository.get(conn, EXP.INS().key("id", importTaskId));
				Integer amount = task.amount;
				Integer offset = 0;
				for (int k = 0; k < amount / 100 + 1; k++) {
					JSONArray listImportTemp = getListImportTemp(client, importTaskId, 100, offset);
					// 遍历获取到的数据
					for (int i = 0; i < listImportTemp.size(); i++) {
						//// 将数据处理后放入到集合中
						int co = 0;// 定义一个变量来循环
						// 修改导入任务为正在导入
						AssetImportTask ass = new AssetImportTask();
						ass.status = AssetImportTask.STATUS.START.v();
						assetImportTaskRepository.update(conn, EXP.INS().key("id", importTaskId), ass, true);

						// 获取导入数据
						JSONObject data = JSONObject.parseObject(listImportTemp.getString(i));
						Long orgId = data.getLong("orgId");
						String originId = data.getString(StringUtils.join("Col", co++));
						String name = data.getString(StringUtils.join("Col", co++));
						String sn = data.getString(StringUtils.join("Col", co++));
						String resType = data.getString(StringUtils.join("Col", co++));
						String assetType = data.getString(StringUtils.join("Col", co++));

						String buildTime = data.getString(StringUtils.join("Col", co++));
						Double originPrice = data.getDouble(StringUtils.join("Col", co++));
						String location = data.getString(StringUtils.join("Col", co++));
						String ownership = data.getString(StringUtils.join("Col", co++));
						String keeper = data.getString(StringUtils.join("Col", co++));

						String imgExt1 = data.getString(StringUtils.join("Col", co++));
						String imgExt2 = data.getString(StringUtils.join("Col", co++));

						String businessMode = data.getString(StringUtils.join("Col", co++));
						String businessTime = data.getString(StringUtils.join("Col", co++));
						String holder = data.getString(StringUtils.join("Col", co++));
						Double yearlyIncome = data.getDouble(StringUtils.join("Col", co++));
						String specType = data.getString(StringUtils.join("Col", co++));

						String estateType = data.getString(StringUtils.join("Col", co++));
						Double area = data.getDouble(StringUtils.join("Col", co++));
						Double floorArea = data.getDouble(StringUtils.join("Col", co++));

						JSONObject b = new JSONObject();
						b.put("east", data.getString(StringUtils.join("Col", co++)));
						b.put("west", data.getString(StringUtils.join("Col", co++)));
						b.put("south", data.getString(StringUtils.join("Col", co++)));
						b.put("north", data.getString(StringUtils.join("Col", co++)));
						String boundary = JSON.toJSONString(b);

						String locationStart = data.getString(StringUtils.join("Col", co++));
						String locationEnd = data.getString(StringUtils.join("Col", co++));
						String coordinateStart = data.getString(StringUtils.join("Col", co++));
						String coordinateEnd = data.getString(StringUtils.join("Col", co++));

						String imgStart = data.getString(StringUtils.join("Col", co++));
						String imgEnd = data.getString(StringUtils.join("Col", co++));

						Double accumulateStock = data.getDouble(StringUtils.join("Col", co++));
						Integer treeNumber = data.getInteger(StringUtils.join("Col", co++));

						String imgFar = data.getString(StringUtils.join("Col", co++));
						String imgNear = data.getString(StringUtils.join("Col", co++));
						String imgFront = data.getString(StringUtils.join("Col", co++));
						String imgBack = data.getString(StringUtils.join("Col", co++));
						String imgSide = data.getString(StringUtils.join("Col", co++));
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
						String groups = data.getString(StringUtils.join("Col", co++));
						String remark = data.getString(StringUtils.join("Col", co++));

						JSONArray arrGroups = new JSONArray();
						JSONArray temp = CodecUtils.convertCommaStringList2JSONArray(groups);

						for (int j = 0; j < temp.size(); j++) {
							String ts = StringUtils.trim(temp.getString(j));
							if (ts.equals("null") || ts.equals("无")) {
								// 无和null，不加
							} else {
								arrGroups.add(ts);
							}
						}
						Long recordId = data.getLong("recordId");
						try {
							// 创建资产
							assetService.createAsset(conn, orgId, originId, name, sn, resType, assetType, buildTime,
									originPrice, location, ownership, keeper, businessMode, businessTime, holder,
									yearlyIncome, specType, estateType, area, floorArea, boundary, locationStart,
									locationEnd, coordinateStart, coordinateEnd, accumulateStock, treeNumber, imgs,
									remark, arrGroups);
							// 修改资产状态为成功
							PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId)
									.add("recordId", recordId).build();
							ColumnBuilder cb = new ColumnBuilder();
							cb.add("status", (int) ImportTempRecord.STATUS.SUCCESS.v());
							List<Column> columns = cb.build();
							TSRepository.nativeUpdate(client, tempRecordRepository.getTableName(), pk, true, columns);
							taskRepository.countORGUserImportCompletionTask(conn, importTaskId);
						} catch (Exception e) {
							PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId)
									.add("recordId", recordId).build();
							ColumnBuilder cb = new ColumnBuilder();
							cb.add("status", ImportTempRecord.STATUS.FAILURE.v());
							cb.add("result", e.getLocalizedMessage());
							List<Column> columns = cb.build();
							TSRepository.nativeUpdate(client, tempRecordRepository.getTableName(), pk, true, columns);
							taskRepository.countORGUserImportNotCompletionTask(conn, importTaskId);
						}
					}
					offset = offset + 100;
				}
				// 执行完成 修改任务表里成功与失败数量
				imp.finishTime = new Date();
				imp.status = ImportTask.STATUS.COMPLETED.v();
				taskRepository.update(conn, EXP.INS().key("id", importTaskId), imp, true);

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

	public void deleteImportTask(SyncClient client, Long importTaskId) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId).addAutoIncermentKey("recordId").build();
		TSRepository.nativeDel(client, "ImportTempRecord", pk);
	}

	public JSONArray getListImportTemp(SyncClient client, Long importTaskId, Integer count, Integer offset)
			throws Exception {

		// 设置起始主键
		PrimaryKey pkStart = new PrimaryKeyBuilder().add("taskId", importTaskId)
				.add("recordId", PrimaryKeyValue.INF_MIN).build();

		// 设置结束主键
		PrimaryKey pkEnd = new PrimaryKeyBuilder().add("taskId", importTaskId).add("recordId", PrimaryKeyValue.INF_MAX)
				.build();
		return tempRecordRepository.getRange(client, Direction.FORWARD, pkStart, pkEnd, count, offset);

	}

	public JSONObject getFailImportRecord(SyncClient client, Long importTaskId, Integer count, Integer offset)
			throws Exception {
		TSQL ts = new TSQL();
		ts.Term(OP.AND, "status", (long) ImportTempRecord.STATUS.FAILURE.v()).Term(OP.AND, "taskId", importTaskId);
		ts.setLimit(count);
		ts.setOffset(offset);
		ts.setGetTotalCount(true);
		SearchQuery query = ts.build();
		return TSRepository.nativeSearch(client, tempRecordRepository.getTableName(), "ImportTempRecordIndex", query);
	}

	public ImportTask getImportTask(DruidPooledConnection conn, Long importTaskId) throws Exception {
		return taskRepository.get(conn, EXP.INS().key("id", importTaskId));
	}

}
