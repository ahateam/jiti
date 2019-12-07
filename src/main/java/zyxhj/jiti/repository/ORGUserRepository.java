package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserRepository extends RDSRepository<ORGUser> {

	public ORGUserRepository() {
		super(ORGUser.class);
	}

	/**
	 * 检查ORGUser权限
	 * 
	 * @param roles 需要具备的权限数组
	 */
	public ORGUser checkORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, ORGUserRole[] roles)
			throws ServerException {
//		ORGUser orgUser = getByANDKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId });

		ORGUser orgUser = get(conn, StringUtils.join("org_id = ", orgId, " AND user_id = ", userId), null);
		ServiceUtils.checkNull(orgUser);
		// check role，字符串搜索的做法不严谨，但是将就了
		if (roles == null || roles.length == 0) {
			return orgUser;
		} else {
			JSONArray arr = JSON.parseArray(orgUser.roles);
			Long id;
			for (ORGUserRole r : roles) {
				for (int i = 0; i < arr.size(); i++) {
					id = arr.getLong(i);
					if (id.equals(r.roleId)) {
						// 遇到匹配的角色
						return orgUser;
					}
				}
			}
			// 没有遇到匹配的角色
			throw new ServerException(BaseRC.USER_AUTH_NOT_ADMIN);
		}
	}

	public int getParticipateCount(DruidPooledConnection conn, Long orgId, Long voteId, JSONObject crowd)
			throws ServerException {
		// 通过ROGUser的roles，groups和tags来判定人数
		// 人群是重叠的，所以查询比较难写

		// WHERE org_id=? AND (JSON_CONTAINS(roles, '101', '$') OR JSON_CONTAINS(roles,
		// '102', '$') OR JSON_CONTAINS(roles, '103', '$') OR JSON_CONTAINS(roles,
		// '104', '$') OR JSON_CONTAINS(roles, '105', '$') )

		JSONArray roles = crowd.getJSONArray("roles");
		JSONArray groups = crowd.getJSONArray("groups");
		JSONObject tags = crowd.getJSONObject("tags");

		EXP sql = EXP.INS().key("org_id", orgId);
		if ((roles != null && roles.size() > 0) || (groups != null && groups.size() > 0) || (tags != null)) {
			EXP sqlEx = EXP.INS();
			if (roles != null && roles.size() > 0) {
				for (int i = 0; i < roles.size(); i++) {
					sqlEx.or(EXP.JSON_CONTAINS("roles", "$", roles.get(i)));
				}
			}

			if (groups != null && groups.size() > 0) {
				for (int i = 0; i < groups.size(); i++) {
					sqlEx.or(EXP.JSON_CONTAINS("group", "$", groups.get(i)));
				}
			}

			if (tags != null && tags.size() > 0) {
				Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
				Entry<String, Object> entry;
				JSONArray arr;
				while (it.hasNext()) {
					entry = it.next();
					String key = entry.getKey();
					arr = (JSONArray) entry.getValue();
					if (arr != null && arr.size() > 0) {
						for (int i = 0; i < arr.size(); i++) {
							sqlEx.or(EXP.JSON_CONTAINS("tags", "$." + key, arr.getString(i)));
						}
					}
				}
			}
			sql.and(sqlEx);
		}

		StringBuffer sb = new StringBuffer("SELECT COUNT(*) FROM tb_ecm_org_user WHERE ");
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		Object[] getObj = sqlGetObjects(conn, sb.toString(), params);
		if (getObj[0].toString() != null) {
			return Integer.parseInt(getObj[0].toString());
		} else {
			return 0;
		}
	}

	public int setORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, JSONArray roles)
			throws ServerException {
		if (roles == null || roles.size() <= 0) {
			return 0;
		} else {
			ORGUser renew = new ORGUser();
			renew.roles = JSON.toJSONString(roles);
			return update(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId), renew, true);
		}
	}

	public List<ORGUser> getORGUsersByRoles(DruidPooledConnection conn, Long orgId, Long[] roles, Integer count,
			Integer offset) throws ServerException {
//		String[] a = new String[roles.size()];
//		for(int i = 0 ; i < roles.size() ; i++) {
//			a[i] = roles.getString(i);
//		}
//		return getListByTagsJSONArray(conn, "roles", "", roles, " org_id=? ", Arrays.asList(orgId), count, offset);
		JSONArray ja = new JSONArray();
		for (int i = 0; i < roles.length; i++) {
			ja.add(roles[i]);
		}
		if (ja != null && ja.size() > 0) {
			EXP exp = EXP.INS().key("org_id", orgId).and(EXP.JSON_CONTAINS_KEYS(ja, "roles", null));
			return getList(conn, exp, count, offset);
		} else {
			return new ArrayList<ORGUser>();
		}

	}

	public List<ORGUser> getORGUsersByGroups(DruidPooledConnection conn, Long orgId, String[] groups, Integer count,
			Integer offset) throws ServerException {

		Long[] group = new Long[groups.length];
		Long l;
		long length = groups.length;
		for (int i = 0; i < length; i++) {
			l = new Long(groups[i]);
			group[i] = l;
		}
		JSONArray ja = (JSONArray) JSONArray.toJSON(group);

		EXP exp = EXP.INS().key("org_id", orgId).and(JsonContainsORKey(ja, "groups", null));

		return getList(conn, exp, count, offset);
	}

	public List<ORGUser> getORGUsersByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws ServerException {
		EXP exp = EXP.INS().key("org_id", orgId).and(EXP.JSON_CONTAINS_JSONOBJECT(tags, "tags"));
		return getList(conn, exp, count, offset);
	}

	public List<User> getORGUsersLikeIDNumber(DruidPooledConnection conn, Long orgId, String idNumber, Integer count,
			Integer offset) throws ServerException {
		try {
			if (StringUtils.isNoneBlank(idNumber) && idNumber.length() > 1) {

				return sqlGetOtherList(conn, Singleton.ins(UserRepository.class), StringUtils.join(
						"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`id_number` LIKE '%",
						idNumber, "%' LIMIT ? OFFSET ?"), Arrays.asList(orgId, count, offset));

			} else {
				return new ArrayList<>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}

	}

	public List<User> getORGUsersLikeRealName(DruidPooledConnection conn, Long orgId, String realName, Integer count,
			Integer offset) throws ServerException {
		if (StringUtils.isNoneBlank(realName)) {

			// return this.getList(conn, StringUtils.join("WHERE org_id=? AND id_number LIKE
			// '%", idNumber, "%'"),
			// new Object[] { orgId }, count, offset);

			try {
				// return this.nativeGetJSONArray(conn, sql.toString(), new Object[] { orgId,
				// count, offset });
				return sqlGetOtherList(conn, Singleton.ins(UserRepository.class), StringUtils.join(
						"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`real_name` LIKE '%",
						realName, "%' LIMIT ? OFFSET ?"), Arrays.asList(orgId, count, offset));
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		} else {
			return new ArrayList<>();
		}

	}

	public void batchEditORGUsersGroups(DruidPooledConnection conn, Long orgId, JSONArray userIds, Long groups)
			throws ServerException {

		// SELECT * FROM tb_ecm_org_user WHERE org_id = 397652553337218 and user_id =
		// 397652692024985
		// AND JSON_CONTAINS(groups,'397652645549447','$')
		int count = 0;
		EXP ta;
		ORGUser getORGUser;
		EXP where;
		EXP set;
		int length = userIds.size();
		for (int i = 0; i < length; i++) {
			// 查询用户
			ta = EXP.INS().key("org_id", orgId).andKey("user_id", userIds.getLong(i));
			getORGUser = get(conn, ta);
			// 等于-1表示不存在
			if (getORGUser != null) {
				// 不存在分组 给组织用户添加分组
				where = EXP.INS().andKey("org_id", orgId).andKey("user_id", userIds.getLong(i));
				set = EXP.JSON_ARRAY_APPEND("groups", groups, true);
				update(conn, set, where);
			}
			count++;
		}
	}

	public Map<String, Integer> countRole(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		Map<String, Integer> map = new HashMap<String, Integer>();

		// SELECT COUNT(*) FROM tb_ecm_org_user WHERE JSON_CONTAINS(roles, '101','$')

		for (int i = 0; i < roles.size(); i++) {
			// 获取roles值
			String ro = roles.getString(i);
			Object[] s = sqlGetObjects(conn,
					StringUtils.join("SELECT COUNT(*) FROM tb_ecm_org_user WHERE JSON_CONTAINS(roles, '", ro, "','$')"),
					null);
			map.put(ro, Integer.parseInt(s[0].toString()));
		}
		return map;
	}

	public List<ORGUser> getORGUsersInfoByUsers(DruidPooledConnection conn, Long orgId, Object[] values)
			throws Exception {
		StringBuffer sb = new StringBuffer();
//		SQL sql = new SQL();
//		sql.addEx("org_id = ?", orgId);
//		sql.AND(SQLEx.exIn("user_id", values));
//		sql.fillSQL(sb);
		return getList(conn, EXP.INS().key("org_id", orgId).and(EXP.IN("user_id", values)), null, null);
	}

	public JSONArray getORGAdmin(DruidPooledConnection conn, Long orgId, Byte level, Integer count, Integer offset)
			throws Exception {
//		SQL sql = new SQL();
		EXP sql = EXP.INS().key("org_id", orgId);
		// SELECT u.* FROM tb_ecm_org_user oru LEFT JOIN tb_user u ON oru.user_id = u.id
		// WHERE org_id = 397652553337218 AND JSON_CONTAINS(oru.roles, "102",'$')
		StringBuffer sb = new StringBuffer(
				"SELECT u.* FROM tb_ecm_org_user oru LEFT JOIN tb_user u ON oru.user_id = u.id WHERE ");
//		sql.addEx("org_id = ?", orgId);
		EXP sqlAnd = EXP.INS();
		JSONArray ja = new JSONArray();
		if (level == ORG.LEVEL.COOPERATIVE.v()) {
//			sqlAnd.AND(StringUtils.join("JSON_CONTAINS(oru.roles, \"102\",'$')"));
			ja.add(102);
			sqlAnd.and(EXP.JSON_CONTAINS_KEYS(ja, "oru.roles", null));
		}
		if (level != ORG.LEVEL.COOPERATIVE.v()) {
//			sqlAnd.OR(StringUtils.join("JSON_CONTAINS(oru.roles, \"112\",'$')"));
			ja.add(112);
			sqlAnd.or(EXP.JSON_CONTAINS_KEYS(ja, "oru.roles", null));
		}
		sql.and(sqlAnd);
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		return sqlGetJSONArray(conn, sb.toString(), params, count, offset);
	}

	public JSONArray getFamilyUserBYFamilyId(DruidPooledConnection conn, Long orgId, Long familyNumber)
			throws Exception {
		// SELECT * FROM tb_ecm_org_user oru LEFT JOIN tb_user user ON oru.user_id =
		// user.id WHERE
		// oru.org_id = 398977803603065 AND oru.family_number = 1475
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM tb_user user  RIGHT JOIN tb_ecm_org_user oru ON oru.user_id = user.id WHERE ");
//		SQL sql = new SQL();
		EXP sql = EXP.INS().key("oru.org_id", orgId).andKey("oru.family_number", familyNumber);
//		sql.addEx("oru.org_id = ? ", orgId);
//		sql.AND("oru.family_number = ? ", familyNumber);

		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		return sqlGetJSONArray(conn, sb.toString(), params, 512, 0);
	}

	public List<ORGUser> getFamilyByFamilyMaster(DruidPooledConnection conn, Long orgId, String master, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();

//		sql.addEx("org_id = ? ", orgId);
//		sql.AND(StringUtils.join("family_master LIKE '%", master, "%'"));
//		sql.addEx("GROUP BY family_number");
//		sql.fillSQL(sb);
		EXP sql = EXP.INS().key("org_id", orgId).and(EXP.INS().LIKE("family_master", master));
		sql.toSQL(sb, params);
		sb.append(" GROUP BY family_number");
		return getList(conn, sb.toString(), params, count, offset);
	}

	public List<ORGUser> getFamilyByshare(DruidPooledConnection conn, Long orgId, String share, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
//		SQL sql = new SQL();
//		sql.addEx("org_id = ? ", orgId);
//		sql.AND(StringUtils.join("share_cer_no LIKE '%", share, "%'"));
//		sql.addEx("GROUP BY family_number");
//		sql.fillSQL(sb);
//		return getList(conn, sb.toString(), sql.getParams(), count, offset);

		EXP sql = EXP.INS().key("org_id", orgId).and(EXP.INS().LIKE("share_cer_no", share));
		sql.toSQL(sb, params);
		sb.append(" GROUP BY family_number");
		return getList(conn, sb.toString(), params, count, offset);
	}

	public List<ORGUser> getFamilyByFamilyNumber(DruidPooledConnection conn, Long orgId, Long number, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
//		SQL sql = new SQL();
//		sql.addEx("org_id = ? ", orgId);
//		sql.AND("family_number = ? ", number);
//		sql.addEx("GROUP BY family_number");
//		sql.fillSQL(sb);
//		return getList(conn, sb.toString(), sql.getParams(), count, offset);

		EXP sql = EXP.INS().key("org_id", orgId).andKey("family_number", number);
		sql.toSQL(sb, params);
		sb.append(" GROUP BY family_number");
		return getList(conn, sb.toString(), params, count, offset);
	}

	public ORGUser maxFamilyNumber(DruidPooledConnection conn, Long orgId) throws Exception {
		StringBuffer sb = new StringBuffer(
				"family_number = (SELECT MAX(family_number) FROM tb_ecm_org_user WHERE org_id = ?)");
		return get(conn, sb.toString(), Arrays.asList(orgId));
	}

	public int getOrgUser(DruidPooledConnection conn, Long orgId, String idNumber) throws Exception {
		// SELECT * FROM tb_user user RIGHT JOIN tb_ecm_org_user oru ON user.id =
		// oru.user_id
		// WHERE user.id_number = '522121196610244546' AND oru.org_id = 397652553337218

		StringBuffer sb = new StringBuffer(
				"SELECT * FROM tb_user user RIGHT JOIN tb_ecm_org_user oru ON user.id = oru.user_id WHERE ");
		EXP sql = EXP.INS();
		sql.key("oru.org_id", orgId);
		sql.andKey("user.id_number", idNumber);
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		JSONObject oru = sqlGetJSONObject(conn, sb.toString(), params);
		if (oru == null) {
			return 0;
		} else {
			return 1;
		}
	}

	public JSONArray getUserByRoles(DruidPooledConnection conn, Long orgId, JSONArray json) throws Exception {
		// SELECT * FROM tb_ecm_org_user oru LEFT JOIN tb_user user ON oru.user_id =
		// user.id WHERE
		// oru.org_id = 397652553337218 AND JSON_CONTAINS(oru.roles, '104')
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM tb_ecm_org_user oru LEFT JOIN tb_user user ON oru.user_id = user.id WHERE ");
		EXP sql = EXP.INS().key("oru.org_id", orgId);
		sql.and(EXP.JSON_CONTAINS_KEYS(json, "oru.roles", null));
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);

		return sqlGetJSONArray(conn, sb.toString(), params, 512, 0);
	}

	public JSONArray getORGUsersByRole(DruidPooledConnection conn, Long orgId, JSONArray json, Integer count,
			Integer offset) throws Exception {
		// SELECT user.wx_open_id FROM tb_user user RIGHT JOIN tb_ecm_org_user oru ON
		// user.id = oru.user_id
		// WHERE JSON_CONTAINS(oru.roles, '104','$') AND user.wx_open_id IS NOT NULL
		StringBuffer sb = new StringBuffer(
				"SELECT user.* FROM tb_ecm_org_user oru LEFT JOIN tb_user user ON oru.user_id = user.id WHERE ");
		EXP sql = EXP.INS().key("oru.org_id", orgId).and("user.wx_open_id IS NOT NULL", null, null);
		sql.and(EXP.JSON_CONTAINS_KEYS(json, "oru.roles", null));
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);

		return sqlGetJSONArray(conn, sb.toString(), params, count, offset);
	}

	public JSONArray getFamilyAll(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		StringBuffer sb = new StringBuffer("SELECT * FROM tb_ecm_org_user  WHERE ");
		EXP sql = EXP.INS().key("org_id", orgId).and("family_number IS NOT NULL", null, null)
				.append("GROUP BY family_master ORDER BY family_number ASC");
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		return sqlGetJSONArray(conn, sb.toString(), params, count, offset);
	}

	public int getCountByRole(DruidPooledConnection conn, Long orgId, Long role) throws Exception {
		JSONArray ja = new JSONArray();
		ja.add(role);
		EXP where = EXP.INS().key("org_id", orgId).and(EXP.JSON_CONTAINS_KEYS(ja, "roles", null));

		StringBuffer sql = new StringBuffer("SELECT COUNT(*) FROM tb_ecm_org_user WHERE ");
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		where.toSQL(sb, params);
		sql.append(sb);

		Object[] s = this.sqlGetObjects(conn, sql.toString(), params);

		int count = Integer.parseInt(s[0].toString());
		return count;
	}

	// 统计内部或外部成员总数
	public int getCount(DruidPooledConnection conn, Long orgId, Boolean isOrgUser) throws Exception {

		EXP where = EXP.INS().key("org_id", orgId).andKey("is_org_user", isOrgUser);

		StringBuffer sql = new StringBuffer("SELECT COUNT(*) FROM tb_ecm_org_user WHERE ");
		StringBuffer sb = new StringBuffer();

		List<Object> params = new ArrayList<Object>();
		where.toSQL(sb, params);
		sql.append(sb);

		Object[] s = this.sqlGetObjects(conn, sql.toString(), params);

		int count = Integer.parseInt(s[0].toString());
		return count;
	}

	public List<Object[]> getExportData(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		StringBuffer sql = new StringBuffer(
				"SELECT OU.family_number AS '户序号', OU.family_master AS '户主姓名', OU.address AS '地址', U.real_name AS '姓名', U.id_number AS '身份证号码', ou.is_org_user AS '是否组织成员', ou.share_amount AS '个人持股数（股）', ou.family_relations AS '与户主关系', ou.share_cer_no AS '成员股权证号', ou.resource_shares AS '本户资源股', ou.asset_shares AS '本户资产股', o.`name` AS '合作社名称', o.address AS '合作社地址', o.create_time AS '合作社成立时间', o.`code` AS '合作社信用代码', o.asset_shares AS '集体资产股', o.resource_shares AS '集体资源股' FROM ( tb_ecm_org O LEFT JOIN tb_ecm_org_user OU ON O.id = OU.org_id ) LEFT JOIN tb_user U ON OU.user_id = U.id WHERE O.id =");
		sql.append(orgId);
		sql.append(" ORDER BY OU.family_number");
		return this.sqlGetObjectsList(conn, sql.toString(), null, count, offset);
	}

	public int getExportDataCount(DruidPooledConnection conn, Long orgId) throws Exception {
		StringBuffer sql = new StringBuffer(
				"SELECT count(*) FROM ( tb_ecm_org O LEFT JOIN tb_ecm_org_user OU ON O.id = OU.org_id ) LEFT JOIN tb_user U ON OU.user_id = U.id WHERE O.id =");
		sql.append(orgId);
		Object[] s = this.sqlGetObjects(conn, sql.toString(), null);

		return Integer.parseInt(s[0].toString());
	}

	// 查询所有户主信息（只查询返回需要显示的字段）（待修改）
	public JSONArray getFamilyMasterList(DruidPooledConnection conn, String sql, Integer count, Integer offset)
			throws Exception {
		List<Object[]> olist = this.sqlGetObjectsList(conn, sql, null, count, offset);
		JSONArray masterArray = new JSONArray();

		for (int i = 0; i < olist.size(); i++) {
			JSONObject master = new JSONObject();
			Object[] s = olist.get(i);
			for (int j = 0; j < s.length; j++) {
				if (s[j] != null) {
					String is = s[j].toString();
					switch (j) {
					case 0:
						master.put("familyNumber", is);
						break;
					case 1:
						master.put("realName", is);
						break;
					case 2:
						master.put("idNumber", is);
						break;
					}
				} else {
					switch (j) {
					case 0:
						master.put("familyNumber", "");
						break;
					case 1:
						master.put("realName", "");
						break;
					case 2:
						master.put("idNumber", "");
						break;
					}
				}
			}
			masterArray.add(master);
		}
		return masterArray;
	}

	public JSONArray getUserIds(DruidPooledConnection conn, EXP exp) throws Exception {
		StringBuffer sb = new StringBuffer();
		List<Object> params = new ArrayList<Object>();
		exp.toSQL(sb, params);
		List<ORGUser> u = this.getList(conn, sb.toString(), params, null, null, "user_id");
		JSONArray ja = new JSONArray();
		for (ORGUser user : u) {
			ja.add(user.userId);
		}
		return ja;
	}

	public List<ORGUser> getUserss(DruidPooledConnection conn) throws Exception {
		String where = "share_cer_img is not null";
		return this.getList(conn, where, null, null, null);
	}

	public int delORGUser(DruidPooledConnection conn, Long id) throws Exception {
		// 获取当前组织管理员
		String where = StringUtils.join(" org_id = ", id, " and JSON_CONTAINS(roles, '102', '$')");
		List<ORGUser> adminList = this.getList(conn, where, null, null, null);
		String sql = StringUtils.join(" org_id = ", id);
		int length = adminList.size();
		if (length > 0) {
			for (int i = 0; i < length; i++) {
				sql = StringUtils.join(sql, " and user_id <> ", adminList.get(i).userId);
			}
		}
		return this.delete(conn, sql, null);
	}

	public Object[] getFamilyMaster(DruidPooledConnection conn, Long orgId, Long familyNumber, String familyMaster) throws Exception {
		String sql = StringUtils.join(
				"select u.id ,org_id, real_name, family_master, family_number, share_cer_no from tb_user u, tb_ecm_org_user o "
				+ "where u.id = o.user_id and u.real_name = o.family_master and o.org_id = ",
				orgId, " and o.family_number = ", familyNumber, " and o.family_master = '", familyMaster,"'");
		Object[] s = this.sqlGetObjects(conn, sql, null);
		return s;
	}

}
