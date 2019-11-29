package zyxhj.jiti.repository;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.domain.ORG;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.rds.RDSRepository;

public class ExamineRepository extends RDSRepository<Examine> {

	public ExamineRepository() {
		super(Examine.class);
	}

	public List<Examine> getExamineByORGIds(DruidPooledConnection conn, List<ORG> orgs, Byte type, Byte status,
			Integer count, Integer offset) throws Exception {

		// TODO 添加倒序
		List<Long> args = new ArrayList<Long>();
		for (ORG org : orgs) {
			args.add(org.id);
		}
		EXP sql = EXP.INS().andKey("type", type).andKey("status", status).and(EXP.IN("org_id", args.toArray()))
				.append(" ORDER BY create_date DESC ");
		return getList(conn, sql, count, offset);
	}

	// 用户查看自己的审核
	public List<Examine> getExamineLikeUserId(DruidPooledConnection conn, Long orgId, Long userId, Byte type,
			Integer count, Integer offset) throws Exception {
		return this.getList(conn, StringUtils.join("org_id = ", orgId, " AND type = ", type,
				" AND JSON_EXTRACT(`data`, '$.newData') ", " LIKE \"%", userId, "%\""), null, count, offset);
	}

	public List<Examine> getExamineByName(DruidPooledConnection conn, String sql, Integer count, Integer offset)
			throws Exception {
		this.getList(conn, sql, null, count, offset);
		return null;
	}

	public List<Examine> getExamines(DruidPooledConnection conn) throws Exception {
		String where = "img_org is not null or img_auth is not null";
		return this.getList(conn, where, null, null, null);
	}

}
