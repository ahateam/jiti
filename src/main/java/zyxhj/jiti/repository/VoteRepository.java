package zyxhj.jiti.repository;

import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Vote;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

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

	public List<Vote> getVotesByOrgId(DruidPooledConnection conn, Long districtId, JSONArray orgIds, Byte status,
			Integer count, Integer offset) throws Exception {
		// 区id暂时未使用
		// 遍历orgId for(orgIds)
		// 再判断status是否为空 if(status == null ) 为空则全查
		// 为空就添加进条件进行查询 else{ 将status插入到查询中 }
		// SELECT * from xxx WHERE
		StringBuffer sb = new StringBuffer(); // TODO 以后要加上区级id
		if (orgIds != null && orgIds.size() > 0) {
			sb.append("WHERE (");
			for (int i = 0; i < orgIds.size(); i++) {
				sb.append("org_id = ").append(orgIds.getString(i));
				if (i < orgIds.size() - 1) {
					sb.append(" OR ");
				}
			}
			sb.append(")");
		}
		if (status != null) {
			sb.append(" AND status = ").append(status);
		}

		return this.getList(conn, sb.toString(), new Object[] {}, count, offset);
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
