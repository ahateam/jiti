package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Vote;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteRepository extends RDSRepository<Vote> {

	public VoteRepository() {
		super(Vote.class);
	}

	public List<Vote> getVotes(DruidPooledConnection conn, Long orgId, Byte status, Integer count, Integer offset)
			throws ServerException {
		if (status == null) {
			return getList(conn, EXP.INS().key("org_id", orgId).append("ORDER BY create_time DESC"), count, offset);

		} else {
//			return getList(conn,
//					EXP.INS().key("org_id", orgId).andKey("status", status).append("ORDER BY create_time DESC"), count,
//					offset);

			EXP sql = EXP.INS().key("org_id", orgId).andKey("status", status);
			StringBuffer sb = new StringBuffer();
			List<Object> params = new ArrayList<Object>();
			sql.toSQL(sb, params);
			sb.append(" ORDER BY create_time DESC");
			return getList(conn, sb.toString(), params, count, offset);
			
			
		}
	}

	public List<Vote> getUserVotes(DruidPooledConnection conn, Long orgId, Long userId, Integer count, Integer offset)
			throws ServerException {
//		return getList(conn, " org_id=? AND user_id=? ORDER BY create_time DESC", Arrays.asList(orgId, userId), count,
//				offset);
		EXP sql = EXP.INS().key("org_id", orgId).andKey("user_id", userId);
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		sb.append(" ORDER BY create_time DESC");

		return getList(conn, sb.toString(), params, count, offset);
	}

	public List<Vote> getVotesByOrgId(DruidPooledConnection conn, JSONArray orgIds, Byte status, Integer count,
			Integer offset) throws Exception {
		// 区id暂时未使用
		// 遍历orgId for(orgIds)
		// 再判断status是否为空 if(status == null ) 为空则全查
		// 为空就添加进条件进行查询 else{ 将status插入到查询中 }
		// SELECT * from xxx WHERE
		EXP sql = EXP.INS();
		EXP sq = EXP.INS();
		if (orgIds != null && orgIds.size() > 0) {
			for (int i = 0; i < orgIds.size(); i++) {
//				sq.OR(StringUtils.join("org_id = ", orgIds.getString(i)));
				sq.or(EXP.INS().key("org_id", orgIds.getString(i)));
			}
			sql.and(sq); // 可能以后会有多个JSONArray
		}
		if (status != null) {
			sql.andKey("status", status);
		}
		return getList(conn, sql, count, offset);

	}

	public JSONArray getVoteTicketByUserId(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {

		// SELECT vo.* FROM tb_ecm_vote vo LEFT JOIN tb_ecm_vote_ticket tk ON vo.id =
		// tk.vote_id WHERE tk.user_id = 398070436000626 AND vo.org_id = 398067474765236
		return sqlGetJSONArray(conn,
				"SELECT vo.* FROM tb_ecm_vote vo LEFT JOIN tb_ecm_vote_ticket tk ON vo.id = tk.vote_id WHERE tk.user_id = ? AND vo.org_id = ?",
				Arrays.asList(userId, orgId), count, offset);
	}

	public List<Vote> getVoteByUserRoles(DruidPooledConnection conn, Long orgId, JSONArray roles, Integer count,
			Integer offset) throws Exception {
		// 根据orgId以及roles获取用户的可投票列表
		// SELECT * FROM tb_ecm_vote WHERE org_id = a AND (...)
		// (JSON_CONTAINS(crowd, '104','$.roles') OR JSON_CONTAINS(crowd,
		// '106','$.roles'))

		EXP exp = EXP.INS().exp("org_id", "=", orgId);
		EXP subExp = EXP.INS();
		for (int i = 0; i < roles.size(); i++) {
			subExp.or(EXP.JSON_CONTAINS("crowd", "$.roles", roles.get(i)));
		}
		exp.and(subExp).append(" ORDER BY create_time DESC");

		return getList(conn, exp, count, offset);

	}
	
	/**
	 *  修改获取投票方法
	 */
	public List<Vote> getVoteByUserRoles(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		EXP exp = EXP.INS().exp("org_id", "=", orgId);
		EXP subExp = EXP.INS();
		for (int i = 0; i < roles.size(); i++) {
			subExp.or(EXP.JSON_CONTAINS("crowd", "$.roles", roles.get(i)));
		}
		exp.and(subExp);

		return getList(conn, exp, null, null);

	}
	
	
	
	

	public JSONArray getVoteByUserRoles(DruidPooledConnection conn, Long orgId, Long userId, String roles,
			Integer count, Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_vote vo LEFT JOIN tb_ecm_vote_ticket tk ON vo.id =
		// tk.vote_id WHERE vo.org_id = 397652553337218 AND tk.user_id = 397652700169528
		// AND (JSON_CONTAINS(crowd, '104','$.roles') OR JSON_CONTAINS(crowd,
		// '106','$.roles'))

		StringBuffer sb = new StringBuffer(
				"SELECT vo.* FROM tb_ecm_vote vo LEFT JOIN tb_ecm_vote_ticket tk ON vo.id = tk.vote_id WHERE ");
		JSONArray json = JSONArray.parseArray(roles);
		EXP sql = EXP.INS().key("vo.org_id", orgId).andKey("tk.user_id", userId);

//		sql.and(EXP.JSON_CONTAINS_KEYS(json, "crowd", null));
		for (int i = 0; i < json.size(); i++) {
//			sqlEx.OR(StringUtils.join("JSON_CONTAINS(crowd, '", json.getLong(i), "','$.roles')"));
			sql.or(EXP.JSON_CONTAINS("crowd", "$.roles", json.getLong(i)));
		}
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);

		return sqlGetJSONArray(conn, sb.toString(), params, count, offset);

	}

	public int getVoteCount(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		EXP exp = EXP.INS().exp("org_id", "=", orgId);
		EXP subExp = EXP.INS();
		for (int i = 0; i < roles.size(); i++) {
//			String temp = StringUtils.join("JSON_CONTAINS(crowd, '", json.getLong(i), "','$.roles')");
//			subExp.or(temp, null);
			subExp.or(EXP.JSON_CONTAINS("crowd", "$.roles", roles.get(i)));
		}
		exp.and(subExp);
		StringBuffer sb = new StringBuffer("select count(*) from tb_ecm_vote where ");
		List<Object> params = new ArrayList<Object>();
		exp.toSQL(sb, params);
		
		Object[] count = this.sqlGetObjects(conn, sb.toString(), params);
		int size = Integer.parseInt(count[0].toString());
		
		return size;
	}

	// //统计组织下可投票人数
	// public void countNumberByOrgId(DruidPooledConnection conn, JSONArray orgIds)
	// {
	// //SELECT SUM(quorum) qu FROM tb_ecm_vote WHERE org_id = ? OR org_id = ?
	// StringBuffer sb = new StringBuffer(
	// "SELECT SUM(quorum) FROM tb_ecm_vote "
	// );
	// if(orgIds != null && orgIds.size() > 0) {
	// sb.append(" WHERE (");
	// for(int i = 0 ; i < orgIds.size() ; i++) {
	// sb.append("org_id = ").append(orgIds.getString(i));
	// if (i < orgIds.size() - 1) {
	// sb.append(" OR ");
	// }
	// }
	//
	// sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR
	// sb.append(")");
	//
	// }
	// }

}
