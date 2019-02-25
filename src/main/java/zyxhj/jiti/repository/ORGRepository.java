package zyxhj.jiti.repository;

import zyxhj.jiti.domain.ORG;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGRepository extends RDSRepository<ORG> {

	public ORGRepository() {
		super(ORG.class);
	}

}
