package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;

import zyxhj.core.domain.ImportTempRecord;
import zyxhj.core.repository.ImportTaskRepository;
import zyxhj.core.repository.ImportTempRecordRepository;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.ts.ColumnBuilder;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.TSQL;
import zyxhj.utils.data.ts.TSRepository;

public class ImportTaskService {

	private static Logger log = LoggerFactory.getLogger(ImportTaskService.class);

	private ImportTaskRepository taskRepository;
	private ImportTempRecordRepository tempRecordRepository;

	public ImportTaskService() {
		try {

			taskRepository = Singleton.ins(ImportTaskRepository.class);
			tempRecordRepository = Singleton.ins(ImportTempRecordRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 导入到临时表
	public void inportRecord(SyncClient client, Long orgId, Long userId, String url, Long importTaskId)
			throws Exception {

		PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId).addAutoIncermentKey("recordId").build();
		JSONArray json = JSONArray.parseArray(url);
		for (int o = 0; o < json.size(); o++) {
			// 1行表头，17列，文件格式写死的
			List<List<Object>> table = ExcelUtils.readExcelOnline(json.getString(o), 1, 17, 0);
			for (List<Object> row : table) {
//				int tt = 0;
				ColumnBuilder cb = new ColumnBuilder();
				cb.add("orgId", orgId);
				for (int i = 0; i < 17; i++) {
					cb.add("Col" + i, ExcelUtils.getString(row.get(i)));
				}

				List<Column> list = cb.build();

//				tempRecordRepository.insert(client, te, true);
				TSRepository.nativeInsert(client, "ImportTempRecord", pk, list, true);

//				String familyNumber = ExcelUtils.getString(row.get(tt++));// 户序号

//				if (StringUtils.isBlank(familyNumber)) {
//					// 户序号为空，直接跳过
//					log.error("---->>户序号为空");
//					continue;
//				}
//
////				ImportTempRecord te = new ImportTempRecord();
////				te.taskId = importTaskId;
////				te.recordId = null;
////				te.status = (long) ImportTempRecord.STATUS.PENDING.v();
////				te.result = "retult";
////				te.dynamicFields.put("familyNumber", Long.parseLong(familyNumber));
////				te.dynamicFields.put("orgId", orgId);
////				te.dynamicFields.put("userId", userId);
////				te.dynamicFields.put("status", ImportTempRecord.STATUS.PENDING.v());
////				te.dynamicFields.put("realName", ExcelUtils.getString(row.get(tt++)));
////
//				String idNumber = ExcelUtils.getString(row.get(tt++));
////				te.dynamicFields.put("idNumber", idNumber);
////
//				String mobile = ExcelUtils.getString(row.get(tt++));
////				te.dynamicFields.put("mobile", mobile);
////
////				te.dynamicFields.put("shareAmount", ExcelUtils.parseDouble(row.get(tt++)));
////				te.dynamicFields.put("weight", ExcelUtils.parseInt(row.get(tt++)));
////				te.dynamicFields.put("address", ExcelUtils.getString(row.get(tt++)));
////				te.dynamicFields.put("familyMaster", ExcelUtils.getString(row.get(tt++)));
////				te.dynamicFields.put("shareCerHolder", ExcelUtils.parseShiFou(row.get(tt++)));
////				te.dynamicFields.put("shareCerNo", ExcelUtils.getString(row.get(tt++)));
//
//				ColumnBuilder cb = new ColumnBuilder();
//				cb.add("familyNumber", Long.parseLong(familyNumber));
//				cb.add("orgId", orgId);
//				cb.add("userId", userId);
//				cb.add("status", 0);
//				cb.add("realName", ExcelUtils.getString(row.get(tt++)));
//
//				cb.add("idNumber", idNumber);
//
//				cb.add("mobile", mobile);
//
//				cb.add("shareAmount", ExcelUtils.parseDouble(row.get(tt++)));
//				cb.add("weight", ExcelUtils.parseInt(row.get(tt++)));
//				cb.add("address", ExcelUtils.getString(row.get(tt++)));
//				cb.add("familyMaster", ExcelUtils.getString(row.get(tt++)));
//				cb.add("shareCerHolder", ExcelUtils.parseShiFou(row.get(tt++)));
//				cb.add("shareCerNo", ExcelUtils.getString(row.get(tt++)));
//
//				String dutyShareholders = ExcelUtils.getString(row.get(tt++));
//				String dutyDirectors = ExcelUtils.getString(row.get(tt++));
//				String dutyVisors = ExcelUtils.getString(row.get(tt++));
//				String dutyOthers = ExcelUtils.getString(row.get(tt++));
//				String dutyAdmins = ExcelUtils.getString(row.get(tt++));
//
//				String groups = ExcelUtils.getString(row.get(tt++));
////				te.dynamicFields.put("groups", groups);
//				cb.add("groups", groups);
//				String tags = ExcelUtils.getString(row.get(tt++));
////				te.dynamicFields.put("tags", tags);
//				cb.add("tags", tags);
//
//				// 合并roles
//				JSONArray roles = new JSONArray();
//				JSONArray temp = null;
//				{
//					// 股东成员职务
//					String ts = StringUtils.trim(dutyShareholders);
//					if (ts.equals(ORGUserRole.role_shareHolder.name)) {
//						roles.add(ORGUserRole.role_shareHolder.roleId);// 股东
//					} else if (ts.equals(ORGUserRole.role_shareDeputy.name)) {
//						roles.add(ORGUserRole.role_shareDeputy.roleId);// 股东代表
//					} else if (ts.equals(ORGUserRole.role_shareFamily.name)) {
//						roles.add(ORGUserRole.role_shareFamily.roleId);// 股东户代表
//					} else {
//						// 无，不加
//					}
//				}
//
//				{
//					// 董事会职务
//					String ts = StringUtils.trim(dutyDirectors);
//					if (ts.equals(ORGUserRole.role_director.name)) {
//						roles.add(ORGUserRole.role_director.roleId);// 董事
//					} else if (ts.equals(ORGUserRole.role_dirChief.name)) {
//						roles.add(ORGUserRole.role_dirChief.roleId);// 董事长
//					} else if (ts.equals(ORGUserRole.role_dirVice.name)) {
//						roles.add(ORGUserRole.role_dirVice.roleId);// 副董事长
//					} else {
//						// 无，不加
//					}
//				}
//
//				{
//					// 监事会职务
//					String ts = StringUtils.trim(dutyVisors);
//					if (ts.equals(ORGUserRole.role_supervisor.name)) {
//						roles.add(ORGUserRole.role_supervisor.roleId);// 监事
//					} else if (ts.equals(ORGUserRole.role_supChief.name)) {
//						roles.add(ORGUserRole.role_supChief.roleId);// 监事长
//					} else if (ts.equals(ORGUserRole.role_supVice.name)) {
//						roles.add(ORGUserRole.role_supVice.roleId);// 副监事长
//					} else {
//						// 无，不加
//					}
//				}
//
//				{
//					// 其它管理角色
//
//					temp = CodecUtils.convertCommaStringList2JSONArray(dutyAdmins);
//					for (int i = 0; i < temp.size(); i++) {
//						String ts = StringUtils.trim(temp.getString(i));
//
//						if (ts.equals(ORGUserRole.role_user.name)) {
//							roles.add(ORGUserRole.role_user.roleId);// 用户
//						} else if (ts.equals(ORGUserRole.role_outuser.name)) {
//							roles.add(ORGUserRole.role_outuser.roleId);// 外部人员
//						} else if (ts.equals(ORGUserRole.role_admin.name)) {
//							roles.add(ORGUserRole.role_admin.roleId);// 管理员
//						} else {
//							// 无，不填默认当作用户
//							roles.add(ORGUserRole.role_user.roleId);// 用户
//						}
//					}
//				}
//
//				{
//					// TODO 这个地方要跟系统中的其它角色做匹配
//					// 其它角色
//					temp = CodecUtils.convertCommaStringList2JSONArray(dutyOthers);
//
//					// for (int i = 0; i < temp.size(); i++) {
//					// String ts = StringUtils.trim(temp.getString(i));
//					// if (ts.equals("null") || ts.equals("无")) {
//					// // 无和null，不加
//					// } else {
//					// roles.add(ts);
//					// }
//					// }
//				}
////				te.dynamicFields.put("roles", roles.toString());
//				cb.add("roles", roles.toString());
//
//				// 开始处理分组和标签
//
//				JSONArray arrGroups = new JSONArray();
//
//				{
//					// 分组
//					temp = CodecUtils.convertCommaStringList2JSONArray(groups);
//
//					for (int i = 0; i < temp.size(); i++) {
//						String ts = StringUtils.trim(temp.getString(i));
//						if (ts.equals("null") || ts.equals("无")) {
//							// 无和null，不加
//						} else {
//							arrGroups.add(ts);
//						}
//					}
////					te.dynamicFields.put("groups", arrGroups.toJSONString());
//					cb.add("groups", arrGroups.toJSONString());
//				}
//
//				JSONObject joTags = new JSONObject();
//				JSONArray arrTags = new JSONArray();
//				{
//					// 标签
//					temp = CodecUtils.convertCommaStringList2JSONArray(tags);
//
//					for (int i = 0; i < temp.size(); i++) {
//						String ts = StringUtils.trim(temp.getString(i));
//						if (ts.equals("null") || ts.equals("无")) {
//							// 无和null，不加
//						} else {
//							arrTags.add(ts);
//						}
//					}
//					if (arrTags.size() > 0) {
//						joTags.put("tags", arrTags);
//					}
////					te.dynamicFields.put("tags", joTags.toJSONString());
//					cb.add("tags", joTags.toJSONString());
//				}
//
//				// 处理idNunber为空的问题
//				if (StringUtils.isBlank(idNumber)) {
////					te.dynamicFields.put("idNumber",
////							StringUtils.join(orgId, "-", familyNumber, "-", IDUtils.getHexSimpleId()));
//					cb.add("idNumber", StringUtils.join(orgId, "-", familyNumber, "-", IDUtils.getHexSimpleId()));
//				}
//				if (StringUtils.isBlank(mobile)) {
////					te.dynamicFields.put("mobile",
////							StringUtils.join(orgId, "-", familyNumber, "-", IDUtils.getHexSimpleId()));
//					cb.add("mobile", StringUtils.join(orgId, "-", familyNumber, "-", IDUtils.getHexSimpleId()));
//				}

			}
		}

	}

	public void deleteImportTask(SyncClient client, Long importTaskId) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId).addAutoIncermentKey("recordId").build();
		TSRepository.nativeDel(client, "ImportTempRecord", pk);
	}

	public JSONArray getListImportTemp(SyncClient client, Long importTaskId) throws Exception {
//		TSQL ts = new TSQL();
//		ts.setFirstTerm("taskId", importTaskId);
//		ts.setLimit(100);
//		ts.setOffset(0);
//
//		SearchQuery myQuery = ts.build();

		List<PrimaryKey> pks = new ArrayList<>();
		pks.add(new PrimaryKeyBuilder().add("taskId", importTaskId).build());

		try {
			JSONArray array = TSRepository.nativeBatchGet(client, "ImportTempRecord", pks);
			System.out.println(JSON.toJSONString(array, true));
			return array;
		} catch (Exception e) {
			return null;
		}
//		return tempRecordRepository.search(client, "ImportTempRecordIndex", myQuery);
	}

}
