package zyxhj.jiti.repository;

import zyxhj.jiti.domain.ORGExamine;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGExamineRepository extends RDSRepository<ORGExamine> {

	public ORGExamineRepository() {
		super(ORGExamine.class);
	}

}
