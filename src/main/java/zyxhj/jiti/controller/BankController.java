package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.service.AssetService;
import zyxhj.jiti.service.BankService;
import zyxhj.jiti.service.ORGService;
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
	private AssetService assetService;
	private BankService bankService;
	private VoteService voteService;
	private ORGService orgService;

	public BankController(String node) {
		super(node);
		try {
			dsRds = DataSourceUtils.getDataSource("rdsDefault");

			assetService = Singleton.ins(AssetService.class);
			bankService = Singleton.ins(BankService.class);
			voteService = Singleton.ins(VoteService.class);
			orgService = Singleton.ins(ORGService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
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
			@P(t = "状态(可填可不填)", r = false) Byte status, //
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
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(voteService.getVoteOptions(conn, voteId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGByName", //
			des = "根据投票编号查询投票选项详情", //
			ret = "返回选项详情"//
	)
	public APIResponse getORGByName(//
			@P(t = "投票编号") String name, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(orgService.getORGByName(conn, name, count, offset)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createBankORG", //
			des = "创建银行", //
			ret = "返回银行详情"//
	)
	public APIResponse createBankORG(//
			@P(t = "区级id") Long districtId, //
			@P(t = "银行名称") String name, //
			@P(t = "银行地址") String address, //
			@P(t = "银行机构代码") String code //

	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(bankService.createBankORG(conn, districtId, name, address, code)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editBankORG", //
			des = "修改银行信息", //
			ret = "返回影响记录数"//
	)
	public APIResponse editBankORG(//
			@P(t = "银行机构id") Long bankId, //
			@P(t = "银行名称") String name, //
			@P(t = "银行地址") String address //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(bankService.editBankORG(conn, bankId, name, address)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "deleteBankORG", //
			des = "删除银行机构", //
			ret = "返回影响记录数"//
	)
	public APIResponse deleteBankORG(//
			@P(t = "银行机构id") Long bankId //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils.checkNull(bankService.deleteBankORG(conn, bankId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createBankAdmin", //
			des = "创建银行管理员", //
			ret = "返回详情"//
	)
	public APIResponse createBankAdmin(//
			@P(t = "银行机构id") Long bankId, //
			@P(t = "银行机构地址d") String address, //
			@P(t = "银行管理员身份证号") String idNumber, //
			@P(t = "银行管理员手机号") String mobile, //
			@P(t = "银行管理员密码") String pwd, //
			@P(t = "银行管理员真名") String realName //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils
					.checkNull(bankService.createBankAdmin(conn, bankId, address, idNumber, mobile, pwd, realName)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getBankAdmin", //
			des = "获取银行管理员", //
			ret = "返回详情"//
	)
	public APIResponse getBankAdmin(//
			@P(t = "银行机构id") Long bankId, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(bankService.getBankAdmin(conn, bankId, count, offset)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "deleteBankAdmin", //
			des = "删除银行管理员", //
			ret = "返回详情"//
	)
	public APIResponse deleteBankAdmin(//
			@P(t = "银行机构id") Long bankId, //
			@P(t = "用户id") Long userId //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse
					.getNewSuccessResp(ServiceUtils.checkNull(bankService.deleteBankAdmin(conn, bankId, userId)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getBankList", //
			des = "获取银行机构列表", // 
			ret = "返回银行机构列表"//
	)
	public APIResponse getBankList(//
			@P(t = "区级机构id") Long districtId, //
			@P(t = "需要查询的名称", r = false) String name, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(bankService.getBankList(conn, districtId, name, count, offset)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getORGByBank", //
			des = "获取组织机构列表", //
			ret = "返回组织机构列表"//
	)
	public APIResponse getORGByBank(//
			@P(t = "银行机构id") Long bankId, //
			@P(t = "需要查询的名称", r = false) String name, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = (DruidPooledConnection) dsRds.openConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(bankService.getORGByBank(conn, bankId, name, count, offset)));
		}
	}

}
