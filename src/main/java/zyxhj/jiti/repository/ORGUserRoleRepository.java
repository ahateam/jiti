package zyxhj.jiti.repository;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserRoleRepository extends RDSRepository<ORGUserRole> {

	public ORGUserRoleRepository() {
		super(ORGUserRole.class);
	}



}
