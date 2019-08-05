package zyxhj.jiti.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.cms.domian.ContentTag;
import zyxhj.jiti.domain.ORGUserTag;
import zyxhj.jiti.repository.ORGUserTagRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

/**
 * 第三方用户自定义角色service
 *
 */
public class ORGUserTagService {

	private static Logger log = LoggerFactory.getLogger(ORGUserTagService.class);

	private static Cache<Long, ORGUserTag> ORG_USER_TAG_CACHE = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(1000)//
			.build();

	private ORGUserTagRepository tagRepository;

	public ORGUserTagService() {
		try {
			tagRepository = Singleton.ins(ORGUserTagRepository.class);
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

		return tagRepository.update(conn,EXP.ins().key("org_id",orgId).andKey("user_id", tagId),
				renew, true);
		
	}

	/**
	 * 根据状态获取标签列表
	 */
	public List<ORGUserTag> getTags(DruidPooledConnection conn, Long orgId, Byte status, String groupKeyword,
			Integer count, Integer offset) throws Exception {
		return tagRepository.getList(conn, EXP.ins().key("org_id", orgId).andKey("status", status).andKey("group_keyword", groupKeyword), count, offset);

	}

	/**
	 * 根据编号获取自定义标签
	 */
	public ORGUserTag getTagById(DruidPooledConnection conn, Long orgId, Long tagId) throws Exception {
		// 先从系统缓存里取，再从缓存去，最后再查
		ORGUserTag tag = ORG_USER_TAG_CACHE.getIfPresent(tagId);
		if (tag == null) {
			// 从数据库中获取
			tag = tagRepository.get(conn, EXP.ins().key("org_id", orgId).andKey("tag_id",tagId));
			if (tag != null) {
				// 放入缓存
				ORG_USER_TAG_CACHE.put(tagId, tag);
			}
		}
		return tag;
	}

}
