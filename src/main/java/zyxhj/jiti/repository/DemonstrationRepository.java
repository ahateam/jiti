package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.Asset;
import zyxhj.utils.data.rds.RDSRepository;

public class DemonstrationRepository extends RDSRepository<Asset> {

	public DemonstrationRepository() {
		super(Asset.class);
	}

	public List<Asset> getAsset(DruidPooledConnection conn, String groups, Integer count, Integer offset) throws Exception{
		//SELECT * FROM tb_ecm_asset WHERE JSON_CONTAINS(groups, '397589360056633')
		JSONArray json = JSONArray.parseArray(groups);
		List<Asset> list = new ArrayList<Asset>();
		String[] ro = new String[json.size()];
		for(int  i = 0 ; i < ro.length ; i++) {
			ro[i] = json.getLong(i).toString();
			StringBuffer sb = new StringBuffer();
			sb.append("WHERE JSON_CONTAINS(groups, '").append(ro[i]).append("','$')");
			 list = this.getList(conn, sb.toString(), new Object[] {}, count, offset);
		}
		return list;
	}

	
}
