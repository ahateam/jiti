package zyxhj.jiti.repository;

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
		EXP sql = EXP.INS();
		EXP sqlEx = EXP.INS();
		for (ORG org : orgs) {
			sqlEx.or(EXP.INS().key("org_id", org.id));
		}
		sql.exp(sqlEx).andKey("type", type).andKey("status", status);

		return getList(conn, sql, count, offset);
	}

	// 用户查看自己的审核
	public List<Examine> getExamineLikeUserId(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {
		return this.getList(conn, StringUtils.join("org_id = ", orgId, " AND JSON_EXTRACT(`data`, '$.newData') ",
				" LIKE \"%", userId, "%\""), null, count, offset);
	}

}
