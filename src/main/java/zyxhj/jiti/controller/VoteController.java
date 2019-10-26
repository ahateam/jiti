package zyxhj.jiti.controller;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.Vote;
import zyxhj.jiti.service.ORGService;
import zyxhj.jiti.service.VoteService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class VoteController extends Controller {

	private static Logger log = LoggerFactory.getLogger(VoteController.class);

	private DruidDataSource dds;
	private VoteService voteService;
	private ORGService orgService;

	public VoteController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

			voteService = Singleton.ins(VoteService.class);
			orgService = Singleton.ins(ORGService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@ENUM(des = "投票类型")
	public Vote.TYPE[] voteTypes = Vote.TYPE.values();

	@ENUM(des = "投票模版类型")
	public Vote.TEMPLATE[] voteTemplates = Vote.TEMPLATE.values();

	@ENUM(des = "投票状态")
	public Vote.STATUS[] voteStatus = Vote.STATUS.values();

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createVote", //
			des = "创建投票", //
			ret = "所创建的对象"//
	)
	public APIResponse createVote(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "投票模版，Vote.TEMPLATE") Byte template, //
			@P(t = "投票类型，Vote.TYPE") Byte type, //
			@P(t = "最多选择的数量") Byte choiceCount, //
			@P(t = "参加投票项目的人群（JSONObject，包含roles、groups及tags三个结构）") JSONObject crowd, //
			@P(t = "用户在有效期内是否可以重新编辑选票") Boolean reeditable, //
			@P(t = "是否实名制") Boolean realName, //
			@P(t = "是否内部投票（外部可允许任何人参与，用于意见采集）") Boolean isInternal, //
			@P(t = "是否带有弃权选项") Boolean isAbstain, //
			@P(t = "生效人数比例（百分率，50代表50%）") Byte effectiveRatio, //
			@P(t = "失效人数比例（期权人数过多就失效，百分率，50代表50%）") Byte failureRatio, //
			@P(t = "标题") String title, //
			@P(t = "备注") String remark, //
			@P(t = "扩展（JSON）") String ext, //
			@P(t = "开始时间") Date startTime, //
			@P(t = "终止时间") Date expiryTime, //
			@P(t = "角色id") String roles, //
			@P(t = "权限id") Long permissionId //

	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			orgService.userAuth(conn, orgId, roles, permissionId);
			return APIResponse.getNewSuccessResp(voteService.createVote(conn, orgId, template, type, choiceCount, crowd,
					reeditable, realName, isInternal, isAbstain, effectiveRatio, failureRatio, title, remark, ext,
					startTime, expiryTime));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editVote", //
			des = "编辑投票", //
			ret = "所创建的对象"//
	)
	public APIResponse editVote(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "投票编号") Long voteId, //
			@P(t = "投票类型，Vote.TYPE") Byte type, //
			@P(t = "最多选择的数量") Byte choiceCount, //
			@P(t = "参加投票项目的人群（JSONObject，包含roles、groups及tags三个结构）") JSONObject crowd, //
			@P(t = "用户在有效期内是否可以重新编辑选票") Boolean reeditable, //
			@P(t = "是否实名制") Boolean realName, //
			@P(t = "是否内部投票（外部可允许任何人参与，用于意见采集）") Boolean isInternal, //
			@P(t = "是否带有弃权选项") Boolean isAbstain, //
			@P(t = "生效人数比例（百分率，50代表50%）") Byte effectiveRatio, //
			@P(t = "失效人数比例（期权人数过多就失效，百分率，50代表50%）") Byte failureRatio, //
			@P(t = "标题") String title, //
			@P(t = "备注") String remark, //
			@P(t = "扩展（JSON）") String ext, //
			@P(t = "开始时间") Date startTime, //
			@P(t = "终止时间") Date expiryTime//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.editVote(conn, orgId, voteId, type, choiceCount, crowd,
					reeditable, realName, isInternal, isAbstain, effectiveRatio, failureRatio, title, remark, ext,
					startTime, expiryTime));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "setVoteActivation", //
			des = "启用/禁用投票", //
			ret = "更新影响的记录数"//
	)
	public APIResponse setVoteActivation(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "启用/禁用") Boolean activation //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.setVoteActivation(conn, voteId, activation));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "setVotePaused", //
			des = "人为作废/恢复投票", //
			ret = "更新影响的记录数"//
	)
	public APIResponse setVotePaused(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "人为废除/恢复") Boolean paused //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.setVotePaused(conn, voteId, paused));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "setVoteOptionIds", //
			des = "设置投票选项编号列表（包含顺序）", //
			ret = "更新影响的记录数"//
	)
	public APIResponse setVoteOptionIds(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "投票选项编号列表（JSONArray）") JSONArray optionIds //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.setVoteOptionIds(conn, voteId, optionIds));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delVote", //
			des = "删除投票项目", //
			ret = "更新影响的记录数"//
	)
	public APIResponse delVote(//
			@P(t = "投票编号") Long voteId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.delVote(conn, voteId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVotes", //
			des = "获取组织中的投票", //
			ret = "所查询的对象列表"//
	)
	public APIResponse getVotes(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "投票状态，空或不填则表示全部查询", r = false) Byte status, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getVotes(conn, orgId, status, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getUserVotes", //
			des = "获取组织用户的投票", //
			ret = "所查询的对象列表"//
	)
	public APIResponse getUserVotes(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getUserVotes(conn, orgId, userId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "addVoteOption", //
			des = "添加投票选项", //
			ret = "添加的选项"//
	)
	public APIResponse addVoteOption(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "标题") String title, //
			@P(t = "备注") String remark, //
			@P(t = "扩展（JSON）") String ext//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.addVoteOption(conn, voteId, false, title, remark, ext));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editVoteOption", //
			des = "修改投票选项", //
			ret = "影响记录的行数"//
	)
	public APIResponse editVoteOption(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "投票选项编号") Long optionId, //
			@P(t = "标题") String title, //
			@P(t = "备注") String remark, //
			@P(t = "扩展（JSON）") String ext//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			int ret = voteService.editVoteOption(conn, voteId, optionId, title, remark, ext);
			return APIResponse.getNewSuccessResp(ret);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delVoteOption", //
			des = "删除投票选项", //
			ret = "影响记录的行数")
	public APIResponse delVoteOption(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "投票选项编号") Long optionId//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.delVoteOption(conn, voteId, optionId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVoteOptions", //
			des = "获取投票的选项列表", //
			ret = "选项对象列表"//
	)
	public APIResponse getVoteOptions(//
			@P(t = "投票编号") Long voteId//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getVoteOptions(conn, voteId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "vote", //
			des = "投票"//
	)
	public APIResponse vote(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "投票编号") Long voteId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "选项JSON数组（potionId列表）\n" + //
					"[\"134441\",\"234234\"]\n") JSONArray selections, //
			@P(t = "用户的选票数") Integer ballotCount, //
			@P(t = "备注") String remark//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			voteService.vote(conn, orgId, voteId, userId, selections, ballotCount, remark);
			return APIResponse.getNewSuccessResp();
		}
	}

	@POSTAPI(//
			path = "getVoteDetail", //
			des = "获取投票详细", //
			ret = "投票详细，vote及opt信息"//
	)
	public APIResponse getVoteDetail(//
			@P(t = "投票编号") Long voteId//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getVoteDetail(conn, voteId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVoteTicket", //
			des = "获取用户的选票", //
			ret = "用户选票对象"//
	)
	public APIResponse getVoteTicket(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "用户编号") Long userId//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getVoteTicket(conn, voteId, userId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getUserBySelection", //
			des = "查看当前选项有哪些用户投", //
			ret = "用户选票对象"//
	)
	public APIResponse getUserBySelection(//
			@P(t = "投票编号") Long voteId, //
			@P(t = "选项") String selection, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(voteService.getUserBySelection(conn, voteId, selection, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "countVoteTurnout", //
			des = "统计投票", //
			ret = "投票数量以及有多少人能投"//
	)
	public APIResponse countVoteTurnout(//
			@P(t = "组织编号") JSONArray orgIds //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.countVoteTurnout(conn, orgIds));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVotesByOrgId", //
			des = "根据组织分类查询投票列表 可能为多个组织", //
			ret = "返回投票列表"//
	)
	public APIResponse getVotesByOrgId(//
			@P(t = "组织编号") JSONArray orgIds, //
			@P(t = "投票编号", r = false) Byte status, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getVotesByOrgId(conn, orgIds, status, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVoteTicketByUserId", //
			des = "查询此用户的投票列表", //
			ret = "返回投票列表"//
	)
	public APIResponse getVoteTicketByUserId(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getVoteTicketByUserId(conn, orgId, userId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getOptionByUserSelection", //
			des = "查询用户所投选项", //
			ret = "返回投票列表"//
	)
	public APIResponse getOptionByUserSelection(//
			@P(t = "用户编号") Long userId, //
			@P(t = "投票编号") Long voteId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(voteService.getOptionByUserSelection(conn, userId, voteId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getNotVoteByUserRoles", //
			des = "查询用户未投票的投票", //
			ret = "返回投票列表"//
	)
	public APIResponse getNotVoteByUserRoles(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "角色") JSONArray roles, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					voteService.getVoteByUserRoles(conn, orgId, userId, roles, count, offset, false));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVoteByUserRoles", //
			des = "查询用户已投票的投票", //
			ret = "返回投票列表"//
	)
	public APIResponse getVoteByUserRoles(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "用户编号") Long userId, //
			@P(t = "角色") JSONArray roles, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse
					.getNewSuccessResp(voteService.getVoteByUserRoles(conn, orgId, userId, roles, count, offset, true));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "sendVoteMessage", //
			des = "发送投票消息", //
			ret = ""//
	)
	public APIResponse sendVoteMessage(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "投票id") Long voteId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			voteService.sendVoteMessage(conn, orgId, voteId);
			return APIResponse.getNewSuccessResp();
		}
	}

	public int VotoISOver() throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return voteService.VotoISOver(conn);
		}
	}

	@POSTAPI(//
			path = "getNoVoteUsers", //
			des = "获取当前投票未投票的用户", //
			ret = "JSONArray"//
	)
	public JSONArray getNoVoteUsers(//
			Long orgId, //
			Long voteId//
	) throws Exception {

		try (DruidPooledConnection conn = dds.getConnection()) {
			return voteService.getNoVoteUsers(conn, orgId, voteId);
		}
	}

}
