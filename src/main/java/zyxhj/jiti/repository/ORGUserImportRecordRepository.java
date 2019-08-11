package zyxhj.jiti.repository;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.ORGUserImportRecord;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserImportRecordRepository extends RDSRepository<ORGUserImportRecord> {

	public ORGUserImportRecordRepository() {
		super(ORGUserImportRecord.class);
	}

	public void updateStatus(DruidPooledConnection conn, Long id, Byte status) throws Exception {
		this.update(conn, StringUtils.join("SET status = ", status), null, "id = ? ", Arrays.asList(id));
	}
}
