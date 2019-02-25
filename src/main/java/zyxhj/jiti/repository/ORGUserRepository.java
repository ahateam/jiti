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

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserRepository extends RDSRepository<ORGUser> {

	public ORGUserRepository() {
		super(ORGUser.class);
	}

	/**
	 * 检查ORGUser权限
	 * 
	 * @param roles
	 *            需要具备的权限数组
	 */
	public ORGUser checkORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, ORGUserRole[] roles)
			throws ServerException {
		ORGUser orgUser = getByKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId });
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
		// 通过ROGUser的role，group和tag来判定人数
		// 人群是重叠的，所以查询比较难写

		// WHERE org_id=? AND (JSON_CONTAINS(roles, '101', '$') OR JSON_CONTAINS(roles,
		// '102', '$') OR JSON_CONTAINS(roles, '103', '$') OR JSON_CONTAINS(roles,
		// '104', '$') OR JSON_CONTAINS(roles, '105', '$') OR JSON_CONTAINS(roles,
		// '106', '$') OR JSON_CONTAINS(roles, '107', '$') OR JSON_CONTAINS(roles,
		// '108', '$') OR JSON_CONTAINS(roles, '109', '$') )

		JSONArray roles = crowd.getJSONArray("roles");
		JSONObject tags = crowd.getJSONObject("tags");

		StringBuffer sb = new StringBuffer();
		boolean flg = false;
		sb.append("WHERE org_id=? AND (");
		if (roles != null && roles.size() > 0) {
			for (int i = 0; i < roles.size(); i++) {
				String role = roles.getString(i);
				sb.append("JSON_CONTAINS(roles, '").append(role).append("', '$') OR ");
				flg = true;
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
						String temp = arr.getString(i);
						// JSON_CONTAINS(tags, '"tag1"', '$.groups')
						// JSON_CONTAINS(tags, '"tag3"', '$.tags')
						sb.append("JSON_CONTAINS(tags, '\"").append(temp).append("\"', '$.").append(key)
								.append("') OR ");
						flg = true;
					}
				}
			}
		}

		if (!flg) {
			// 一个查询条件都没有进入，则直接返回0
			return 0;
		} else {
			sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR

			sb.append(" )");

			String where = sb.toString();

			System.out.println(where);
			return this.count(conn, where, new Object[] { orgId });
		}

	}

	public int setORGUserRoles(DruidPooledConnection conn, Long orgId, Long userId, JSONArray roles)
			throws ServerException {
		if (roles == null || roles.size() <= 0) {
			return 0;
		} else {
			ORGUser renew = new ORGUser();
			renew.roles = JSON.toJSONString(roles);

			return updateByKeys(conn, new String[] { "org_id", "user_id" }, new Object[] { orgId, userId }, renew,
					true);
		}
	}

	public List<ORGUser> getORGUsersByRoles(DruidPooledConnection conn, Long orgId, JSONArray roles, Integer count,
			Integer offset) throws ServerException {
		// WHERE org_id=? AND (JSON_CONTAINS(roles, '101', '$') OR JSON_CONTAINS(roles,
		// '102', '$') OR JSON_CONTAINS(roles, '103', '$') OR JSON_CONTAINS(roles,
		// '104', '$') OR JSON_CONTAINS(roles, '105', '$') OR JSON_CONTAINS(roles,
		// '106', '$') OR JSON_CONTAINS(roles, '107', '$') OR JSON_CONTAINS(roles,
		// '108', '$') OR JSON_CONTAINS(roles, '109', '$') )

		StringBuffer sb = new StringBuffer();
		boolean flg = false;
		sb.append("WHERE org_id=? AND (");
		if (roles != null && roles.size() > 0) {
			for (int i = 0; i < roles.size(); i++) {
				String role = roles.getString(i);
				sb.append("JSON_CONTAINS(roles, '").append(role).append("', '$') OR ");
				flg = true;
			}
		}

		if (!flg) {
			// 一个查询条件都没有进入，则直接返回0
			return new ArrayList<ORGUser>();
		} else {
			sb.delete(sb.length() - 3, sb.length() - 1);// 移除最后的 OR

			sb.append(" )");

			String where = sb.toString();

			System.out.println(where);
			return this.getList(conn, sb.toString(), new Object[] { orgId }, count, offset);
		}

	}

	public List<User> getORGUsersLikeIDNumber(DruidPooledConnection conn, Long orgId, String idNumber, Integer count,
			Integer offset) throws ServerException {
		if (StringUtils.isNoneBlank(idNumber) && idNumber.length() > 1) {

			// return this.getList(conn, StringUtils.join("WHERE org_id=? AND id_number LIKE
			// '%", idNumber, "%'"),
			// new Object[] { orgId }, count, offset);

			StringBuffer sql = new StringBuffer(
					"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`id_number` LIKE '%");
			sql.append(idNumber).append("%' LIMIT ? OFFSET ?");
			try {
				return this.nativeGetList(conn, Singleton.ins(UserRepository.class), sql.toString(),
						new Object[] { orgId, count, offset });
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<User>();
			}
		} else {
			return new ArrayList<User>();
		}

	}

	public List<User> getORGUsersLikeRealName(DruidPooledConnection conn, Long orgId, String realName, Integer count,
			Integer offset) throws ServerException {
		if (StringUtils.isNoneBlank(realName)) {

			// return this.getList(conn, StringUtils.join("WHERE org_id=? AND id_number LIKE
			// '%", idNumber, "%'"),
			// new Object[] { orgId }, count, offset);

			StringBuffer sql = new StringBuffer(
					"SELECT * FROM `tb_user` INNER JOIN `tb_ecm_org_user` ON `tb_user`.`id` = `tb_ecm_org_user`.`user_id` WHERE `org_id` =? AND `tb_user`.`real_name` LIKE '%");
			sql.append(realName).append("%' LIMIT ? OFFSET ?");
			try {
				return this.nativeGetList(conn, Singleton.ins(UserRepository.class), sql.toString(),
						new Object[] { orgId, count, offset });
			} catch (Exception e) {
				e.printStackTrace();
				return new ArrayList<User>();
			}
		} else {
			return new ArrayList<User>();
		}

	}
}
