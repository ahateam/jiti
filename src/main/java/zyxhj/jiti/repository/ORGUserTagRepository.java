package zyxhj.jiti.repository;

import zyxhj.jiti.domain.ORGUserTag;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGUserTagRepository extends RDSRepository<ORGUserTag> {

	public ORGUserTagRepository() {
		super(ORGUserTag.class);
	}

}
