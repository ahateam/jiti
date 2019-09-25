package zyxhj.jiti.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.core.domain.ExportTask;
import zyxhj.jiti.service.ExportTaskService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class ExportTaskController extends Controller {
	
	private static Logger log = LoggerFactory.getLogger(ImportController.class);

	private DruidDataSource dds;
	private ExportTaskService taskService;
	
	public ExportTaskController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

			taskService = Singleton.ins(ExportTaskService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	@POSTAPI(path= "createExportTask",
			des = "创建导出任务",
			ret = "当前任务详情"
			)
	public ExportTask createExportTask(//
			@P(t = "任务标题") String title,//
			@P(t = "组织编号") Long orgId,//
			@P(t = "用户编号") Long userId
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return taskService.createExportTask(conn, title, orgId, userId);
		}
	}
	@POSTAPI(path= "getTaskList",
			des = "获取所有导出任务",
			ret = "当前任务详情"
			)
	public List<ExportTask> getTaskList(//
			@P(t = "组织编号") Long orgId,//
			Integer count,//
			Integer offset//
			) throws Exception{
		try (DruidPooledConnection conn = dds.getConnection()) {
			return taskService.getExportTaskList(conn, orgId, count, offset);
		}
	}
	@POSTAPI(path= "downLoadExcel",
			des = "导出数据到Excel中，并上传到OSS，下载路径存放在任务详情的fileUrl中",
			ret = "当前任务详情"
			)
	public ExportTask downLoadExcel(
			@P(t = "任务组织编号") Long taskId,//
			@P(t = "组织编号") Long orgId
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			//将数据导入到Excel中，并上传到OSS
			taskService.ExportDataIntoOSS(conn, orgId, taskId);
			//返回任务（任务完成）
			return taskService.getExportTask(conn, taskId);
		}
	}
	@POSTAPI(path= "getExportTaskById",
			des = "获取任务详情",
			ret = "当前任务详情"
			)
	public ExportTask getExportTaskById(//
			@P(t = "组织编号") Long taskId,//
			Integer count,//
			Integer offset//
			) throws Exception{
		try (DruidPooledConnection conn = dds.getConnection()) {
			return taskService.getExportTask(conn, taskId);
		}
	}
	
	
	
}
