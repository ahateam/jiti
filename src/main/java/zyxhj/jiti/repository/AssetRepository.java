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

	public List<Asset> getAssetsBySn(DruidPooledConnection conn, Long orgId, String assetNum, Integer count,
			Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_org_user WHERE family_master LIKE '%文%' OR user_id LIKE
		// "3755%"
		StringBuffer sb = new StringBuffer();
		// 按编号模糊查询
		sb.append("WHERE org_id = ? AND sn LIKE '%").append(assetNum).append("%'");
		return this.getList(conn, sb.toString(), new Object[] { orgId }, count, offset);
	}

	public List<Asset> getAssetsByName(DruidPooledConnection conn, Long orgId, String assetNum, Integer count,
			Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_org_user WHERE family_master LIKE '%文%' OR user_id LIKE
		// "3755%"
		StringBuffer sb = new StringBuffer();
		// 按名称模糊查询
		sb.append("WHERE org_id = ? AND name LIKE '%").append(assetNum).append("%')");
		return this.getList(conn, sb.toString(), new Object[] { orgId }, count, offset);
	}

	public List<Asset> getAssetByYear(DruidPooledConnection conn, Long orgId, String buildTime, Integer count,
			Integer offset) throws Exception {
		return this.getList(conn, "WHERE org_id = ? AND build_time = ? ", new Object[] { orgId, buildTime }, count,
				offset);
	}

	// 组织统计某一年报表
	public JSONArray orgCountByYear(DruidPooledConnection conn, Long orgId, String buildTime, JSONArray orgIds,
			JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes) throws Exception {
		return this.sumAssetBYGRAB(conn, orgId, buildTime, groups, resTypes, assetTypes, businessModes);
	}

	// 组织统计多年报表
	public JSONArray orgCountByYears(DruidPooledConnection conn, Long orgId, JSONArray buildTimes, JSONArray orgIds,
			JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes) throws Exception {
		JSONArray json = new JSONArray();
		for (int i = 0; i < buildTimes.size(); i++) {
			String bu = buildTimes.getString(i);
			String s = this.sumAssetBYGRAB(conn, orgId, bu, groups, resTypes, assetTypes, businessModes).toString();
			s = s.substring(1, s.length());// 移除前[
			s = s.substring(0, s.length() - 1);// 移除后]
			json.add(JSONArray.parse(s));
		}
		return json;
	}

	/**
	 * 根据年份，资产类型等条件，统计资源原值，产值等
	 * 
	 * @param groups
	 *            分组
	 * @param resType
	 *            资源类型
	 * @param assetType
	 *            资产类型
	 * @param businessMode
	 *            经营方式
	 */
	public JSONArray sumAssetBYGRAB(DruidPooledConnection conn, Long orgId, String buildTime, JSONArray groups,
			JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes) throws Exception {

		// SELECT sum FROM table WHERE org_id=? AND build_time=? AND(...)
		// (groups) AND (resType) AND (xxx) AND (yyy)

		StringBuffer sb = new StringBuffer(
				" SELECT build_time,sum(origin_price) originPrice , sum(yearly_income) yearlyIncome FROM tb_ecm_asset WHERE org_id = ? AND build_time = ? ");

		if ((groups != null && groups.size() > 0) || (resTypes != null && resTypes.size() > 0)
				|| (assetTypes != null && assetTypes.size() > 0)
				|| (businessModes != null && businessModes.size() > 0)) {
			sb.append(" AND (");

			boolean flg = false;

			if (groups != null && groups.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < groups.size(); i++) {
					String group = groups.getString(i);
					sb.append("JSON_CONTAINS(groups, '").append(group).append("', '$')");
					if (i < groups.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				sb.append(" AND ");
			}

			if (resTypes != null && resTypes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < resTypes.size(); i++) {
					sb.append("res_type=").append("'").append(resTypes.getString(i)).append("'");
					if (i < resTypes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				sb.append(" AND ");
			}

			if (assetTypes != null && assetTypes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < assetTypes.size(); i++) {
					sb.append("asset_type=").append("'").append(assetTypes.getString(i)).append("'");
					if (i < assetTypes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				sb.append(" AND ");
			}

			if (businessModes != null && businessModes.size() > 0) {
				// 最后一个节点，不设置开关
				// flg = true;
				sb.append("(");
				for (int i = 0; i < businessModes.size(); i++) {
					sb.append("business_mode=").append("'").append(businessModes.getString(i)).append("'");
					if (i < businessModes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				// 最后一个节点，不加AND连接符号
				// sb.append(" AND ");
			} else {
				// 前面有节点，加过AND ，但最后没有节点，因此语句需要删除AND
				if (flg) {
					sb.delete(sb.length() - 4, sb.length() - 1);// 移除最后的 OR
				}
			}

			sb.append(")");
		}

		// String s = nativeGetJSONArray(conn, sb.toString(), new Object[] { orgId,
		// buildTime }).toString();
		// s = s.substring(1, s.length());// 移除前[
		// s = s.substring(0, s.length() - 1);// 移除后]
		// js.add(JSONArray.parse(s));

		return nativeGetJSONArray(conn, sb.toString(), new Object[] { orgId, buildTime });
	}

	// int xxx = 4;
	//
	// for(int i = 0;i < xxx;i++) {
	// //按年分别查询不同条件下的资产统计数据
	//
	// StringBuffer sb = new StringBuffer(
	// " SELECT build_time,sum(origin_price) originPrice , sum(yearly_income)
	// yearlyIncome FROM tb_ecm_asset WHERE org_id = ? AND build_time = ? ");
	//
	// //处理group筛选信息
	// for (int j = 0; j < groups.size(); j++) {
	// // 如果分组不为空 则加入条件进行查询
	// }
	//
	// //处理assetType资源分类筛选信息
	// for (int j = 0; j < assType.size(); j++) {
	// // 如果分组不为空 则加入条件进行查询
	// }

	// 区管理员统计某一年报表
	public JSONArray districtCountByYear(DruidPooledConnection conn, Long districtId, String buildTime,
			JSONArray orgIds, JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes)
			throws Exception {
		return this.sumAssetByDstrictId(conn, districtId, buildTime, orgIds, groups, resTypes, assetTypes,
				businessModes);
	}

	// 区管理员统计多年报表
	public JSONArray districtCountByYears(DruidPooledConnection conn, Long districtId, JSONArray buildTimes,
			JSONArray orgIds, JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes)
			throws Exception {
		JSONArray json = new JSONArray();
		for (int i = 0; i < buildTimes.size(); i++) {
			String bu = buildTimes.getString(i);
			String s = this
					.sumAssetByDstrictId(conn, districtId, bu, orgIds, groups, resTypes, assetTypes, businessModes)
					.toString();
			s = s.substring(1, s.length());// 移除前[
			s = s.substring(0, s.length() - 1);// 移除后]
			json.add(JSONArray.parse(s));
		}

		return json;
	}

	/**
	 * 区管理员统计
	 * 
	 * TODO 多年一起查询，可以尝试了。。。
	 * 
	 * @param orgIds
	 *            组织id
	 * @param groups
	 *            分组
	 * @param resType
	 *            资源类型
	 * @param assetType
	 *            资产类型
	 * @param businessMode
	 *            经营方式
	 */
	public JSONArray sumAssetByDstrictId(DruidPooledConnection conn, Long districtId, String buildTime,
			JSONArray orgIds, JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes)
			throws Exception {
		// SELECT sum FROM table WHERE build_time=? AND(...)
		// (groups) AND (resType) AND (xxx) AND (yyy)

		// JSONArray js = new JSONArray();

		StringBuffer sb = new StringBuffer(" SELECT build_time,sum(origin_price) originPrice , "
				+ "sum(yearly_income) yearlyIncome FROM tb_ecm_asset WHERE build_time = ? ");// TODO
																								// 未开发区级平台
																								// 开发完成后需修改添加区id
		if ((orgIds != null && orgIds.size() > 0) || (groups != null && groups.size() > 0)
				|| (resTypes != null && resTypes.size() > 0) || (assetTypes != null && assetTypes.size() > 0)
				|| (businessModes != null && businessModes.size() > 0)) {
			boolean flg = false;
			sb.append(" AND (");

			if (orgIds != null && orgIds.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < orgIds.size(); i++) {
					sb.append("org_id =").append(orgIds.getString(i));
					if (i < orgIds.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (groups != null && groups.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < groups.size(); i++) {
					String group = groups.getString(i);
					sb.append("JSON_CONTAINS(groups, '").append(group).append("', '$')");
					if (i < groups.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (resTypes != null && resTypes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < resTypes.size(); i++) {
					sb.append("res_type=").append("'").append(resTypes.getString(i)).append("'");
					if (i < resTypes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (assetTypes != null && assetTypes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < assetTypes.size(); i++) {
					sb.append("asset_type=").append("'").append(assetTypes.getString(i)).append("'");
					if (i < assetTypes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				sb.append(" AND ");
			}

			if (businessModes != null && businessModes.size() > 0) {
				// 最后一个节点，不设置开关
				// flg = true;
				sb.append("(");
				for (int i = 0; i < businessModes.size(); i++) {
					sb.append("business_mode=").append("'").append(businessModes.getString(i)).append("' ");
					if (i < businessModes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				// 最后一个节点，不加AND连接符号
				// sb.append(" AND ");
			} else {
				// 前面有节点，加过AND ，但最后没有节点，因此语句需要删除AND
				if (flg) {
					sb.delete(sb.length() - 4, sb.length() - 1);// 移除最后的 OR
				}
			}
			sb.append(")");
		}

		// String s = nativeGetJSONArray(conn, sb.toString(), new Object[] { buildTime
		// }).toString();
		// s = s.substring(1, s.length());// 移除前[
		// s = s.substring(0, s.length() - 1);// 移除后]
		// js.add(JSONArray.parse(s));
		System.out.println(sb.toString());

		return nativeGetJSONArray(conn, sb.toString(), new Object[] { buildTime });
	}

	// 根据类型获取资产列表
	public List<Asset> getAssetListByTypes(DruidPooledConnection conn, Long districtId, JSONArray buildTimes,
			JSONArray orgIds, JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes,
			Integer count, Integer offset) throws Exception {

		// 先判断用户传了哪些值过来 ,对值进行拼接
		// select * from xxxxx where 以后添加区id (...)
		// (build_time) And (org_id) AND (xxx)

		StringBuffer sb = new StringBuffer();

		// 如果有条件进入 则插入条件进行查询 如果没有 则返回所有列表
		if ((buildTimes != null && buildTimes.size() > 0) || (orgIds != null && orgIds.size() > 0)
				|| (groups != null && groups.size() > 0) || (resTypes != null && resTypes.size() > 0)
				|| (assetTypes != null && assetTypes.size() > 0)
				|| (businessModes != null && businessModes.size() > 0)) {

			boolean flg = false;
			sb.append(" WHERE (");

			if (buildTimes != null && buildTimes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < buildTimes.size(); i++) {
					sb.append("build_time =").append("'").append(buildTimes.getString(i)).append("'");
					if (i < buildTimes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (orgIds != null && orgIds.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < orgIds.size(); i++) {
					sb.append("org_id =").append(orgIds.getString(i));
					if (i < orgIds.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (groups != null && groups.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < groups.size(); i++) {
					String group = groups.getString(i);
					sb.append("JSON_CONTAINS(groups, '").append(group).append("', '$')");
					if (i < groups.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (resTypes != null && resTypes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < resTypes.size(); i++) {
					sb.append("res_type=").append("'").append(resTypes.getString(i)).append("'");
					if (i < resTypes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");
				sb.append(" AND ");
			}

			if (assetTypes != null && assetTypes.size() > 0) {
				flg = true;
				sb.append("(");
				for (int i = 0; i < assetTypes.size(); i++) {
					sb.append("asset_type=").append("'").append(assetTypes.getString(i)).append("'");
					if (i < assetTypes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				sb.append(" AND ");
			}

			if (businessModes != null && businessModes.size() > 0) {
				// 最后一个节点，不设置开关
				// flg = true;
				sb.append("(");
				for (int i = 0; i < businessModes.size(); i++) {
					sb.append("business_mode=").append("'").append(businessModes.getString(i)).append("' ");
					if (i < businessModes.size() - 1) {
						sb.append(" OR ");
					}
				}
				sb.append(")");

				// 最后一个节点，不加AND连接符号
				// sb.append(" AND ");
			} else {
				// 前面有节点，加过AND ，但最后没有节点，因此语句需要删除AND
				if (flg) {
					sb.delete(sb.length() - 4, sb.length() - 1);// 移除最后的 OR
				}
			}
			sb.append(")");
		}
		System.out.println(sb.toString());
		return this.getList(conn, sb.toString(), new Object[] {}, count, offset);

	}

	// TODO 现区级未完 区id以后再添加到查询内
	public List<String> getAssetType(DruidPooledConnection conn, Long districtId) throws Exception {
		// SELECT * FROM xxxx WHERE district_id = ? and org_id = ?

		return this.getColumnStrings(conn, "asset_type", " GROUP BY asset_type", new Object[] {}, 512, 0);
	}

	// TODO 现区级未完 区id以后再添加到查询内
	public List<String> getResType(DruidPooledConnection conn, Long districtId) throws Exception {
		return this.getColumnStrings(conn, "res_type", " GROUP BY res_type", new Object[] {}, 512, 0);
	}

	// TODO 现区级未完 区id以后再添加到查询内
	public List<String> getBuildTime(DruidPooledConnection conn, Long districtId) throws Exception {
		return this.getColumnStrings(conn, "build_time", " GROUP BY build_time", new Object[] {}, 512, 0);
	}

	// TODO 现区级未完 区id以后再添加到查询内
	public List<String> getBusinessMode(DruidPooledConnection conn, Long districtId) throws Exception {
		return this.getColumnStrings(conn, "business_mode", " GROUP BY business_mode", new Object[] {}, 512, 0);
	}

	// 根据区id查询类型
	public List<String> getTypeBydistrictId(DruidPooledConnection conn, Long districtId, Long orgId, String buildTime,
			String assetType, String resType, String businessMode, Integer count, Integer offset) throws Exception {
		List<String> list = new ArrayList<String>();
		StringBuffer sb = new StringBuffer(); // TODO 以后添加区id进行查询

		if (orgId != null) {
			sb.append("WHERE org_id =").append(orgId);
		}

		if (StringUtils.isNotBlank(buildTime) && "buildTime".equals(buildTime)) {
			sb.append(" GROUP BY build_time ");
			List<String> bu = this.getColumnStrings(conn, "build_time", sb.toString(), new Object[] {}, count, offset);
			for (String s : bu) {
				if (s.isEmpty()) {
				} else {
					list.add(s);
				}
			}
		}

		if (StringUtils.isNotBlank(assetType) && "assetType".equals(assetType)) {
			sb = new StringBuffer();
			sb.append(" GROUP BY asset_type ");
			List<String> bu = this.getColumnStrings(conn, "asset_type", sb.toString(), new Object[] {}, count, offset);
			for (String s : bu) {
				if (s.isEmpty()) {
				} else {
					list.add(s);
				}
			}
		}

		if (StringUtils.isNotBlank(resType) && "resType".equals(resType)) {
			sb.append(" GROUP BY res_type ");
			List<String> bu = this.getColumnStrings(conn, "res_type", sb.toString(), new Object[] {}, count, offset);
			for (String s : bu) {
				if (s.isEmpty()) {
				} else {
					list.add(s);
				}
			}
		}

		if (StringUtils.isNotBlank(businessMode) && "businessMode".equals(businessMode)) {
			sb.append(" GROUP BY business_mode");
			List<String> bu = this.getColumnStrings(conn, "business_mode", sb.toString(), new Object[] {}, count,
					offset);
			for (String s : bu) {
				if (s.isEmpty()) {
				} else {
					list.add(s);
				}
			}
		}
		System.out.println(sb.toString());
		return list;
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
