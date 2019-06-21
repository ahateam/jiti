package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import zyxhj.jiti.service.ImportTaskService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;
import zyxhj.utils.data.ts.TSAutoCloseableClient;

public class ImportController extends Controller {

	private static Logger log = LoggerFactory.getLogger(ImportController.class);

	private TSAutoCloseableClient client;

	private DataSource dsRds;
	private ImportTaskService importTaskService;

	public ImportController(String node) {
		super(node);
		try {
			dsRds = DataSourceUtils.getDataSource("rdsDefault");
			client = (TSAutoCloseableClient) DataSourceUtils.getDataSource("tsDefault").openConnection();

			importTaskService = Singleton.ins(ImportTaskService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "inportRecord", //
			des = "获取省", //
			ret = "返回省列表"//
	)
	public APIResponse getPro(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "excel文件url") String url, //
			@P(t = "导入任务id") Long importTaskId//
	) throws Exception {
		importTaskService.inportRecord(client, orgId, userId, url, importTaskId);
		return APIResponse.getNewSuccessResp();
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "deleteImportTask", //
			des = "获取省", //
			ret = "返回省列表"//
	)
	public APIResponse deleteImportTask(//
			@P(t = "导入任务id") Long importTaskId//
	) throws Exception {
		importTaskService.deleteImportTask(client, importTaskId);
		return APIResponse.getNewSuccessResp();
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getListImportTemp", //
			des = "获取省", //
			ret = "返回省列表"//
	)
	public APIResponse getListImportTemp(//
			@P(t = "导入任务id") Long importTaskId//
	) throws Exception {

		return APIResponse.getNewSuccessResp(importTaskService.getListImportTemp(client, importTaskId));
	}

}
