package zyxhj.jiti.repository;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.VoteTicket;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteTicketRepository extends RDSRepository<VoteTicket> {

	public VoteTicketRepository() {
		super(VoteTicket.class);
	}

	public List<VoteTicket> getUserBySelection(DruidPooledConnection conn, Long voteId, String selection, Integer count,
			Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_vote_ticket WHERE vote_id = 397557883853724 AND
		// JSON_CONTAINS(selection, '397557885981598','$')

		return this.getList(conn,
				StringUtils.join(" vote_id = ? AND JSON_CONTAINS(selection,'", selection, "','$')"),
				Arrays.asList( voteId), count, offset);
	}

	public int countTicket(DruidPooledConnection conn, Long id) throws Exception {
		Object[] s = sqlGetObjects(conn, "SELECT count(*) FROM tb_ecm_vote_ticket WHERE vote_id = ? ",
				Arrays.asList( id));
		return Integer.parseInt(s[0].toString());
	}

}
