package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Asset;
import zyxhj.utils.data.rds.RDSRepository;

public class DemonstrationRepository extends RDSRepository<Asset> {

	public DemonstrationRepository() {
		super(Asset.class);
	}

	public List<Asset> getAsset(DruidPooledConnection conn, String groups, Integer count, Integer offset)
			throws Exception {
		// SELECT * FROM tb_ecm_asset WHERE JSON_CONTAINS(groups, '397589360056633')
		JSONArray json = JSONArray.parseArray(groups);
		List<Asset> list = new ArrayList<Asset>();
		String[] ro = new String[json.size()];
		for (int i = 0; i < ro.length; i++) {
			list = this.getList(conn,
					StringUtils.join("WHERE JSON_CONTAINS(groups, '", json.getLong(i).toString(), "','$')"),
					new Object[] {}, count, offset);
		}
		return list;
	}

}
