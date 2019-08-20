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
import zyxhj.utils.data.rds.SQL;
import zyxhj.utils.data.rds.SQLEx;

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
			for (ORGUserRole r : roles) {
				for (int i = 0; i < arr.size(); i++) {
					Long id = arr.getLong(i);
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

		SQL sql = new SQL();
		
		sql.addEx("org_id = ? ");
		if ((roles != null && roles.size() > 0) || (groups != null && groups.size() > 0) || (tags != null)) {
			SQL sqlEx = new SQL();

			if (roles != null && roles.size() > 0) {
				for (int i = 0; i < roles.size(); i++) {
					sqlEx.OR(StringUtils.join("JSON_CONTAINS(roles, '", roles.getString(i), "', '$') "));
				}
			}

			if (groups != null && groups.size() > 0) {
				for (int i = 0; i < groups.size(); i++) {
					sqlEx.OR(StringUtils.join("JSON_CONTAINS(group, '", groups.getString(i), "', '$') "));
				}
			}

			if (tags != null) {
				Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, Object> entry = it.next();
					String key = entry.getKey();
					JSONArray arr = (JSONArray) entry.getValue();

					if (arr != null && arr.size() > 0) {
						for (int i = 0; i < arr.size(); i++) {
							// JSON_CONTAINS(tags, '"tag1"', '$.groups')
							// JSON_CONTAINS(tags, '"tag3"', '$.tags')
							sqlEx.OR(StringUtils.join("JSON_CONTAINS(tags, '\"", arr.getString(i), "\"', '$.", key,
									"') "));
						}
					}
				}
			}
			sql.AND(sqlEx);
		}

		StringBuffer s = new StringBuffer("SELECT COUNT(*) FROM tb_ecm_org_user WHERE ");

		sql.fillSQL(s);
		Object[] getObj = sqlGetObjects(conn, s.toString(), Arrays.asList(orgId));
		return Integer.parseInt(getObj[0].toString());

	}

	public int setORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, JSONArray roles)
			throws ServerException {
		if (roles == null || roles.size() <= 0) {
			return 0;
		} else {
			ORGUser renew = new ORGUser();
			renew.roles = JSON.toJSONString(roles);

			return update(conn,EXP.INS().key("org_id", orgId).andKey("user_id", userId), renew,true);
			
		}
	}

	public List<ORGUser> getORGUsersByRoles(DruidPooledConnection conn, Long orgId, String[] roles, Integer count,
			Integer offset) throws ServerException {
//		String[] a = new String[roles.size()];
//		for(int i = 0 ; i < roles.size() ; i++) {
//			a[i] = roles.getString(i);
//		}
		return getListByTagsJSONArray(conn, "roles", "", roles, " org_id=? ",Arrays.asList(orgId), count,
				offset);
	}

	public List<ORGUser> getORGUsersByGroups(DruidPooledConnection conn, Long orgId, String[] groups, Integer count,
			Integer offset) throws ServerException {

		return getListByTagsJSONArray(conn, "groups", "", groups, " org_id=? ", Arrays.asList(orgId), count,
				offset);
	}

	public List<ORGUser> getORGUsersByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws ServerException {

		return getListByTagsJSONObject(conn, "tags", tags, " org_id=? ", Arrays.asList(orgId), count, offset);
	}

	public List<User> getORGUsersLikeIDNumber(DruidPooledConnection conn, Long orgId, String idNumber, Integer count,
			Integer offset) throws ServerException {
		if (StringUtils.isNoneBlank(idNumber) && idNumber.length() > 1) {

			// return this.getList(conn, StringUtils.join("WHERE org_id=? AND id_number LIKE
			// '%", idNumber, "%'"),
			// new Object[] { orgId }, count, offset);

			try {
				// return this.nativeGetJSONArray(conn, sql.toString(), new Object[] { orgId,
				// count, offset });
				return sqlGetOtherList(conn, Singleton.ins(UserRepository.class), StringUtils.join(
						"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`id_number` LIKE '%",
						idNumber, "%' LIMIT ? OFFSET ?"), Arrays.asList(orgId, count, offset ));
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		} else {
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
						realName, "%' LIMIT ? OFFSET ?"), Arrays.asList(orgId, count, offset ));
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

		// SET groups="[123,456,345]"
		// StringBuffer sbset = new StringBuffer(" SET ");
		// ArrayList<Object> pset = new ArrayList<>();
		// SQL sqlset = new SQL();

		// 不能为空，为空需要填写默认分组
//		sbset.append("SET groups=?");
//		if (groups == null || groups.size() <= 0) {
//			// 填入未分组，避免空
//			groups = new JSONArray();
//			groups.add(ORGUserTagGroup.group_undefine.groupId);
//		}
		// 判断原来的用户是否有这个分组 没有的话添加分组 不要直接覆盖分组
		// SELECT * FROM tb_ecm_org_user WHERE org_id = 397652553337218 and user_id =
		// 397652692024985
		// AND JSON_CONTAINS(groups,'397652645549447','$')

		for (int i = 0; i < userIds.size(); i++) {
			// 查询用户
//			ORGUser getORGUser = getByANDKeys(conn, new String[] { "org_id", "user_id" },
//					new Object[] { orgId, userIds.getLong(i) });
			ORGUser getORGUser = get(conn, StringUtils.join("org_id = ", orgId," AND user_id",userIds.getLong(i)),null);

			// 等于-1表示不存在
			if (getORGUser.groups.indexOf(groups.toString()) == -1) {
				// 不存在分组 给组织用户添加分组
				JSONArray json = JSONArray.parseArray(getORGUser.groups);
				json.add(groups);
				ORGUser or = new ORGUser();
				or.groups = json.toString();
				update(conn,EXP.INS().key("org_id", orgId).andKey("user_id", userIds.getLong(i)), or, true);
				
			}

		}

	}

	public Map<String, Integer> countRole(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		Map<String, Integer> map = new HashMap<String, Integer>();

		// SELECT COUNT(*) FROM tb_ecm_org_user WHERE JSON_CONTAINS(roles, '101','$')

		for (int i = 0; i < roles.size(); i++) {
			// 获取roles值
			String ro = roles.getString(i);
			Object[] s = sqlGetObjects(conn,
					StringUtils.join("SELECT COUNT(*) FROM tb_ecm_org_user WHERE JSON_CONTAINS(roles, '", ro, "','$')"), null);
			map.put(ro, Integer.parseInt(s[0].toString()));
		}
		return map;
	}

	public List<ORGUser> getORGUsersInfoByUsers(DruidPooledConnection conn, Long orgId, Object[] values)
			throws Exception {
		StringBuffer sb = new StringBuffer();
		SQL sql = new SQL();
		sql.addEx("org_id = ?", orgId);
		sql.AND(SQLEx.exIn("user_id", values));
		sql.fillSQL(sb);
		// System.out.println(sb.toString());
		return getList(conn, sb.toString(), sql.getParams(), null, null);
	}

	public JSONArray getORGAdmin(DruidPooledConnection conn, Long orgId, Byte level, Integer count, Integer offset)
			throws Exception {
		SQL sql = new SQL();
		// SELECT u.* FROM tb_ecm_org_user oru LEFT JOIN tb_user u ON oru.user_id = u.id
		// WHERE org_id = 397652553337218 AND JSON_CONTAINS(oru.roles, "102",'$')
		StringBuffer sb = new StringBuffer(
				"SELECT u.* FROM tb_ecm_org_user oru LEFT JOIN tb_user u ON oru.user_id = u.id WHERE ");
		sql.addEx("org_id = ?", orgId);
		SQL sqlAnd = new SQL();
		if (level == ORG.LEVEL.COOPERATIVE.v()) {
			sqlAnd.AND(StringUtils.join("JSON_CONTAINS(oru.roles, \"102\",'$')"));
		}
		if (level != ORG.LEVEL.COOPERATIVE.v()) {
			sqlAnd.OR(StringUtils.join("JSON_CONTAINS(oru.roles, \"112\",'$')"));
		}
		sql.AND(sqlAnd);
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public JSONArray getFamilyUserBYFamilyId(DruidPooledConnection conn, Long orgId, Long familyNumber)
			throws Exception {
		// SELECT * FROM tb_ecm_org_user oru LEFT JOIN tb_user user ON oru.user_id =
		// user.id WHERE
		// oru.org_id = 398977803603065 AND oru.family_number = 1475
		StringBuffer sb = new StringBuffer(
				"SELECT * FROM tb_user user  RIGHT JOIN tb_ecm_org_user oru ON oru.user_id = user.id WHERE ");
		SQL sql = new SQL();
		sql.addEx("oru.org_id = ? ", orgId);
		sql.AND("oru.family_number = ? ", familyNumber);
		sql.fillSQL(sb);
		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), 512, 0);
	}

	public List<ORGUser> getFamilyByFamilyMaster(DruidPooledConnection conn, Long orgId, String master, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		SQL sql = new SQL();
		sql.addEx("org_id = ? ", orgId);
		sql.AND(StringUtils.join("family_master LIKE '%", master, "%'"));
		sql.addEx("GROUP BY family_number");
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public List<ORGUser> getFamilyByshare(DruidPooledConnection conn, Long orgId, String share, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		SQL sql = new SQL();
		sql.addEx("org_id = ? ", orgId);
		sql.AND(StringUtils.join("share_cer_no LIKE '%", share, "%'"));
		sql.addEx("GROUP BY family_number");
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public List<ORGUser> getFamilyByFamilyNumber(DruidPooledConnection conn, Long orgId, Long number, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer();
		SQL sql = new SQL();
		sql.addEx("org_id = ? ", orgId);
		sql.AND("family_number = ? ", number);
		sql.addEx("GROUP BY family_number");
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public ORGUser maxFamilyNumber(DruidPooledConnection conn, Long orgId) throws Exception {
		StringBuffer sb = new StringBuffer(
				"family_number = (SELECT MAX(family_number) FROM tb_ecm_org_user WHERE org_id = ?)");
		return get(conn, sb.toString(), Arrays.asList(orgId ));
	}

	public int getOrgUser(DruidPooledConnection conn, Long orgId, String idNumber) throws Exception {
		// SELECT * FROM tb_user user RIGHT JOIN tb_ecm_org_user oru ON user.id =
		// oru.user_id
		// WHERE user.id_number = '522121196610244546' AND oru.org_id = 397652553337218

		StringBuffer sb = new StringBuffer(
				"SELECT * FROM tb_user user RIGHT JOIN tb_ecm_org_user oru ON user.id = oru.user_id WHERE ");
		SQL sql = new SQL();
		sql.addEx("oru.org_id = ? ", orgId);
		sql.AND("user.id_number = ? ", idNumber);
		sql.fillSQL(sb);
		System.out.println(sb.toString());
		JSONObject oru = sqlGetJSONObject(conn, sb.toString(), sql.getParams());
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
		SQL sql = new SQL();
		sql.addEx("oru.org_id = ? ", orgId);
		SQL sqlEx = new SQL();
		for (int i = 0; i < json.size(); i++) {
			sqlEx.OR(StringUtils.join("JSON_CONTAINS(oru.roles, '", json.getLong(i), "','$')"));
		}
		sql.AND(sqlEx);
		sql.fillSQL(sb);

		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), 512, 0);
	}

	public JSONArray getORGUsersByRole(DruidPooledConnection conn, Long orgId, JSONArray json, Integer count,
			Integer offset) throws Exception {
		// SELECT user.wx_open_id FROM tb_user user RIGHT JOIN tb_ecm_org_user oru ON
		// user.id = oru.user_id
		// WHERE JSON_CONTAINS(oru.roles, '104','$') AND user.wx_open_id IS NOT NULL
		StringBuffer sb = new StringBuffer(
				"SELECT user.* FROM tb_ecm_org_user oru LEFT JOIN tb_user user ON oru.user_id = user.id WHERE ");
		SQL sql = new SQL();
		sql.addEx("oru.org_id = ? ", orgId);
		sql.AND(" user.wx_open_id IS NOT NULL ");
		SQL sqlEx = new SQL();
		for (int i = 0; i < json.size(); i++) {
			sqlEx.OR(StringUtils.join("JSON_CONTAINS(oru.roles, '", json.getLong(i), "','$')"));
		}
		sql.AND(sqlEx);
		sql.fillSQL(sb);

		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public JSONArray getFamilyAll(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		StringBuffer sb = new StringBuffer("SELECT * FROM tb_ecm_org_user  WHERE ");
		SQL sql = new SQL();
		sql.addEx("org_id = ? ", orgId);
		sql.AND(" family_number IS NOT NULL ");
		sql.addEx("GROUP BY family_master");
		sql.fillSQL(sb);
		return sqlGetJSONArray(conn, sb.toString(), sql.getParams(), count, offset);
	}

}
