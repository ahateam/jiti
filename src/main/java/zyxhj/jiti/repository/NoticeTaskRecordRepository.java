package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.NoticeTaskRecord;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class NoticeTaskRecordRepository extends RDSRepository<NoticeTaskRecord> {

	public NoticeTaskRecordRepository() {
		super(NoticeTaskRecord.class);
	}

	public void addNoticeTaskOpenId(DruidPooledConnection conn, Long noId, Long orgId, String roles) throws Exception {
		JSONArray json = JSONArray.parseArray(roles);
		// SELECT user.* FROM tb_user user LEFT JOIN tb_ecm_org_user orguser ON user.id
		// = orguser.user_id
		// WHERE org_id = 397652553337218 AND status = 1 AND
		// JSON_CONTAINS(orguser.roles, '102')
		StringBuffer sb = new StringBuffer(
				"SELECT user.* FROM tb_user user LEFT JOIN tb_ecm_org_user orguser ON user.id = orguser.user_id WHERE ");

		EXP sql = EXP.INS().key("org_id", orgId).and("user.wx_open_id IS NOT NULL", null, null);
		EXP sqlEx = EXP.JSON_CONTAINS_KEYS(json, "orguser.roles", null);
//		for (int i = 0; i < json.size(); i++) {
//			sqlEx.OR(StringUtils.join(" JSON_CONTAINS(orguser.roles, '", json.getLong(i), "')"));
//		}
		sql.and(sqlEx);
		List<Object> params = new ArrayList<Object>();
		sql.toSQL(sb, params);
		List<User> us = sqlGetOtherList(conn, Singleton.ins(UserRepository.class), sb.toString(), params);

		NoticeTaskRecord noti = new NoticeTaskRecord();
		noti.taskId = noId;
		noti.status = NoticeTaskRecord.STATUS.UNDETECTED.v();
		// 向消息任务表内添加openID
		for (User user : us) {
			noti.openId = user.wxOpenId;
			this.insert(conn, noti);
		}
	}

	public Integer addNoticeTaskRecord(DruidPooledConnection conn, Long orgId, Long taskId, JSONObject crowd)
			throws Exception {
		// 通过ROGUser的roles，groups和tags来判定人数
		// 人群是重叠的，所以查询比较难写

		// WHERE org_id=? AND (JSON_CONTAINS(roles, '101', '$') OR JSON_CONTAINS(roles,
		// '102', '$') OR JSON_CONTAINS(roles, '103', '$') OR JSON_CONTAINS(roles,
		// '104', '$') OR JSON_CONTAINS(roles, '105', '$') )

		JSONArray roles = crowd.getJSONArray("roles");
		JSONArray groups = crowd.getJSONArray("groups");
		JSONObject tags = crowd.getJSONObject("tags");

		EXP sql = EXP.INS().key("org_id", orgId).and("user.wx_open_id IS NOT NULL", null, null);

		if ((roles != null && roles.size() > 0) || (groups != null && groups.size() > 0) || (tags != null)) {
			EXP sqlEx = EXP.INS();
			if (roles != null && roles.size() > 0) {
				sqlEx.JSON_CONTAINS_KEYS(roles, "orguser.roles", null);
//				for (int i = 0; i < roles.size(); i++) {
//					sqlEx.OR(StringUtils.join("JSON_CONTAINS(orguser.roles, '", roles.getString(i), "', '$') "));
//				}
			}

			if (groups != null && groups.size() > 0) {
				sqlEx.JSON_CONTAINS_KEYS(groups, "orguser.group", null);
//				for (int i = 0; i < groups.size(); i++) {
//					sqlEx.OR(StringUtils.join("JSON_CONTAINS(orguser.group, '", groups.getString(i), "', '$') "));
//				}
			}

			if (tags != null) {
				sqlEx.JSON_CONTAINS_JSONOBJECT(tags, "orguser.tags");
//				Iterator<Entry<String, Object>> it = tags.entrySet().iterator();
//				while (it.hasNext()) {
//					Entry<String, Object> entry = it.next();
//					String key = entry.getKey();
//					JSONArray arr = (JSONArray) entry.getValue();
//
//					if (arr != null && arr.size() > 0) {
//						for (int i = 0; i < arr.size(); i++) {
//							// JSON_CONTAINS(tags, '"tag1"', '$.groups')
//							// JSON_CONTAINS(tags, '"tag3"', '$.tags')
//							sqlEx.OR(StringUtils.join("JSON_CONTAINS(orguser.tags, '\"", arr.getString(i), "\"', '$.", key,
//									"') "));
//						}
//					}
//				}
			}
			sql.and(sqlEx);
		}

		StringBuffer s = new StringBuffer(
				"SELECT user.* FROM tb_user user LEFT JOIN tb_ecm_org_user orguser ON user.id = orguser.user_id WHERE ");

		List<Object> params = new ArrayList<Object>();
		sql.toSQL(s, params);
		List<User> us = sqlGetOtherList(conn, Singleton.ins(UserRepository.class), s.toString(), params);

		Integer sum = 0;
		NoticeTaskRecord noti = new NoticeTaskRecord();
		noti.taskId = taskId;
		noti.orgId = orgId;
		noti.status = NoticeTaskRecord.STATUS.UNDETECTED.v();
		// 向消息任务表内添加openID
		for (User user : us) {
			noti.userId = user.id;
			noti.openId = user.wxOpenId;
			noti.mobile = user.mobile;
			this.insert(conn, noti);
			sum++;
		}

		return sum;

	}

}
