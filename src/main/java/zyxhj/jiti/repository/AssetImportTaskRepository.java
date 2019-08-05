package zyxhj.jiti.repository;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.AssetImportTask;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class AssetImportTaskRepository extends RDSRepository<AssetImportTask> {

	public AssetImportTaskRepository() {
		super(AssetImportTask.class);
	}

	public void countImportTaskSum(DruidPooledConnection conn, Long importTaskId, Integer sum) throws Exception {
		this.update(conn, StringUtils.join("SET sum = ", sum), null, " WHERE id = ? ",
				Arrays.asList(importTaskId));
	}

	public void countAssetImportCompletionTask(DruidPooledConnection conn, Long importTaskId) throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx("id = ? ", importTaskId);
		sql.fillSQL(sb);
		this.update(conn, StringUtils.join("SET success = success+1,completion = completion + 1"), null, sb.toString(),
				sql.getParams());
	}

	public void countAssetImportNotCompletionTask(DruidPooledConnection conn, Long importTaskId) throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx("id = ? ", importTaskId);
		sql.fillSQL(sb);
		this.update(conn, StringUtils.join("SET success = success+1,not_completion = not_completion + 1"), null,
				sb.toString(), sql.getParams());
	}

}
