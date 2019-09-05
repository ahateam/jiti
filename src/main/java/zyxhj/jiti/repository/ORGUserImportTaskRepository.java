package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.ORGUserImportTask;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserImportTaskRepository extends RDSRepository<ORGUserImportTask> {

	public ORGUserImportTaskRepository() {
		super(ORGUserImportTask.class);
	}

	public void countImportTaskSum(DruidPooledConnection conn, Long importTaskId, Integer sum) throws Exception {
		this.update(conn, StringUtils.join("SET sum = ", sum), null, "id = ? ", Arrays.asList(importTaskId));
	}

	public void countORGUserImportCompletionTask(DruidPooledConnection conn, Long importTaskId) throws Exception {
		StringBuffer sb = new StringBuffer();
		EXP sql = EXP.INS().key("id", importTaskId);
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb,params);
		this.update(conn, "SET success = success+1,completion = completion + 1", null, sb.toString(), params);
//		this.update(conn, StringUtils.join("SET success = success+1,completion = completion + 1"), null, sb.toString(),
//				sql.getParams());
	}

	public void countORGUserImportNotCompletionTask(DruidPooledConnection conn, Long importTaskId) throws Exception {
		StringBuffer sb = new StringBuffer();

		EXP sql = EXP.INS().key("id", importTaskId);
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb,params);
		this.update(conn, "SET success = success+1,completion = completion + 1", null, sb.toString(), params);
		
//		this.update(conn, StringUtils.join("SET success = success+1,not_completion = not_completion + 1"), null,
//				sb.toString(), sql.getParams());
	}

	public List<ORGUserImportTask> getORGUserImportTasks(DruidPooledConnection conn, Long orgId, Long userId,
			Integer count, Integer offset) throws Exception {
		EXP sql = EXP.INS().key("org_id", orgId).andKey("user_id", userId).append("ORDER BY create_time DESC");
		return this.getList(conn, sql, count, offset);
	}

}
