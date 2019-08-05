package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.jiti.repository.ORGUserTagGroupRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

/**
 * 第三方用户自定义角色service
 *
 */
public class ORGUserGroupService {

	private static Logger log = LoggerFactory.getLogger(ORGUserGroupService.class);

	/**
	 * 系统级第三方权限，会被
	 */
	private static HashMap<Long, ORGUserTagGroup> SYS_ORG_USER_TAG_GROUP_MAP = new HashMap<>();
	public static ArrayList<ORGUserTagGroup> SYS_ORG_USER_TAG_GROUP_LIST = new ArrayList<>();

	static {
		// 添加admin，member，股东，董事，监事等角色到系统中
		SYS_ORG_USER_TAG_GROUP_MAP.put(ORGUserTagGroup.group_groups.groupId, ORGUserTagGroup.group_groups);
		// undefine 在 groups下面，也是默认分组
		SYS_ORG_USER_TAG_GROUP_MAP.put(ORGUserTagGroup.group_undefine.groupId, ORGUserTagGroup.group_undefine);

		SYS_ORG_USER_TAG_GROUP_MAP.put(ORGUserTagGroup.group_tags.groupId, ORGUserTagGroup.group_tags);

		Iterator<ORGUserTagGroup> it = SYS_ORG_USER_TAG_GROUP_MAP.values().iterator();
		while (it.hasNext()) {
			SYS_ORG_USER_TAG_GROUP_LIST.add(it.next());
		}
	}

	private static Cache<Long, List<ORGUserTagGroup>> ORG_USER_TAG_GROUP_LIST_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(100)//
			.build();

	private ORGUserTagGroupRepository groupRepository;

	public ORGUserGroupService() {
		try {
			groupRepository = Singleton.ins(ORGUserTagGroupRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建自定义标签分组
	 */
	public ORGUserTagGroup createTagGroup(DruidPooledConnection conn, Long orgId, Long parentId, JSONArray parents,
			String keyword, String remark) throws Exception {
		ORGUserTagGroup group = new ORGUserTagGroup();
		group.orgId = orgId;
		group.groupId = IDUtils.getSimpleId();
		group.parentId = parentId;
		if (parents == null || parents.size() <= 0) {
			group.parents = "[]";
		} else {
			group.parents = JSON.toJSONString(parents);
		}
		group.keyword = keyword;
		group.remark = remark;

		groupRepository.insert(conn, group);

		return group;
	}

	/**
	 * 编辑自定义角色
	 */
	public int editTagGroup(DruidPooledConnection conn, Long orgId, Long groupId, Long parentId, JSONArray parents,
			String keyword, String remark) throws Exception {
		ORGUserTagGroup renew = new ORGUserTagGroup();
		renew.parentId = parentId;
		if (parents == null || parents.size() <= 0) {
			renew.parents = "[]";
		} else {
			renew.parents = JSON.toJSONString(parents);
		}
		renew.keyword = keyword;
		renew.remark = remark;

		return groupRepository.update(conn,EXP.ins().key("org_id", orgId).andKey("group_id", groupId), renew, true);
		

	}

	public List<ORGUserTagGroup> getTagGroups(DruidPooledConnection conn, Long orgId) throws Exception {
		List<ORGUserTagGroup> ret = ORG_USER_TAG_GROUP_LIST_CACHE.getIfPresent(orgId);
		if (ret == null) {
			ret = groupRepository.getList(conn,EXP.ins().key("org_id", orgId), 512, 0);
			ORG_USER_TAG_GROUP_LIST_CACHE.put(orgId, ret);
		}
		return ret;
	}

	public int delTagGroupById(DruidPooledConnection conn, Long groupId) throws Exception {
		return groupRepository.delete(conn,EXP.ins().key("group_id", groupId));
	}

	public JSONArray getTagGroupTree(DruidPooledConnection conn, Long orgId, Long groupId) throws Exception {
		return groupRepository.getTagGroupTree(conn, orgId, groupId);
	}
}
