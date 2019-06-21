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
import com.alicloud.openservices.tablestore.model.PrimaryKeyValue;
import com.alicloud.openservices.tablestore.model.RowChange;

import zyxhj.core.domain.ImportTempRecord;
import zyxhj.core.repository.ImportTaskRepository;
import zyxhj.core.repository.ImportTempRecordRepository;
import zyxhj.utils.ExcelUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.ts.ColumnBuilder;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.RowChangeBuilder;
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

			List<List<Column>> batchRows = new ArrayList<>();
			for (List<Object> row : table) {
				// int tt = 0;
				ColumnBuilder cb = new ColumnBuilder();
				cb.add("orgId", orgId);
				cb.add("status", (long) ImportTempRecord.STATUS.PENDING.v());
				for (int i = 0; i < 17; i++) {
					cb.add("Col" + i, ExcelUtils.getString(row.get(i)));
				}

				List<Column> list = cb.build();

				batchRows.add(list);

				if (batchRows.size() >= 10) {
					RowChangeBuilder rcb = new RowChangeBuilder();
					for (List<Column> cc : batchRows) {
						rcb.put("ImportTempRecord", pk, cc, true);
					}

					TSRepository.nativeBatchWrite(client, rcb.build());
					batchRows.clear();
				}
			}

			if (batchRows.size() > 0) {
				RowChangeBuilder rcb = new RowChangeBuilder();
				for (List<Column> cc : batchRows) {
					rcb.put("ImportTempRecord", pk, cc, true);
				}

				TSRepository.nativeBatchWrite(client, rcb.build());
				batchRows.clear();
			}

		}

	}

	public void deleteImportTask(SyncClient client, Long importTaskId) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("taskId", importTaskId).addAutoIncermentKey("recordId").build();
		TSRepository.nativeDel(client, "ImportTempRecord", pk);
	}

	public JSONArray getListImportTemp(SyncClient client, Long importTaskId) throws Exception {
		// TSQL ts = new TSQL();
		// ts.setFirstTerm("taskId", importTaskId);
		// ts.setLimit(100);
		// ts.setOffset(0);
		//
		// SearchQuery myQuery = ts.build();

		// 设置起始主键
		PrimaryKey pkStart = new PrimaryKeyBuilder().add("taskId", importTaskId)
				.add("recordId", PrimaryKeyValue.INF_MIN).build();

		// 设置结束主键
		PrimaryKey pkEnd = new PrimaryKeyBuilder().add("taskId", importTaskId).add("recordId", PrimaryKeyValue.INF_MAX)
				.build();
		JSONArray array = tempRecordRepository.getRange(client, pkStart, pkEnd, 512, 0);
		System.out.println(JSON.toJSONString(array, true));
		return array;

//		List<PrimaryKey> pks = new ArrayList<>();
//		pks.add(new PrimaryKeyBuilder().add("taskId", importTaskId).build());
//
//		try {
//			JSONArray array = TSRepository.nativeBatchGet(client, "ImportTempRecord", pks);
//			System.out.println(JSON.toJSONString(array, true));
//			return array;
//		} catch (Exception e) {
//			return null;
//		}
		// return tempRecordRepository.search(client, "ImportTempRecordIndex", myQuery);
	}

}
