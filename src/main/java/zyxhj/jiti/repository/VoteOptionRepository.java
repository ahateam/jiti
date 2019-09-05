package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.VoteOption;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteOptionRepository extends RDSRepository<VoteOption> {

	public VoteOptionRepository() {
		super(VoteOption.class);
	}

	public int countTicket(DruidPooledConnection conn, Object[] ids, Integer weight) throws ServerException {
		StringBuffer sb = new StringBuffer();
//		SQL sql = new SQL();
//		sql.addEx(SQLEx.exIn("id", ids));
//		return this.update(conn, StringUtils.join("SET ballot_count=ballot_count+1 , weight=weight+", weight), null,
//				sb.toString(), sql.getParams());

		EXP where = EXP.INS().IN("id",ids);
		List<Object> params = new ArrayList<Object>();
		where.toSQL(sb,params);
		return this.update(conn, StringUtils.join("SET ballot_count=ballot_count+1 , weight=weight+", weight), null, sb.toString(), params);
	}

	public int subTicket(DruidPooledConnection conn, Object[] ids, Integer weight) throws Exception {

//		VoteOption vo = getByKey(conn, "id", ids);
		VoteOption vo = get(conn, StringUtils.join("id = " ,ids), null);
		
		if (vo.ballotCount == 0) {
			throw new ServerException(BaseRC.ECM_VOTE_NO_BALLOTCOUNT);
		} else {
			StringBuffer sb = new StringBuffer();
//			SQL sql = new SQL();
//			sql.addEx(SQLEx.exIn("id", ids));
//			sql.fillSQL(sb);
//
//			return this.update(conn, StringUtils.join("SET ballot_count=ballot_count-1 , weight=weight-", weight), null,
//					sb.toString(), sql.getParams());
			
			EXP where = EXP.INS().IN("id",ids);
			List<Object> params = new ArrayList<Object>();
			where.toSQL(sb,params);
			return this.update(conn, StringUtils.join("SET ballot_count=ballot_count+1 , weight=weight-", weight), null, sb.toString(), params);
		}
	}

	public List<VoteOption> getOptionByVoteId(DruidPooledConnection conn, Long voteId) throws Exception {
		StringBuffer sb = new StringBuffer();
//		SQL sql = new SQL();
//		sql.addEx("vote_id = ? ", voteId);
//		sql.AND(" title <> '弃权'");
//		sql.fillSQL(sb);
//		System.out.println(sb.toString());
//		return this.getList(conn, sb.toString(), sql.getParams(), 512, 0);
		EXP sql = EXP.INS().key("vote_id", voteId).and("title <> '弃权'",null, null);
		return this.getList(conn, sql, 512, 0);
	}

}
