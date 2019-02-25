package zyxhj.jiti.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.ORGUserGroup;
import zyxhj.jiti.repository.ORGUserGroupRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;

/**
 * 第三方用户自定义角色service
 *
 */
public class ORGUserGroupService {

	private static Logger log = LoggerFactory.getLogger(ORGUserGroupService.class);

	private static Cache<Long, ORGUserGroup> ORG_USER_GROUP_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(1000)//
			.build();

	private ORGUserGroupRepository orgUserGroupRepository;

	public ORGUserGroupService() {
		try {
			orgUserGroupRepository = Singleton.ins(ORGUserGroupRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建自定义角色
	 */
	public ORGUserGroup createORGUserGroup(DruidPooledConnection conn, Long orgId, String name, String remark)
			throws Exception {
		ORGUserGroup group = new ORGUserGroup();
		group.orgId = orgId;
		group.groupId = IDUtils.getSimpleId();
		group.name = name;
		group.remark = remark;

		orgUserGroupRepository.insert(conn, group);

		return group;
	}

	/**
	 * 编辑自定义角色
	 */
	public int editORGUserGroup(DruidPooledConnection conn, Long orgId, Long groupId, String name, String remark)
			throws Exception {
		ORGUserGroup renew = new ORGUserGroup();
		renew.name = name;
		renew.remark = remark;

		return orgUserGroupRepository.updateByKeys(conn, new String[] { "org_id", "group_id" },
				new Object[] { orgId, groupId }, renew, true);
	}

	/**
	 * 删除自定义角色
	 */
	public int delORGUserGroup(DruidPooledConnection conn, Long orgId, Long groupId) throws Exception {
		return orgUserGroupRepository.deleteByKeys(conn, new String[] { "org_id", "group_id" },
				new Object[] { orgId, groupId });
	}

	public ORGUserGroup getORGUserGroupById(DruidPooledConnection conn, Long orgId, Long groupId) throws Exception {
		// 先从系统缓存里取，再从缓存去，最后再查
		ORGUserGroup group = ORG_USER_GROUP_CACHE.getIfPresent(groupId);
		if (group == null) {
			// 从数据库中获取
			group = orgUserGroupRepository.getByKeys(conn, new String[] { "org_id", "group_id" },
					new Object[] { orgId, groupId });
			if (group != null) {
				// 放入缓存
				ORG_USER_GROUP_CACHE.put(groupId, group);
			}
		}
		return group;
	}

	/**
	 * 获取自定义角色列表
	 */
	public List<ORGUserGroup> getORGUserGroups(DruidPooledConnection conn, Long orgId, Integer count, Integer offset)
			throws Exception {
		return orgUserGroupRepository.getListByKey(conn, "org_id", orgId, count, offset);
	}

}
