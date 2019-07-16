package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alicloud.openservices.tablestore.SyncClient;

import zyxhj.core.domain.ImportTask;
import zyxhj.jiti.service.ImportTaskService;
import zyxhj.jiti.service.SortService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class SortController extends Controller {

	private static Logger log = LoggerFactory.getLogger(SortController.class);

	private SyncClient client;

	private DruidDataSource dds;
	private ImportTaskService importTaskService;
	private SortService sortService;

	public SortController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");
			client = DataSource.getTableStoreSyncClient("tsDefault.prop");

			importTaskService = Singleton.ins(ImportTaskService.class);
			sortService = Singleton.ins(SortService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@ENUM(des = "导入任务状态")
	public ImportTask.STATUS[] voteStatus = ImportTask.STATUS.values();

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createAdvert", //
			des = "创建广告", //
			ret = "返回创建广告信息"//
	)
	public APIResponse createAdvert(//
			@P(t = "标题") String title, //
			@P(t = "存放图片地址") String data, //
			@P(t = "存放链接地址") String tags, //
			@P(t = "广告类型") Byte type //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(sortService.createAdvert(conn, title, data, tags, type));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editAdvert", //
			des = "修改广告", //
			ret = "返回创建广告信息"//
	)
	public APIResponse editAdvert(//
			@P(t = "标题") Long adverId, //
			@P(t = "标题") String title, //
			@P(t = "存放图片地址") String data, //
			@P(t = "存放链接地址") String tags //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(sortService.editAdvert(conn, adverId, title, data, tags));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delAdvert", //
			des = "删除广告", //
			ret = ""//
	)
	public APIResponse delAdvert(//
			@P(t = "标题") Long adverId //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			sortService.delAdvert(conn, adverId);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAdverts", //
			des = "获取广告", //
			ret = "返回广告信息"//
	)
	public APIResponse getAdverts(//
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(sortService.getAdverts(conn, count, offset));
		}
	}

}
