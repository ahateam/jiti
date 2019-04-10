package zyxhj.jiti.repository;

import java.util.ArrayList;
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
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
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
		ORGUser orgUser = getByANDKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId });
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
			SQL sq = new SQL();

			if (roles != null && roles.size() > 0) {
				for (int i = 0; i < roles.size(); i++) {
					sq.OR(StringUtils.join("JSON_CONTAINS(roles, '", roles.getString(i), "', '$') "));
				}
			}

			if (groups != null && groups.size() > 0) {
				for (int i = 0; i < groups.size(); i++) {
					sq.OR(StringUtils.join("JSON_CONTAINS(group, '", groups.getString(i), "', '$') "));
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
							sq.OR(StringUtils.join("JSON_CONTAINS(tags, '\"", arr.getString(i), "\"', '$.", key,
									"') "));
						}
					}
				}
			}
			sql.AND(sq);
		}

		StringBuffer s = new StringBuffer("SELECT COUNT(*) FROM tb_ecm_org_user WHERE ");

		sql.fillSQL(s);
		Object[] getObj = sqlGetObjects(conn, s.toString(), new Object[] { orgId });
		return Integer.parseInt(getObj[0].toString());

	}

	public int setORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, JSONArray roles)
			throws ServerException {
		if (roles == null || roles.size() <= 0) {
			return 0;
		} else {
			ORGUser renew = new ORGUser();
			renew.roles = JSON.toJSONString(roles);

			return updateByANDKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId }, renew,
					true);
		}
	}

	public List<ORGUser> getORGUsersByRoles(DruidPooledConnection conn, Long orgId, JSONObject roles, Integer count,
			Integer offset) throws ServerException {
		return this.getListByTags(conn, "roles", roles, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
	}

	public List<ORGUser> getORGUsersByGroups(DruidPooledConnection conn, Long orgId, JSONObject groups, Integer count,
			Integer offset) throws ServerException {

		return this.getListByTags(conn, "groups", groups, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
	}

	public List<ORGUser> getORGUsersByTags(DruidPooledConnection conn, Long orgId, JSONObject tags, Integer count,
			Integer offset) throws ServerException {

		return this.getListByTags(conn, "tags", tags, "WHERE org_id=? ", new Object[] { orgId }, count, offset);
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
				return this.sqlGetOtherList(conn, Singleton.ins(UserRepository.class), StringUtils.join(
						"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`id_number` LIKE '%",
						idNumber, "%' LIMIT ? OFFSET ?)"), new Object[] { orgId, count, offset });
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
				return this.sqlGetOtherList(conn, Singleton.ins(UserRepository.class), StringUtils.join(
						"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`real_name` LIKE '%",
						realName, "%' LIMIT ? OFFSET ?"), new Object[] { orgId, count, offset });
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<>();
			}
		} else {
			return new ArrayList<>();
		}

	}

	public int batchEditORGUsersGroups(DruidPooledConnection conn, Long orgId, JSONArray userIds, JSONArray groups)
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

		sbwhere.append("WHERE org_id=? AND user_id IN (");
		pwhere.add(orgId);

		if (userIds != null && userIds.size() > 0) {
			for (int i = 0; i < userIds.size(); i++) {
				Long userId = userIds.getLong(i);
				sbwhere.append("?,");
				pwhere.add(userId);
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

	public Map<String, Integer> countRole(DruidPooledConnection conn, Long orgId, JSONArray roles) throws Exception {
		Map<String, Integer> map = new HashMap<String, Integer>();

		// SELECT COUNT(*) FROM tb_ecm_org_user WHERE JSON_CONTAINS(roles, '101','$')

		for (int i = 0; i < roles.size(); i++) {
			// 获取roles值
			String ro = roles.getString(i);
			Object[] s = sqlGetObjects(conn,
					StringUtils.join("SELECT COUNT(*) FROM tb_ecm_org_user WHERE JSON_CONTAINS(roles, '",
							ro, "','$')"),
					new Object[] {});
			map.put(ro, Integer.parseInt(s[0].toString()));
		}
		return map;
	}

	public List<ORGUser> getORGUsersInfoByUsers(DruidPooledConnection conn, Long orgId, Object[] values)
			throws Exception {
		StringBuffer sb = new StringBuffer(" WHERE ");
		SQL sql = new SQL();
		sql.addEx("org_id = ?", orgId);
		sql.addEx(SQLEx.exIn("user_id", values));
		sql.fillSQL(sb);
		return getList(conn, sb.toString(),sql.getParams(), null, null);
	}

}
