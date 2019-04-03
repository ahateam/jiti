package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.service.DemonstrationService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;

public class DemonstrationController extends Controller {

	private static Logger log = LoggerFactory.getLogger(DemonstrationController.class);

	private DataSource dsRds;
	private DemonstrationService demonstrationService;

	public DemonstrationController(String node) {
		super(node);
		try {
			dsRds = DataSourceUtils.getDataSource("rdsDefault");

			demonstrationService = Singleton.ins(DemonstrationService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	
	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAsset", //
			des = "获取资产信息", //
			ret = "返回分组信息"//
	)
	public APIResponse getAsset(//
			@P(t = "分组编号") String groups //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(demonstrationService.getAsset(conn, groups));
		}
	}
	
	/**
	 * 
	 */
	@POSTAPI(//
			path = "getGroup", //
			des = "获取 所有的分组列表", //
			ret = "分组列表"//
	)
	public APIResponse getGroup(//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(demonstrationService.getGroup(conn));
		}
	}
	
	
	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetById", //
			des = "根据资产id查询资产列表", //
			ret = "资产列表"//
	)
	public APIResponse getAssetById(//
			@P(t = "资产id") Long assetId //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(demonstrationService.getAssetById(conn,assetId));
		}
	}
}
