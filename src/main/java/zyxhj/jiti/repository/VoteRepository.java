package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Vote;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class VoteRepository extends RDSRepository<Vote> {

	public VoteRepository() {
		super(Vote.class);
	}

	public List<Vote> getVotes(DruidPooledConnection conn, Long orgId, Byte status, Integer count, Integer offset)
			throws ServerException {
		if (status == null) {
			return getList(conn, "WHERE org_id=? ORDER BY create_time DESC", new Object[] { orgId }, count, offset);
		} else {
			return getList(conn, "WHERE org_id=? AND status=? ORDER BY create_time DESC",
					new Object[] { orgId, status }, count, offset);
		}
	}

	public List<Vote> getUserVotes(DruidPooledConnection conn, Long orgId, Long userId, Integer count, Integer offset)
			throws ServerException {
		return getList(conn, "WHERE org_id=? AND user_id=? ORDER BY create_time DESC", new Object[] { orgId, userId },
				count, offset);
	}

	public List<Vote> getVotesByOrgId(DruidPooledConnection conn, JSONArray orgIds, Byte status, Integer count,
			Integer offset) throws Exception {
		// 区id暂时未使用
		// 遍历orgId for(orgIds)
		// 再判断status是否为空 if(status == null ) 为空则全查
		// 为空就添加进条件进行查询 else{ 将status插入到查询中 }
		// SELECT * from xxx WHERE
		SQL sql = new SQL();
		SQL sq = new SQL();
		if (orgIds != null && orgIds.size() > 0) {
			for (int i = 0; i < orgIds.size(); i++) {
				sq.OR(StringUtils.join("org_id = ", orgIds.getString(i)));
			}
			sql.AND(sq); // 可能以后会有多个JSONArray
		}
		if (status != null) {
			sql.AND("status = ? ", status);
		}
		StringBuffer sb = new StringBuffer(" WHERE "); // TODO 以后要加上区级id
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return getList(conn, sb.toString(), sql.getParams(), count, offset);

	}

	public JSONArray getVoteTicketByUserId(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {

		// SELECT vo.* FROM tb_ecm_vote vo LEFT JOIN tb_ecm_vote_ticket tk ON vo.id =
		// tk.vote_id WHERE tk.user_id = 398070436000626 AND vo.org_id = 398067474765236
		return sqlGetJSONArray(conn,
				"SELECT vo.* FROM tb_ecm_vote vo LEFT JOIN tb_ecm_vote_ticket tk ON vo.id = tk.vote_id WHERE tk.user_id = ? AND vo.org_id = ?",
				new Object[] { userId, orgId }, count, offset);
	}

	public List<Vote> getNotVoteByUserRoles(DruidPooledConnection conn, Long orgId, String roles, Integer count,
			Integer offset) throws Exception {
		// 根据orgId以及roles获取用户的可投票列表
		// SELECT * FROM tb_ecm_vote WHERE org_id = a AND (...)
		// (JSON_CONTAINS(crowd, '104','$.roles') OR JSON_CONTAINS(crowd,
		// '106','$.roles'))

		StringBuffer sb = new StringBuffer("WHERE ");
		JSONArray json = JSONArray.parseArray(roles);
		SQL sql = new SQL();
		sql.addEx("org_id = ?", orgId);
		SQL sqlEx = new SQL();
		for (int i = 0; i < json.size(); i++) {
			sqlEx.OR(StringUtils.join("JSON_CONTAINS(crowd, '", json.getLong(i), "','$.roles')"));
		}
		sql.AND(sqlEx);
		sql.fillSQL(sb);

		return getList(conn, sb.toString(), sql.getParams(), count, offset);

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
		SQL sql = new SQL();
		sql.addEx("vo.org_id = ? ", orgId);
		sql.AND("tk.user_id = ? ", userId);

		SQL sqlEx = new SQL();
		for (int i = 0; i < json.size(); i++) {
			sqlEx.OR(StringUtils.join("JSON_CONTAINS(crowd, '", json.getLong(i), "','$.roles')"));
		}
		sql.AND(sqlEx);
		sql.fillSQL(sb);

		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), count, offset);

	}

//	//统计组织下可投票人数
//	public void countNumberByOrgId(DruidPooledConnection conn, JSONArray orgIds) {
//		//SELECT SUM(quorum) qu FROM tb_ecm_vote WHERE org_id = ? OR org_id = ?
//		StringBuffer sb = new StringBuffer(
//				"SELECT SUM(quorum) FROM tb_ecm_vote "
//				);
//		if(orgIds != null && orgIds.size() > 0) {
//			sb.append(" WHERE (");
//			for(int i = 0 ; i < orgIds.size() ; i++) {
//				sb.append("org_id = ").append(orgIds.getString(i));
//				if (i < orgIds.size() - 1) {
//					sb.append(" OR ");
//				}
//			}
//			
//			sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR
//			sb.append(")");
//			
//		}
//	}

}
