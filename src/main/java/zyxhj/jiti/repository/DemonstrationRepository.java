package zyxhj.jiti.repository;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Asset;
import zyxhj.utils.data.rds.RDSRepository;

public class DemonstrationRepository extends RDSRepository<Asset> {

	public DemonstrationRepository() {
		super(Asset.class);
	}

	public List<Asset> getAsset(DruidPooledConnection conn, Long orgId, String groups, Integer count, Integer offset)
			throws Exception {
		// SELECT * FROM tb_ecm_asset WHERE JSON_CONTAINS(groups, '397589360056633')
		// JSONArray json = JSONArray.parseArray(groups);
		// List<Asset> list = new ArrayList<Asset>();
		// String[] ro = new String[json.size()];
		// for (int i = 0; i < ro.length; i++) {

		// }
		return getList(conn, StringUtils.join("WHERE org_id = ? AND JSON_CONTAINS(groups, '", groups, "','$')"),
				Arrays.asList(orgId), count, offset);
	}

}
