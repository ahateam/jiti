package zyxhj.jiti.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import zyxhj.utils.data.rds.RDSAnnEntity;
import zyxhj.utils.data.rds.RDSAnnField;
import zyxhj.utils.data.rds.RDSAnnID;

/**
 * 组织角色
 *
 */
@RDSAnnEntity(alias = "tb_ecm_org_user_tag_group")
public class ORGUserTagGroup {

	private static ORGUserTagGroup buildSysTagGroup(Long groupId, Long parentId, JSONArray parents, String keyword,
			String remark) {
		ORGUserTagGroup ret = new ORGUserTagGroup();
		ret.orgId = 100L;// ORG组织间公用的系统权限，跨组织存在，默认orgId填写100
		ret.parentId = parentId;
		ret.groupId = groupId;
		if (parents == null || parents.size() <= 0) {
			ret.parents = "[]";
		} else {
			ret.parents = JSON.toJSONString(parents);
		}
		ret.keyword = keyword;
		ret.remark = remark;

		return ret;
	}

	private static Long temp = 100L;// 自增编号

	public static final ORGUserTagGroup group_groups;
	public static final ORGUserTagGroup group_undefine;
	public static final ORGUserTagGroup group_tags;

	static {
		group_groups = buildSysTagGroup(temp++, 0L, null, "groups", null);
		group_tags = buildSysTagGroup(temp++, 0L, null, "tags", null);

		// 添加未分类标签分组
		JSONArray arr = new JSONArray();
		arr.add(group_groups.groupId);
		group_undefine = buildSysTagGroup(temp++, group_groups.groupId, arr, "undefine", null);

	}

	/**
	 * 组织编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long orgId;

	/**
	 * 分组编号
	 */
	@RDSAnnID
	@RDSAnnField(column = RDSAnnField.ID)
	public Long groupId;

	@RDSAnnField(column = RDSAnnField.ID)
	public Long parentId;

	/**
	 * 父节点编号JSONArray数组
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String parents;

	/**
	 * 分组名称
	 */
	@RDSAnnField(column = RDSAnnField.TEXT_NAME)
	public String keyword;

	/**
	 * 备注
	 */
	@RDSAnnField(column = RDSAnnField.SHORT_TEXT)
	public String remark;
}
