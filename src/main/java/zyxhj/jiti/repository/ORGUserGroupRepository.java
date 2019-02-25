package zyxhj.jiti.repository;

import zyxhj.jiti.domain.ORGUserGroup;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserGroupRepository extends RDSRepository<ORGUserGroup> {

	public ORGUserGroupRepository() {
		super(ORGUserGroup.class);
	}

}
