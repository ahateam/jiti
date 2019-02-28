package zyxhj.jiti.repository;

import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Vote;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteRepository extends RDSRepository<Vote> {

	public VoteRepository() {
		super(Vote.class);
	}

	public List<Vote> getVotes(DruidPooledConnection conn, Long orgId, Byte status, Integer count, Integer offset)
			throws ServerException {
		StringBuffer sb = new StringBuffer();
		if (status == null) {
			sb.append("WHERE org_id=? ORDER BY create_time DESC");
			return getList(conn, sb.toString(), new Object[] { orgId }, count, offset);
		} else {
			sb.append("WHERE org_id=? AND status=? ORDER BY create_time DESC");
			return getList(conn, sb.toString(), new Object[] { orgId, status }, count, offset);
		}
	}

	public List<Vote> getUserVotes(DruidPooledConnection conn, Long orgId, Long userId, Integer count, Integer offset)
			throws ServerException {
		StringBuffer sb = new StringBuffer();
		sb.append("WHERE org_id=? AND user_id=? ORDER BY create_time DESC");
		return getList(conn, sb.toString(), new Object[] { orgId, userId }, count, offset);
	}
}
