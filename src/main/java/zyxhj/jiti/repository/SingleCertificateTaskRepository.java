package zyxhj.jiti.repository;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.SingleCertificateTask;
import zyxhj.utils.data.rds.RDSRepository;

public class SingleCertificateTaskRepository extends RDSRepository<SingleCertificateTask> {

	public SingleCertificateTaskRepository() {
		super(SingleCertificateTask.class);
	}

	public int getORGUserCount(DruidPooledConnection conn, String sql) throws Exception {
		Object[] s = this.sqlGetObjects(conn, sql, null);
		return Integer.parseInt(s[0].toString());
	}
}
