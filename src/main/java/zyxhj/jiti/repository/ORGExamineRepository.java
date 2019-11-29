package zyxhj.jiti.repository;

import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.ORGExamine;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGExamineRepository extends RDSRepository<ORGExamine> {

	public ORGExamineRepository() {
		super(ORGExamine.class);
	}

	public List<ORGExamine> getExamines(DruidPooledConnection conn) throws Exception {
		String where = "img_org is not null or img_auth is not null";
		return this.getList(conn, where, null, null, null);
	}

}
