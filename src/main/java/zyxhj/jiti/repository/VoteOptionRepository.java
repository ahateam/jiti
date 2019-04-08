package zyxhj.jiti.repository;

import java.util.ArrayList;
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

	public int countTicket(DruidPooledConnection conn, List<String> ids, Integer weight) throws ServerException {

		SQL sql = new SQL();

		sql.addEx(SQLEx.exIn("id", ids.toArray()));


		return this.update(conn, StringUtils.join("SET ballot_count=ballot_count+1 , weight=weight+", weight), null,
				sql.getSQL(), sql.getParams());
	}

	public int subTicket(DruidPooledConnection conn, List<String> ids, Integer weight) throws Exception {
		VoteOption vo = getByKey(conn, "id", ids);
		if (vo.ballotCount == 0) {
			throw new ServerException(BaseRC.ECM_VOTE_NO_BALLOTCOUNT);
		} else {

//			Object[] objs = new Object[ids.length];
//			for (int i = 0; i < ids.length; i++) {
//				objs[i] = ids[i];
//			}
			SQL sql = new SQL();

			sql.addEx(SQLEx.exIn("id", ids.toArray()));

//			StringBuffer sb = new StringBuffer();
//			sql.fillSQL(sb);

//			ArrayList<Object> args = new ArrayList<>();
//			sql.fillParams(args);

			return this.update(conn, StringUtils.join("SET ballot_count=ballot_count-1 , weight=weight-", weight), null,
					sql.getSQL(), sql.getParams());
		}
	}

}
