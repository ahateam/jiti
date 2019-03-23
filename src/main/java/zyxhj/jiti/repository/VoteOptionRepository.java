package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.VoteOption;
import zyxhj.jiti.domain.VoteTicket;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteOptionRepository extends RDSRepository<VoteOption> {

	public VoteOptionRepository() {
		super(VoteOption.class);
	}

	public int countTicket(DruidPooledConnection conn, String[] ids, Integer weight) throws ServerException {
		return updateKeyInValues(conn, "id", ids,
				StringUtils.join("SET ballot_count=ballot_count+1 , weight=weight+", weight), ids);
	}
	
	public int subTicket(DruidPooledConnection conn, String[] ids, Integer weight) throws Exception{
		VoteOption vo = getByKey(conn, "id", ids);
		if(vo.ballotCount == 0) {
			 throw new ServerException(BaseRC.ECM_VOTE_NO_BALLOTCOUNT);
		}
		else {
			return updateKeyInValues(conn, "id", ids,
				StringUtils.join("SET ballot_count=ballot_count-1 , weight=weight-", weight), ids);
		}
	}


}
