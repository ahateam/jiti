package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

import zyxhj.utils.Singleton;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

/**
 * 	审批流程
 * 
 * @author JXians
 *
 */
public class ApprovalProcessController extends Controller {

	private static Logger log = LoggerFactory.getLogger(ApprovalProcessController.class);

	private DruidDataSource dds;
//	private AssetService assetService;

	public ApprovalProcessController(String node) {

		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

//			assetService = Singleton.ins(AssetService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

}
