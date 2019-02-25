package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.cms.domain.ContentTag;
import zyxhj.jiti.domain.ORGUserTag;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.jiti.repository.ORGUserTagGroupRepository;
import zyxhj.jiti.repository.ORGUserTagRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;

/**
 * 第三方用户自定义角色service
 *
 */
public class ORGUserTagService {

	private static Logger log = LoggerFactory.getLogger(ORGUserTagService.class);

	/**
	 * 系统级第三方权限，会被
	 */
	private static HashMap<Long, ORGUserTagGroup> SYS_ORG_USER_TAG_GROUP_MAP = new HashMap<>();

	static {
		// 添加admin，member，股东，董事，监事等角色到系统中
		SYS_ORG_USER_TAG_GROUP_MAP.put(ORGUserTagGroup.group_groups.groupId, ORGUserTagGroup.group_groups);
		SYS_ORG_USER_TAG_GROUP_MAP.put(ORGUserTagGroup.group_tags.groupId, ORGUserTagGroup.group_tags);

	}

	private static Cache<Long, ORGUserTag> ORG_USER_TAG_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(1000)//
			.build();

	private static Cache<Long, ORGUserTagGroup> ORG_USER_TAG_GROUP_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(1000)//
			.build();

	private ORGUserTagRepository tagRepository;
	private ORGUserTagGroupRepository groupRepository;

	public ORGUserTagService() {
		try {
			tagRepository = Singleton.ins(ORGUserTagRepository.class);
			groupRepository = Singleton.ins(ORGUserTagGroupRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建自定义标签
	 */
	public ORGUserTag createTag(DruidPooledConnection conn, Long orgId, String groupKeyword, String name)
			throws Exception {
		ORGUserTag tag = new ORGUserTag();
		tag.orgId = orgId;
		tag.tagId = IDUtils.getSimpleId();
		tag.status = ContentTag.STATUS.ENABLED.v();
		tag.groupKeyword = groupKeyword;
		tag.name = name;

		tagRepository.insert(conn, tag);

		return tag;
	}

	/**
	 * 启用/禁用标签
	 */
	public int editTagStatus(DruidPooledConnection conn, Long orgId, Long tagId, Byte status) throws Exception {
		ORGUserTag renew = new ORGUserTag();
		if (status == ORGUserTag.STATUS.DISABLED.v()) {
			renew.status = status;
		} else {
			renew.status = ORGUserTag.STATUS.ENABLED.v();
		}

		return tagRepository.updateByKeys(conn, new String[] { "org_id", "tag_id" }, new Object[] { orgId, tagId },
				renew, true);
	}

	/**
	 * 根据状态获取标签列表
	 */
	public List<ORGUserTag> getTags(DruidPooledConnection conn, Long orgId, Byte status, String groupKeyword,
			Integer count, Integer offset) throws Exception {
		return tagRepository.getListByKeys(conn, new String[] { "org_id", "status", "group_keyword" },
				new Object[] { orgId, status, groupKeyword }, count, offset);
	}

	/**
	 * 根据编号获取自定义标签
	 */
	public ORGUserTag getTagById(DruidPooledConnection conn, Long orgId, Long tagId) throws Exception {
		// 先从系统缓存里取，再从缓存去，最后再查
		ORGUserTag tag = ORG_USER_TAG_CACHE.getIfPresent(tagId);
		if (tag == null) {
			// 从数据库中获取
			tag = tagRepository.getByKeys(conn, new String[] { "org_id", "tag_id" }, new Object[] { orgId, tagId });
			if (tag != null) {
				// 放入缓存
				ORG_USER_TAG_CACHE.put(tagId, tag);
			}
		}
		return tag;
	}

	////////////////

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

		return groupRepository.updateByKeys(conn, new String[] { "org_id", "group_id" },
				new Object[] { orgId, groupId }, renew, true);
	}

	public ORGUserTagGroup getTagGroupById(DruidPooledConnection conn, Long groupId) throws Exception {
		// 先从系统缓存里取，再从缓存去，最后再查
		ORGUserTagGroup group = SYS_ORG_USER_TAG_GROUP_MAP.get(groupId);
		if (group == null) {
			group = ORG_USER_TAG_GROUP_CACHE.getIfPresent(groupId);
			if (group == null) {
				// 从数据库中获取
				group = groupRepository.getByKey(conn, "group_id", groupId);
				if (group != null) {
					// 放入缓存
					ORG_USER_TAG_GROUP_CACHE.put(groupId, group);
				}
			}
		}
		return group;
	}

	public int delTagGroupById(DruidPooledConnection conn, Long groupId) throws Exception {
		return groupRepository.deleteByKey(conn, "group_id", groupId);
	}

	/**
	 * 获取标签分组列表
	 */
	public List<ORGUserTagGroup> getSysTagGroups(DruidPooledConnection conn) throws Exception {
		Iterator<ORGUserTagGroup> it = SYS_ORG_USER_TAG_GROUP_MAP.values().iterator();
		ArrayList<ORGUserTagGroup> ret = new ArrayList<>();
		while (it.hasNext()) {
			ret.add(it.next());
		}
		return ret;
	}

	public JSONArray getTagGroupTree(DruidPooledConnection conn, Long orgId, Long groupId) throws Exception {
		return groupRepository.getTagGroupTree(conn, orgId, groupId);
	}
}
