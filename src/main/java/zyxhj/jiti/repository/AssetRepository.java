package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.Asset;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

public class AssetRepository extends RDSRepository<Asset> {

	public AssetRepository() {
		super(Asset.class);
	}

	public List<Asset> queryAssets(DruidPooledConnection conn, Long orgId, String assetType, JSONArray groups,
			JSONObject tags, Integer count, Integer offset) throws ServerException {
		ArrayList<Object> objs = new ArrayList<>();

		StringBuffer sb = new StringBuffer();
		sb.append("WHERE org_id=? ");
		objs.add(orgId);
		if (null != assetType) {
			sb.append("AND asset_type=? ");
			objs.add(assetType);
		}

		if (groups != null && groups.size() > 0) {
			sb.append("AND (");
			for (int i = 0; i < groups.size(); i++) {
				Long groupId = groups.getLong(i);
				sb.append("JSON_CONTAINS(groups, '").append(groupId);
				sb.append("', '$') OR ");
			}

			sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR
			sb.append(" ) ");
		}

		if (tags != null && tags.entrySet().size() > 0) {
			boolean flg = false;
			sb.append(" AND (");

			Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
			while (it.hasNext()) {
				Entry<String, Object> entry = it.next();
				String key = entry.getKey();
				JSONArray arr = (JSONArray) entry.getValue();

				if (arr != null && arr.size() > 0) {
					for (int i = 0; i < arr.size(); i++) {
						String temp = arr.getString(i);
						sb.append("JSON_CONTAINS(tags, '").append(temp).append("', '$.").append(key).append("') OR ");
						flg = true;
					}
				}
			}

			if (!flg) {
				// 没加任何条件，补完语句
				sb.append(" TRUE ) ");
			} else {
				sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR
				sb.append(" )");
			}
		}

		String newWhere = sb.toString();
		System.out.println(newWhere);
		return this.getList(conn, newWhere, objs.toArray(), count, offset);
	}

	public List<Asset> getAssetsByGroups(DruidPooledConnection conn, Long orgId, JSONArray groups, Integer count,
			Integer offset) throws ServerException {
		return this.getListByTags(conn, "groups", "", groups, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
	}

	public List<Asset> getAssetsByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws ServerException {
		return this.getListByTags(conn, "groups", tags, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
	}

	public int batchEditAssetsGroups(DruidPooledConnection conn, Long orgId, JSONArray assetIds, JSONArray groups)
			throws ServerException {

		// SET groups="[123,456,345]"
		StringBuffer sbset = new StringBuffer();
		ArrayList<Object> pset = new ArrayList<>();

		// 不能为空，为空需要填写默认分组
		sbset.append("SET groups=?");
		if (groups == null || groups.size() <= 0) {
			// 填入未分组，避免空
			groups = new JSONArray();
			groups.add(ORGUserTagGroup.group_undefine.groupId);
		}

		pset.add(JSON.toJSONString(groups));
		String set = sbset.toString();

		// WHERE org_id=? AND id IN (1,2,3)
		StringBuffer sbwhere = new StringBuffer();
		ArrayList<Object> pwhere = new ArrayList<>();

		sbwhere.append("WHERE org_id=? AND id IN (");
		pwhere.add(orgId);

		if (assetIds != null && assetIds.size() > 0) {
			for (int i = 0; i < assetIds.size(); i++) {
				Long id = assetIds.getLong(i);
				sbwhere.append("?,");
				pwhere.add(id);
			}
			sbwhere.deleteCharAt(sbwhere.length() - 1);

			sbwhere.append(") ");

			String where = sbwhere.toString();
			System.out.println(StringUtils.join(set, " ", where));
			return this.update(conn, set, pset.toArray(), where, pwhere.toArray());
		} else {
			return 0;
		}
	}

	// public List<Asset> getAssetsByGroups(DruidPooledConnection conn, Long orgId,
	// JSONArray groups, Integer count,
	// Integer offset) throws ServerException {
	//
	// StringBuffer sb = new StringBuffer();
	// boolean flg = false;
	// sb.append("WHERE org_id=? AND (");
	// if (groups != null && groups.size() > 0) {
	//
	// for (int i = 0; i < groups.size(); i++) {
	// String group = groups.getString(i);
	// sb.append("JSON_CONTAINS(groups, '").append(group).append("', '$') OR ");
	// flg = true;
	// }
	// }
	//
	// if (!flg) {
	// // 一个查询条件都没有进入，则直接返回0
	// return new ArrayList<Asset>();
	// } else {
	// sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR
	//
	// sb.append(" )");
	//
	// String where = sb.toString();
	//
	// System.out.println(where);
	// return this.getList(conn, sb.toString(), new Object[] { orgId }, count,
	// offset);
	// }
	// }

	// public List<Asset> getAssetsByTags(DruidPooledConnection conn, Long orgId,
	// JSONObject tags, Integer count,
	// Integer offset) throws ServerException {
	//
	// StringBuffer sb = new StringBuffer();
	// boolean flg = false;
	// sb.append("WHERE org_id=? AND (");
	// if (tags != null) {
	//
	// Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
	// while (it.hasNext()) {
	// Entry<String, Object> entry = it.next();
	// String key = entry.getKey();
	// JSONArray arr = (JSONArray) entry.getValue();
	//
	// if (arr != null && arr.size() > 0) {
	// for (int i = 0; i < arr.size(); i++) {
	// String temp = arr.getString(i);
	// // JSON_CONTAINS(tags, '"tag1"', '$.groups')
	// // JSON_CONTAINS(tags, '"tag3"', '$.tags')
	// sb.append("JSON_CONTAINS(tags, '").append(temp).append("', '$') OR ");
	// flg = true;
	// }
	// }
	// }
	//
	// }
	//
	// if (!flg) {
	// // 一个查询条件都没有进入，则直接返回0
	// return new ArrayList<Asset>();
	// } else {
	// sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR
	//
	// sb.append(" )");
	//
	// String where = sb.toString();
	//
	// System.out.println(where);
	// return this.getList(conn, sb.toString(), new Object[] { orgId }, count,
	// offset);
	// }
	// }
}
