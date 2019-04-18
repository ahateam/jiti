package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.service.AssetService;
import zyxhj.jiti.service.BankService;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.ORGUserGroupService;
import zyxhj.jiti.service.ORGUserService;
import zyxhj.jiti.service.VoteService;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;

public class BankController extends Controller {

	private static Logger log = LoggerFactory.getLogger(BankController.class);

	private DataSource dsRds;
	private ORGService orgService;
	private ORGUserService orgUserService;
	private ORGUserGroupService orgUserGroupService;
	private AssetService assetService;
	private BankService bankService;
	private VoteService voteService;

	public BankController(String node) {
		super(node);
		try {
			dsRds = DataSourceUtils.getDataSource("rdsDefault");

			orgService = Singleton.ins(ORGService.class);
			orgUserService = Singleton.ins(ORGUserService.class);

			orgUserGroupService = Singleton.ins(ORGUserGroupService.class);
			assetService = Singleton.ins(AssetService.class);
			bankService = Singleton.ins(BankService.class);
			voteService = Singleton.ins(VoteService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "bankAdminLoginInORG", //
			des = "银行管理员登陆", //
			ret = "ORGLoginBo对象，包含user，session及org等信息"//
	)
	public APIResponse bankAdminLoginInORG(//
			@P(t = "用户编号") Long userId, //
			@P(t = "组织编号") Long orgId//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.bankAdminLoginInORG(conn, userId, orgId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPro", //
			des = "获取省", //
			ret = "返回省列表"//
	)
	public APIResponse getPro(//
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(bankService.getPro(conn, count, offset)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getDownORG", //
			des = "获取下级组织", //
			ret = "下级组织列表"//
	)
	public APIResponse getDownORG(//
			@P(t = "组织编号") Long orgId, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.getDownORG(conn, orgId, count, offset)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetsBySn", //
			des = "根据id和证件号模糊查询资产信息", //
			ret = "返回资产信息"//
	)
	public APIResponse getAssetsBySn(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "需要查询的内容") String sn, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(assetService.getAssetsBySn(conn, orgId, sn, count, offset)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getAssetsByName", //
			des = "根据id和名称模糊查询资产信息", //
			ret = "返回资产信息"//
	)
	public APIResponse getAssetsByName(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "需要查询的内容") String name, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(assetService.getAssetsByName(conn, orgId, name, count, offset)));
		}
	}
	
	
	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVotes", //
			des = "根据组织id查询组织投票列表", //
			ret = "返回投票列表"//
	)
	public APIResponse getVotes(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "状态(可填可不填)",r = false) Byte status, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(voteService.getVotes(conn, orgId, status, count, offset)));
		}
	}
	
	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVoteOptions", //
			des = "根据投票编号查询投票选项详情", //
			ret = "返回选项详情"//
	)
	public APIResponse getVoteOptions(//
			@P(t = "投票编号") Long voteId //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(voteService.getVoteOptions(conn, voteId)));
		}
	}
	
	
}
