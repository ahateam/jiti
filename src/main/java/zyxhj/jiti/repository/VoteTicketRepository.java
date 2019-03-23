package zyxhj.jiti.repository;

import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.VoteTicket;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteTicketRepository extends RDSRepository<VoteTicket> {

	public VoteTicketRepository() {
		super(VoteTicket.class);
	}

	public int getTicketCount(DruidPooledConnection conn, Long voteId) throws ServerException {
		return countByKey(conn, "vote_id", voteId);
	}

	public List<VoteTicket> getUserBySel(DruidPooledConnection conn, Long voteId, String selection, Integer count,
			Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_vote_ticket WHERE vote_id = 397557883853724 AND JSON_CONTAINS(selection, '397557885981598','$')
		StringBuffer sb = new StringBuffer(" WHERE vote_id = ? AND JSON_CONTAINS(selection,'").append(selection).append("','$')");
		
		return this.getList(conn, sb.toString(), new Object[]{voteId}, count, offset);
	}

}
