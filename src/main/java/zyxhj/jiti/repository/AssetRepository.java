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
import zyxhj.utils.data.rds.SQL;
import zyxhj.utils.data.rds.SQLEx;

public class AssetRepository extends RDSRepository<Asset> {

	public AssetRepository() {
		super(Asset.class);
	}

	public List<Asset> queryAssets(DruidPooledConnection conn, Long orgId, String assetType, JSONArray groups,
			JSONObject tags, Integer count, Integer offset) throws ServerException {

		SQL sql = new SQL();
		sql.addEx("org_id=? ", orgId);

		StringBuffer sb = new StringBuffer(" WHERE ");
		if (null != assetType) {
			sql.AND("asset_type= ? ", assetType);
		}

		if ((groups != null && groups.size() > 0) || (tags != null && tags.entrySet().size() > 0)) {
			if (groups != null && groups.size() > 0) {
				SQL sq = new SQL();
				for (int i = 0; i < groups.size(); i++) {
					sq.OR(StringUtils.join("JSON_CONTAINS(groups, '", groups.getLong(i), "', '$') "));
				}
				sql.AND(sq);
			}

			if (tags != null && tags.entrySet().size() > 0) {
				SQL sq = new SQL();

				Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					String key = entry.getKey();
					JSONArray arr = (JSONArray) entry.getValue();

					if (arr != null && arr.size() > 0) {
						for (int i = 0; i < arr.size(); i++) {
							sq.OR(StringUtils.join("JSON_CONTAINS(tags, '", arr.getString(i), "', '$.", key, "') "));
						}
					}
				}
				sql.AND(sq);
			}
		}
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public List<Asset> getAssetsByGroups(DruidPooledConnection conn, Long orgId, String[] groups, Integer count,
			Integer offset) throws ServerException {
		return getListByTagsJSONArray(conn, "groups","", groups, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
	}

	public List<Asset> getAssetsByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws ServerException {
		return getListByTagsJSONObject(conn, "groups", tags, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
	}

	public int batchEditAssetsGroups(DruidPooledConnection conn, Long orgId, JSONArray assetIds, JSONArray groups)
			throws ServerException {

		// SET groups="[123,456,345]"
		StringBuffer sbset = new StringBuffer(" SET ");
		ArrayList<Object> pset = new ArrayList<>();
		SQL sqlset = new SQL();

		// 不能为空，为空需要填写默认分组
		if (groups == null || groups.size() <= 0) {
			// 填入未分组，避免空
			groups = new JSONArray();
			groups.add(ORGUserTagGroup.group_undefine.groupId);
		}
		sqlset.addEx("groups=? ");
		pset.add(JSON.toJSONString(groups));
		sqlset.fillSQL(sbset);

		// WHERE org_id=? AND id IN (1,2,3)
		StringBuffer sbwhere = new StringBuffer(" WHERE ");

		SQL sqlWhere = new SQL();
		sqlWhere.addEx("org_id= ? ", orgId);

		if (assetIds != null && assetIds.size() > 0) {
			sqlWhere.AND(SQLEx.exIn("id", assetIds.toArray()));

			sqlWhere.fillSQL(sbwhere);

			System.out.println(StringUtils.join(sbset.toString(), " ", sbwhere.toString()));
			return update(conn, sbset.toString(), pset.toArray(), sbwhere.toString(), sqlWhere.getParams());
		} else {
			return 0;
		}
	}

	public List<Asset> getAssetsBySn(DruidPooledConnection conn, Long orgId, String assetNum, Integer count,
			Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_org_user WHERE family_master LIKE '%文%' OR user_id LIKE
		// "3755%"
		// 按编号模糊查询
		return this.getList(conn, StringUtils.join("WHERE org_id = ? AND sn LIKE '%", assetNum, "%'"),
				new Object[] { orgId }, count, offset);
	}

	public List<Asset> getAssetsByName(DruidPooledConnection conn, Long orgId, String assetNum, Integer count,
			Integer offset) throws Exception {
		// SELECT * FROM tb_ecm_org_user WHERE family_master LIKE '%文%' OR user_id LIKE
		// "3755%"
		// 按名称模糊查询
		return this.getList(conn, StringUtils.join("WHERE org_id = ? AND name LIKE '%", assetNum, "%' "),
				new Object[] { orgId }, count, offset);
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
	 * @param groups       分组
	 * @param resType      资源类型
	 * @param assetType    资产类型
	 * @param businessMode 经营方式
	 */
	public JSONArray sumAssetBYGRAB(DruidPooledConnection conn, Long orgId, String buildTime, JSONArray groups,
			JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes) throws Exception {

		// SELECT sum FROM table WHERE org_id=? AND build_time=? AND(...)
		// (groups) AND (resType) AND (xxx) AND (yyy)
		SQL sql = new SQL();
		StringBuffer sb = new StringBuffer(
				" SELECT build_time,sum(origin_price) originPrice , sum(yearly_income) yearlyIncome FROM tb_ecm_asset WHERE ");
		sql.addEx(" org_id = ? ", orgId);
		sql.AND("build_time = ? ", buildTime);
		if ((groups != null && groups.size() > 0) || (resTypes != null && resTypes.size() > 0)
				|| (assetTypes != null && assetTypes.size() > 0)
				|| (businessModes != null && businessModes.size() > 0)) {
			SQL subEx = new SQL();
			if (groups != null && groups.size() > 0) {
				SQL sqlGroup = new SQL();
				for (int i = 0; i < groups.size(); i++) {
					sqlGroup.OR(StringUtils.join("JSON_CONTAINS(groups, '", groups.getString(i), "', '$')"));
				}
				subEx.AND(sqlGroup);
			}

			if (resTypes != null && resTypes.size() > 0) {
				SQL sqlResType = new SQL();
				for (int i = 0; i < resTypes.size(); i++) {
					sqlResType.OR(StringUtils.join("res_type= '", resTypes.getString(i), "'"));
				}
				subEx.AND(sqlResType);
			}

			if (assetTypes != null && assetTypes.size() > 0) {
				SQL sqlAssetTypes = new SQL();
				for (int i = 0; i < assetTypes.size(); i++) {
					sqlAssetTypes.OR(StringUtils.join("asset_type= '", assetTypes.getString(i), "'"));
				}
				subEx.AND(sqlAssetTypes);
			}

			if (businessModes != null && businessModes.size() > 0) {
				SQL sqlBusinessModes = new SQL();
				for (int i = 0; i < businessModes.size(); i++) {
					sqlBusinessModes.OR(StringUtils.join("business_mode= '", businessModes.getString(i), "'"));
				}
				subEx.AND(sqlBusinessModes);
			}
			sql.AND(subEx);
		}

		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), 1, 0);
	}

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
	 * 
	 * @param orgIds       组织id
	 * @param groups       分组
	 * @param resType      资源类型
	 * @param assetType    资产类型
	 * @param businessMode 经营方式
	 */
	public JSONArray sumAssetByDstrictId(DruidPooledConnection conn, Long districtId, String buildTime,
			JSONArray orgIds, JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes)
			throws Exception {
		// SELECT sum FROM table WHERE build_time=? AND(...)
		// (groups) AND (resType) AND (xxx) AND (yyy)

		SQL sql = new SQL();

		sql.addEx("build_time = ?", buildTime);// TODO 未开发区级平台
												// 开发完成后需修改添加区id
		if ((orgIds != null && orgIds.size() > 0) || (groups != null && groups.size() > 0)
				|| (resTypes != null && resTypes.size() > 0) || (assetTypes != null && assetTypes.size() > 0)
				|| (businessModes != null && businessModes.size() > 0)) {
			SQL sqlEx = new SQL();
			if (orgIds != null && orgIds.size() > 0) {

				SQL sqlOrgIds = new SQL();
				for (int i = 0; i < orgIds.size(); i++) {
					sqlOrgIds.OR(StringUtils.join("org_id =", orgIds.getString(i)));
				}
				sqlEx.AND(sqlOrgIds);
			}

			if (groups != null && groups.size() > 0) {
				SQL sqlGroups = new SQL();
				for (int i = 0; i < groups.size(); i++) {
					sqlGroups.OR(StringUtils.join("JSON_CONTAINS(groups, '", groups.getString(i), "', '$')"));
				}
				sqlEx.AND(sqlGroups);
			}

			if (resTypes != null && resTypes.size() > 0) {
				SQL sqlresTypes = new SQL();
				for (int i = 0; i < resTypes.size(); i++) {
					sqlresTypes.OR(StringUtils.join("res_type='", resTypes.getString(i), "'"));
				}
				sqlEx.AND(sqlresTypes);
			}

			if (assetTypes != null && assetTypes.size() > 0) {
				SQL sqlassetTypes = new SQL();
				for (int i = 0; i < assetTypes.size(); i++) {
					sqlassetTypes.OR(StringUtils.join("asset_type='", assetTypes.getString(i), "'"));
				}
				sqlEx.AND(sqlassetTypes);
			}

			if (businessModes != null && businessModes.size() > 0) {
				SQL sqlbusinessModes = new SQL();
				for (int i = 0; i < businessModes.size(); i++) {
					sqlbusinessModes.OR(StringUtils.join("business_mode='", businessModes.getString(i), "' "));
				}

				sqlEx.AND(sqlbusinessModes);
			}
			sql.AND(sqlEx);
		}
		StringBuffer sb = new StringBuffer(" SELECT build_time,sum(origin_price) originPrice , "
				+ "sum(yearly_income) yearlyIncome FROM tb_ecm_asset WHERE ");
		sql.fillSQL(sb);

		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), 1, 0);
	}

	// 根据类型获取资产列表
	public List<Asset> getAssetListByTypes(DruidPooledConnection conn, Long districtId, JSONArray buildTimes,
			JSONArray orgIds, JSONArray groups, JSONArray resTypes, JSONArray assetTypes, JSONArray businessModes,
			Integer count, Integer offset) throws Exception {

		// 先判断用户传了哪些值过来 ,对值进行拼接
		// select * from xxxxx where 以后添加区id (...)
		// (build_time) And (org_id) AND (xxx)

		StringBuffer sb = new StringBuffer();
		SQL sql = new SQL();

		// 如果有条件进入 则插入条件进行查询 如果没有 则返回所有列表
		if ((buildTimes != null && buildTimes.size() > 0) || (orgIds != null && orgIds.size() > 0)
				|| (groups != null && groups.size() > 0) || (resTypes != null && resTypes.size() > 0)
				|| (assetTypes != null && assetTypes.size() > 0)
				|| (businessModes != null && businessModes.size() > 0)) {

			sb.append(" WHERE "); // TODO 此处where在添加区级管理以后 放到上面去
			SQL sqlEx = new SQL();

			if (buildTimes != null && buildTimes.size() > 0) {
				SQL sqlbuildTimes = new SQL();
				for (int i = 0; i < buildTimes.size(); i++) {
					sqlbuildTimes.OR(StringUtils.join("build_time ='", buildTimes.getString(i), "'"));
				}
				sqlEx.AND(sqlbuildTimes);
			}

			if (orgIds != null && orgIds.size() > 0) {
				SQL sqlorgIds = new SQL();
				for (int i = 0; i < orgIds.size(); i++) {
					sqlorgIds.OR(StringUtils.join("org_id =", orgIds.getString(i)));
				}
				sqlEx.AND(sqlorgIds);
			}

			if (groups != null && groups.size() > 0) {
				SQL sqlgroups = new SQL();
				for (int i = 0; i < groups.size(); i++) {
					sqlgroups.OR(StringUtils.join("JSON_CONTAINS(groups, '", groups.getString(i), "', '$')"));
				}
				sqlEx.AND(sqlgroups);
			}

			if (resTypes != null && resTypes.size() > 0) {
				SQL sqlresTypes = new SQL();
				for (int i = 0; i < resTypes.size(); i++) {
					sqlresTypes.OR(StringUtils.join("res_type ='", resTypes.getString(i), "'"));
				}
				sqlEx.AND(sqlresTypes);
			}

			if (assetTypes != null && assetTypes.size() > 0) {
				SQL sqlassetTypes = new SQL();
				for (int i = 0; i < assetTypes.size(); i++) {
					sqlassetTypes.OR(StringUtils.join("asset_type ='", assetTypes.getString(i), "'"));
				}
				sqlEx.AND(sqlassetTypes);
			}

			if (businessModes != null && businessModes.size() > 0) {
				SQL sqlbusinessModes = new SQL();
				for (int i = 0; i < businessModes.size(); i++) {
					sqlbusinessModes.OR(StringUtils.join("business_mode ='", businessModes.getString(i), "'"));
				}
				sqlEx.AND(sqlbusinessModes);
			}
			sql.AND(sqlEx);
		}
		sql.fillSQL(sb);

		// System.out.println(sb.toString());
		return getList(conn, sb.toString(), sql.getParams(), count, offset);

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
