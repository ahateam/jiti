package zyxhj.jiti.repository;

import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserRoleRepository extends RDSRepository<ORGUserRole> {

	public ORGUserRoleRepository() {
		super(ORGUserRole.class);
	}



}
