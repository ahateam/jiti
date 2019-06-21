package zyxhj.jiti.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Examine;
import zyxhj.jiti.domain.ORG;
import zyxhj.utils.data.rds.RDSRepository;
import zyxhj.utils.data.rds.SQL;

public class ExamineRepository extends RDSRepository<Examine> {

	public ExamineRepository() {
		super(Examine.class);
	}

	public List<Examine> getExamineByORGIds(DruidPooledConnection conn, List<ORG> orgs, Byte type, Byte status,
			Integer count, Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer("WHERE ");
		SQL sql = new SQL();
		SQL sqlEx = new SQL();
		for (ORG org : orgs) {
			sqlEx.OR(StringUtils.join("org_id = ", org.id));
		}
		sql.AND(sqlEx);
		sql.AND("type = ? ", type);
		sql.AND("status = ?", status);
		sql.fillSQL(sb);
		return getList(conn, sb.toString(), sql.getParams(), count, offset);
	}

	public List<Examine> getExamineLikeUserId(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {
		StringBuffer sb = new StringBuffer("WHERE org_id = ").append(orgId)
				.append(" AND JSON_EXTRACT(`data`, '$.newData') ").append(StringUtils.join(" LIKE \"%", userId, "%\""));

		return this.getList(conn, sb.toString(), new Object[] {}, count, offset);
	}

}
