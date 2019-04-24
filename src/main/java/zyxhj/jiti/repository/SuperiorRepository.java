package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Superior;
import zyxhj.utils.data.rds.RDSRepository;

public class SuperiorRepository extends RDSRepository<Superior> {

	public SuperiorRepository() {
		super(Superior.class);
	}

}
