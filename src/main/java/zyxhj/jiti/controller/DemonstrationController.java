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
			ret = "用户选票对象"//
	)
	public APIResponse getAsset(//
			@P(t = "投票编号") String groups, //
			@P(t = "总记录",r=false) Integer count,//
			@P(t = "第几条纪录开始",r=false) Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(demonstrationService.getAsset(conn, groups,count,offset));
		}
	}
	
}
