package zyxhj.jiti.repository;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.AssetImportRecord;
import zyxhj.utils.data.rds.RDSRepository;

public class AssetImportRecordRepository extends RDSRepository<AssetImportRecord> {

	public AssetImportRecordRepository() {
		super(AssetImportRecord.class);
	}

	public void updateStatus(DruidPooledConnection conn, Long id, Byte status) throws Exception {
		this.update(conn, StringUtils.join("SET status = ", status), null, "WHERE id = ? ", new Object[] { id });
	}

}
