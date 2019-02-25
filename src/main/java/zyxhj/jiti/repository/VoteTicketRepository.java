package zyxhj.jiti.repository;

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
}
