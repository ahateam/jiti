package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.VoteOption;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;
import zyxhj.utils.data.rds.SQLEx;

public class VoteOptionRepository extends RDSRepository<VoteOption> {

	public VoteOptionRepository() {
		super(VoteOption.class);
	}

	public int countTicket(DruidPooledConnection conn, Object[] ids, Integer weight) throws ServerException {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx(SQLEx.exIn("id", ids));
		sql.fillSQL(sb);

		return this.update(conn, StringUtils.join("SET ballot_count=ballot_count+1 , weight=weight+", weight), null,
				sb.toString(), sql.getParams());
	}

	public int subTicket(DruidPooledConnection conn, Object[] ids, Integer weight) throws Exception {

		VoteOption vo = getByKey(conn, "id", ids);
		if (vo.ballotCount == 0) {
			throw new ServerException(BaseRC.ECM_VOTE_NO_BALLOTCOUNT);
		} else {
			StringBuffer sb = new StringBuffer("WHERE ");
			SQL sql = new SQL();
			sql.addEx(SQLEx.exIn("id", ids));
			sql.fillSQL(sb);

			return this.update(conn, StringUtils.join("SET ballot_count=ballot_count-1 , weight=weight-", weight), null,
					sb.toString(), sql.getParams());
		}
	}

	public List<VoteOption> getOptionByVoteId(DruidPooledConnection conn, Long voteId) throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		sql.addEx("vote_id = ? ", voteId);
		sql.AND(" title <> '弃权'");
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return this.getList(conn, sb.toString(), sql.getParams(), 512, 0);
	}

}
