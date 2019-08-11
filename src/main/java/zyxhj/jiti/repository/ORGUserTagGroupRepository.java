package zyxhj.jiti.repository;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserTagGroupRepository extends RDSRepository<ORGUserTagGroup> {

	public ORGUserTagGroupRepository() {
		super(ORGUserTagGroup.class);
	}

	private void tree(Long groupId, List<ORGUserTagGroup> groups, JSONArray array) {

		// System.out.println("---tree>" + groupId);
		for (int i = 0; i < groups.size(); i++) {

			ORGUserTagGroup tg = groups.get(i);

			if (tg.parentId.equals(groupId)) {
				// 顶级元素是当前节点，要添加到node下，然后展开递归，然后从数组中移除当前元素
				JSONObject jo = (JSONObject) JSON.toJSON(tg);// 转换成json再添加

				JSONArray childs = new JSONArray();
				jo.put("childs", childs);

				array.add(jo);
				// System.out.println("---tree>>>>>" + groupId + " --- " + tg.parentId);

				tree(tg.groupId, groups, childs);
			}
		}
	}

	public JSONArray getTagGroupTree(DruidPooledConnection conn, Long orgId, Long groupId) throws ServerException {

		// 找出groupId对应的所有子节点
		List<ORGUserTagGroup> groups = this.getList(conn,
				StringUtils.join("org_id=? AND JSON_CONTAINS(parents, '", groupId, "', '$')"),
				Arrays.asList(orgId ), 512, 0);

		JSONArray ret = new JSONArray();
		if (groups != null && groups.size() > 0) {
			// 将列表转换成树

			tree(groupId, groups, ret);
		}
		// System.out.println(JSON.toJSONString(ret));
		return ret;
	}
}
