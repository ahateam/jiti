package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.Vote;
import zyxhj.jiti.domain.VoteOption;
import zyxhj.jiti.domain.VoteTicket;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.repository.VoteOptionRepository;
import zyxhj.jiti.repository.VoteRepository;
import zyxhj.jiti.repository.VoteTicketRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;

public class VoteService {

	private static Logger log = LoggerFactory.getLogger(VoteService.class);

	private VoteRepository voteRepository;
	private VoteOptionRepository optionRepository;
	private VoteTicketRepository ticketRepository;
	private ORGUserRepository orgUserRepository;

	public VoteService() {
		try {
			voteRepository = Singleton.ins(VoteRepository.class);
			optionRepository = Singleton.ins(VoteOptionRepository.class);
			ticketRepository = Singleton.ins(VoteTicketRepository.class);
			orgUserRepository = Singleton.ins(ORGUserRepository.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 未开始返回-1</br>
	 * 正在进行返回0</br>
	 * 已经结束返回1</br>
	 */
	private int compareTime(Date startTime, Date expiryTime) {
		Date now = new Date();
		if (now.compareTo(startTime) > 0) {
			// 已经开始
			if (now.compareTo(expiryTime) > 0) {
				// 已经结束
				return 1;
			} else {
				return 0;
			}
		} else {
			return -1;
		}
	}

	public List<Vote> getVotes(DruidPooledConnection conn, Long orgId, Byte status, Integer count, Integer offset)
			throws Exception {
		return voteRepository.getVotes(conn, orgId, status, count, offset);
	}

	public List<Vote> getUserVotes(DruidPooledConnection conn, Long orgId, Long userId, Integer count, Integer offset)
			throws Exception {
		return voteRepository.getUserVotes(conn, orgId, userId, count, offset);
	}

	private void checkVoteTime(DruidPooledConnection conn, Long voteId) throws Exception {
		if (true) {
			return;
		}
		Vote vp = voteRepository.getByKey(conn, "id", voteId);
		if (vp != null) {
			int tmp = compareTime(vp.startTime, vp.expiryTime);
			if (tmp >= 0) {
				// 已开始或已经结束，不能再修改
				throw new ServerException(BaseRC.ECM_VOIT_PROJECT_STARTED);
			} else {
				return;
			}
		} else {
			throw new ServerException(BaseRC.ECM_VOIT_PROJECT_NOTEXIST);
		}
	}

	public Vote createVote(DruidPooledConnection conn, Long orgId, Byte template, Byte type, Byte choiceCount,
			JSONObject crowd, Boolean reeditable, Boolean realName, Boolean isInternal, Boolean isAbstain,
			Byte effectiveRatio, Byte failureRatio, String title, String remark, String ext, //
			Date startTime, Date expiryTime) throws Exception {

		Vote v = new Vote();
		v.id = IDUtils.getSimpleId();
		v.orgId = orgId;
		v.template = template;
		v.type = type;
		v.choiceCount = choiceCount;
		v.status = Vote.STATUS.VOTING.v();

		v.crowd = crowd.toJSONString();
		v.reeditable = reeditable;
		v.realName = realName;
		v.isInternal = isInternal;
		v.isAbstain = isAbstain;

		v.effectiveRatio = effectiveRatio;
		v.failureRatio = failureRatio;
		v.title = title;
		v.remark = remark;
		v.ext = ext;

		v.startTime = startTime;
		v.expiryTime = expiryTime;

		// 应到人数
		v.quorum = orgUserRepository.getParticipateCount(conn, orgId, v.id, crowd);

		voteRepository.insert(conn, v);

		if (isAbstain) {
			// 如果自动带有弃权选项，则默认创建一个VoteOption
			addVoteOption(conn, v.id, true, "弃权", "", "");
		}

		return v;
	}

	public int editVote(DruidPooledConnection conn, Long orgId, Long voteId, Byte type, Byte choiceCount,
			JSONObject crowd, Boolean reeditable, Boolean realName, Boolean isInternal, Boolean isAbstain,
			Byte effectiveRatio, Byte failureRatio, String title, String remark, String ext, //
			Date startTime, Date expiryTime) throws Exception {

		checkVoteTime(conn, voteId);

		Vote renew = new Vote();
		renew.type = type;
		renew.choiceCount = choiceCount;

		renew.crowd = crowd.toJSONString();
		renew.reeditable = reeditable;
		renew.realName = realName;
		renew.isInternal = isInternal;
		renew.isAbstain = isAbstain;

		renew.effectiveRatio = effectiveRatio;
		renew.failureRatio = failureRatio;
		renew.title = title;
		renew.remark = remark;
		renew.ext = ext;

		renew.startTime = startTime;
		renew.expiryTime = expiryTime;

		// 应到人数
		renew.quorum = orgUserRepository.getParticipateCount(conn, orgId, renew.id, crowd);

		int ret = voteRepository.updateByKey(conn, "id", voteId, renew, true);

		VoteOption op = optionRepository.getByANDKeys(conn, new String[] { "vote_id", "is_abstain" },
				new Object[] { voteId, true });
		if (isAbstain) {
			// 没有要创建
			if (op == null) {
				addVoteOption(conn, voteId, true, "弃权", "", "");
			}
		} else {
			// 有要删除
			if (op != null) {
				delVoteOption(conn, voteId, op.id);
			}
		}
		return ret;
	}

	public int setVoteActivation(DruidPooledConnection conn, Long voteId, Boolean activation) throws Exception {
		checkVoteTime(conn, voteId);

		Vote exist = voteRepository.getByKey(conn, "id", voteId);
		if (exist == null) {
			throw new ServerException(BaseRC.ECM_VOIT_NOTEXIST);
		} else {
			// 完成或作废状态的Vote，状态无法被更改
			if (activation) {
				// 激活
				if (exist.status == Vote.STATUS.WAITING.v()) {
					// 只有waiting状态可以激活
					Vote renew = new Vote();
					renew.status = Vote.STATUS.VOTING.v();

					return voteRepository.updateByKey(conn, "id", voteId, renew, true);
				}
			} else {
				// 禁用
				if (exist.status == Vote.STATUS.VOTING.v()) {
					// 只有投票中可以被禁用
					Vote renew = new Vote();
					renew.status = Vote.STATUS.WAITING.v();

					return voteRepository.updateByKey(conn, "id", voteId, renew, true);
				}
			}
		}

		return 0;
	}

	public int setVotePaused(DruidPooledConnection conn, Long voteId, Boolean paused) throws Exception {
		checkVoteTime(conn, voteId);

		Vote exist = voteRepository.getByKey(conn, "id", voteId);
		if (exist == null) {
			throw new ServerException(BaseRC.ECM_VOIT_NOTEXIST);
		} else {
			if (paused) {
				// 人为废除
				if (exist.status == Vote.STATUS.VOTING.v()) {
					// 只有投票中可以被人为废除
					Vote renew = new Vote();
					renew.status = Vote.STATUS.PAUSED.v();

					return voteRepository.updateByKey(conn, "id", voteId, renew, true);
				}
			} else {
				// 恢复
				if (exist.status == Vote.STATUS.PAUSED.v()) {
					// 只有人为废除中可以被恢复
					Vote renew = new Vote();
					renew.status = Vote.STATUS.VOTING.v();

					return voteRepository.updateByKey(conn, "id", voteId, renew, true);
				}
			}
		}

		return 0;
	}

	/**
	 * 重新设置选项编号列表（顺序）
	 */
	public int setVoteOptionIds(DruidPooledConnection conn, Long voteId, JSONArray optionIds) throws Exception {
		checkVoteTime(conn, voteId);
		Vote renew = new Vote();
		renew.optionIds = JSON.toJSONString(optionIds);

		return voteRepository.updateByKey(conn, "id", voteId, renew, true);
	}

	public int delVote(DruidPooledConnection conn, Long voteId) throws Exception {

		checkVoteTime(conn, voteId);

		return voteRepository.deleteByKey(conn, "id", voteId);

	}

	public VoteOption addVoteOption(DruidPooledConnection conn, Long voteId, Boolean isAbstain, String title,
			String remark, String ext) throws Exception {

		checkVoteTime(conn, voteId);

		VoteOption vo = new VoteOption();
		vo.id = IDUtils.getSimpleId();
		vo.voteId = voteId;
		vo.isAbstain = isAbstain;
		vo.title = title;
		vo.remark = remark;
		vo.ext = ext;

		// 必须赋值，否则为空的话，自增会失败
		vo.ballotCount = 0;
		vo.weight = 0;

		optionRepository.insert(conn, vo);

		Vote vote = voteRepository.getByKey(conn, "id", voteId);
		if (vote != null) {
			JSONArray ar = JSON.parseArray(vote.optionIds);
			if (ar == null) {
				ar = new JSONArray();
			}
			ar.add(vo.id);

			Vote renew = new Vote();
			renew.optionIds = JSON.toJSONString(ar);
			voteRepository.updateByKey(conn, "id", voteId, renew, true);
		}
		return vo;
	}

	public int editVoteOption(DruidPooledConnection conn, Long voteId, Long optionId, String title, String remark,
			String ext) throws Exception {

		checkVoteTime(conn, voteId);

		VoteOption renew = new VoteOption();
		renew.title = title;
		renew.remark = remark;
		renew.ext = ext;

		return optionRepository.updateByKey(conn, "id", optionId, renew, true);
	}

	public int delVoteOption(DruidPooledConnection conn, Long voteId, Long optionId) throws Exception {
		checkVoteTime(conn, voteId);

		int ret = optionRepository.deleteByKey(conn, "id", optionId);
		if (ret == 1) {
			Vote vote = voteRepository.getByKey(conn, "id", voteId);
			if (vote != null) {
				JSONArray ar = JSON.parseArray(vote.optionIds);
				if (ar == null) {
					ar = new JSONArray();
				}
				ar.remove(optionId);

				Vote renew = new Vote();
				renew.optionIds = JSON.toJSONString(ar);
				voteRepository.updateByKey(conn, "id", voteId, renew, true);
			}
		}
		return ret;
	}

	public List<VoteOption> getVoteOptions(DruidPooledConnection conn, Long voteId) throws Exception {

		Vote vote = voteRepository.getByKey(conn, "id", voteId);

		JSONArray ja = JSON.parseArray(vote.optionIds);

		if (ja != null && ja.size() > 0) {
			String[] values = new String[ja.size()];
			for (int i = 0; i < values.length; i++) {
				values[i] = ja.get(i).toString();
			}

			return optionRepository.getListByKeyInValues(conn, "id", values);
		} else {
			return new ArrayList<VoteOption>();
		}
	}

	private boolean comparePromissionArray(JSONArray ca, JSONArray ua) {
		if (ca != null && ca.size() > 0) {
			if (ua != null && ua.size() > 0) {
				for (int i = 0; i < ca.size(); i++) {
					Long cr = ca.getLong(i);
					for (int j = 0; j < ua.size(); j++) {
						if (ua.getLong(j) == cr) {
							// 包含该权限
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean hasPrmission(String strCrowd, ORGUser ou) {
		JSONObject crowd = JSON.parseObject(strCrowd);

		if (crowd.containsKey("roles") || crowd.containsKey("tags") || crowd.containsKey("groups")) {
			// 包含权限要求，需要进一步判断

			if (crowd.containsKey("roles")) {
				// 如果有角色权限要求，则判定角色
				JSONArray cRoles = crowd.getJSONArray("roles");
				JSONArray uRoles = JSON.parseArray(ou.roles);

				if (comparePromissionArray(cRoles, uRoles)) {
					return true;
				}

				// 没有匹配的权限，什么都不做跳过进入后续的权限判断
			}

			if (crowd.containsKey("groups")) {
				// 如果有角色权限要求，则判定角色
				JSONArray cGroups = crowd.getJSONArray("groups");
				JSONArray uGroups = JSON.parseArray(ou.groups);

				if (comparePromissionArray(cGroups, uGroups)) {
					return true;
				}
				// 没有匹配的权限，什么都不做跳过进入后续的权限判断
			}

			if (crowd.containsKey("tags")) {
				// 如果有标签权限要求，则判定标签
				JSONObject cTags = crowd.getJSONObject("tags");
				JSONObject uTags = JSON.parseObject(ou.tags);

				if (cTags != null) {
					Iterator<Entry<String, Object>> it = cTags.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, Object> entry = it.next();
						String key = entry.getKey();
//						JSONArray arr = (JSONArray) entry.getValue();s

						// 逐个判断tags中的权限述求(groups,tags,以及其它标签分组)

						if (uTags.containsKey(key)) {

							JSONArray ca = cTags.getJSONArray(key);
							JSONArray ua = uTags.getJSONArray(key);

							if (comparePromissionArray(ca, ua)) {
								return true;
							}

							// 没有匹配的权限，什么都不做跳过进入后续的权限判断
						} else {
							// 该分组不存在，跳过
							continue;
						}
					}
				}

			}

			// 没有找到任何匹配的权限，返回失败
			return false;
		} else {
			// 没有任何要求，直接判定全体可以投票
			return true;
		}

	}

	/**
	 * 投票缓存，缓存2分钟
	 */
	public static Cache<Long, Vote> VOTE_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(30, TimeUnit.SECONDS)// 缓存对象有效时间，2天
			.maximumSize(1000)//
			.build();

	/**
	 * 投票，截止时间前，都可以投票
	 */
	public void vote(DruidPooledConnection conn, Long orgId, Long voteId, Long userId, JSONArray selections,
			Integer ballotCount, String remark) throws Exception {

		// 判断project是否开启，可以投票
		// VoteProject vp = projectRepository.getByKey(conn, "id", projectId);
		// if (vp != null) {
		// // int tmp = compareTime(vp.startTime, vp.expiryTime);
		// // if (tmp == 0) {
		// // // 已开始，未结束，正好
		// // // continue
		// // } else if (tmp < 0) {
		// // throw new ServerException(BaseRC.ECM_VOIT_PROJECT_NOTSTARTED);
		// // } else {
		// // throw new ServerException(BaseRC.ECM_VOIT_PROJECT_FINISHED);
		// // }
		// } else {
		// throw new ServerException(BaseRC.ECM_VOIT_PROJECT_NOTEXIST);
		// }

		Vote vote = VOTE_CACHE.getIfPresent(voteId);
		if (vote == null) {
			// 缓存中没有，从数据库中获取
			vote = voteRepository.getByKey(conn, "id", voteId);
			if (vote == null) {
				throw new ServerException(BaseRC.ECM_VOIT_NOTEXIST);
			} else {
				// 放入缓存
				VOTE_CACHE.put(voteId, vote);
			}
		}

		// 非投票中状态，抛出异常
		if (vote.status != Vote.STATUS.VOTING.v()) {
			throw new ServerException(BaseRC.ECM_VOIT_STATUS_ERROR);
		}

		// 判断用户是否有权限投票
		ORGUser ou = orgUserRepository.getByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId });
		if (ou == null) {
			throw new ServerException(BaseRC.ECM_VOTE_ORGROLE_ERROR);
		}

		if (hasPrmission(vote.crowd, ou)) {
			// 开始投票
			VoteTicket exist = ticketRepository.getByANDKeys(conn, new String[] { "vote_id", "user_id" },
					new Object[] { voteId, userId });

			boolean firstTime = (exist == null ? true : false);

			// 处理选票
			if (firstTime) {
				// 该用户没有投过票，第一次
				if (selections.size() <= 0) {
					return;
				}
				// 添加选票
				addVoteTicket(conn, orgId, voteId, userId, selections, ballotCount, remark);

			} else {
				// 不再创建，更新
				// 该用户没有选择投票选项
				if (selections.size() <= 0) {
					return;
				}

//				VoteTicket gvt = getVoteTicket(conn, voteId, userId);
//				JSONArray json = JSONArray.parseArray(gvt.selection);

				// 删除当前用户的投票
				delVoteTicket(conn, voteId, userId);

				// 计票-1
				List<String> ids = new ArrayList<>(selections.size());
				for (int i = 0; i < ids.size(); i++) {
					ids.set(i, selections.getLong(i).toString());
				}
				optionRepository.subTicket(conn, ids, ballotCount);

				addVoteTicket(conn, orgId, voteId, userId, selections, ballotCount, remark);

			}
		} else {
			throw new ServerException(BaseRC.ECM_VOTE_NO_PROMISS);
		}
	}

	public void delVoteTicket(DruidPooledConnection conn, Long voteId, Long userId) throws Exception {
		voteRepository.deleteByANDKeys(conn, new String[] { "vote_id", "user_id" }, new Object[] { voteId, userId });
	}

	/**
	 * 获取投票详细
	 */
	public JSONObject getVoteDetail(DruidPooledConnection conn, Long voteId) throws Exception {
		Vote vote = voteRepository.getByKey(conn, "id", voteId);

		List<VoteOption> options = optionRepository.getListByKey(conn, "vote_id", voteId, 512, 0);

		int ticketCount = ticketRepository.countTicket(conn, voteId);

		vote.quorum = orgUserRepository.getParticipateCount(conn, vote.orgId, vote.id, JSON.parseObject(vote.crowd));
		JSONObject ret = new JSONObject();
		ret.put("vote", vote);
		ret.put("ops", options);
		ret.put("ticketCount", ticketCount);

		return ret;
	}

	/**
	 * 获取用户的选票
	 */
	public VoteTicket getVoteTicket(DruidPooledConnection conn, Long voteId, Long userId) throws Exception {
		return ticketRepository.getByANDKeys(conn, new String[] { "vote_id", "user_id" },
				new Object[] { voteId, userId });
	}

	/**
	 * 添加选票
	 */
	public void addVoteTicket(DruidPooledConnection conn, Long orgId, Long voteId, Long userId, JSONArray selections,
			Integer ballotCount, String remark) throws Exception {

		// 添加票
		VoteTicket vt = new VoteTicket();
		vt.voteId = voteId;
		vt.orgId = orgId;
		vt.userId = userId;
		vt.voteTime = new Date();
		vt.ballotCount = ballotCount;
		vt.selection = JSON.toJSONString(selections);
		vt.remark = remark;
		// 创建选票
		ticketRepository.insert(conn, vt);

		// 计票
		List<String> ids = new ArrayList<>(selections.size());
		for (int i = 0; i < ids.size(); i++) {
			ids.set(i, selections.getLong(i).toString());
		}
		optionRepository.countTicket(conn, ids, ballotCount);
	}

	public List<VoteTicket> getUserBySelection(DruidPooledConnection conn, Long voteId, String selection, Integer count,
			Integer offset) throws Exception {
		return ticketRepository.getUserBySelection(conn, voteId, selection, count, offset);
	}

	// 区级统计投票的积极性
	public Double countVoteTurnout(DruidPooledConnection conn, Long districtId, JSONArray orgIds) throws Exception {
		// voteRepository.countNumberByOrgId( conn, orgId);
		// for(遍历org)
		// 查询当前org下的投票
		// 将可投票的人数进行相加
//		Map<String, Integer> ma = new HashMap<String, Integer>();
//		Integer a = 0, b = 0;
//		for (int i = 0; i < orgIds.size(); i++) {
//			
//			
//			List<Vote> getVote = voteRepository.getListByKey(conn, "org_id", orgIds.getString(i), null, null);
//			for (Vote v : getVote) {
//				ma.put("countQuorum", a = a + v.quorum);
//			}
//			
//			int co = ticketRepository.countByKey(conn, "org_id", orgIds.getString(i));
//			ma.put("countTicket", b = b + co);
//			
//		}
//
//		System.out.println(ma);
		List<Double> list = new ArrayList<Double>();
		Double d = 0.0; // 拿来放总票率
		// 遍历orgid 获取orgid下的投票
		for (int i = 0; i < orgIds.size(); i++) {
			// 根据orgId取得当前org下得VOTE
			List<Vote> getVote = voteRepository.getListByKey(conn, "org_id", orgIds.getString(i), null, null);
			// 遍历VOTE 得到一个投票率
			for (Vote v : getVote) {
				if (v.quorum != 0) {
					double a = v.quorum;
					int c = ticketRepository.countTicket(conn, v.id);
					if (c != 0) {
						// 把相票率放入list中
						list.add(c / a);
					} else {
						list.add(0.5);
					}
				} else {
					list.add(0.5);
				}
			}
		}
		for (int i = 0; i < list.size(); i++) {
			d = d + list.get(i);
		}
		// 输出 总票率/投票个数
		return d / list.size();
	}

	// 根据组织分类查询投票列表 可能为多个组织
	public List<Vote> getVotesByOrgId(DruidPooledConnection conn, Long districtId, JSONArray orgIds, Byte status,
			Integer count, Integer offset) throws Exception {
		return voteRepository.getVotesByOrgId(conn, districtId, orgIds, status, count, offset);
	}

	// 查询用户投票列表
	// TODO 可以用一个查询做
	public List<Vote> getVoteTicketByUserId(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {
		// 查询用户投票记录
		List<VoteTicket> li = ticketRepository.getListByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { orgId, userId }, count, offset);

		// 获取到投票记录的voidId 在Vote表里进行匹配
		if (li != null && li.size() > 0) {
			String[] s = new String[li.size()];
			for (int i = 0; i < li.size(); i++) {
				s[i] = li.get(i).voteId.toString();
			}
			return voteRepository.getListByKeyInValues(conn, "id", s);
		} else {
			return new ArrayList<Vote>();
		}
	}

	// 查询用户所投选项
	public List<VoteOption> getOptionByUserSelection(DruidPooledConnection conn, Long userId, Long voteId)
			throws Exception {

		VoteTicket getTicket = ticketRepository.getByANDKeys(conn, new String[] { "vote_id", "user_id" },
				new Object[] { voteId, userId });
		JSONArray js = JSON.parseArray(getTicket.selection);

		if (js != null && js.size() > 0) {
			String[] s = new String[js.size()];
			for (int i = 0; i < js.size(); i++) {
				s[i] = js.get(i).toString();
			}
			return optionRepository.getListByKeyInValues(conn, "id", s);
		} else {
			return new ArrayList<VoteOption>();
		}
	}

}
