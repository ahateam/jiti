package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Family;
import zyxhj.utils.data.rds.RDSRepository;

public class FamilyRepository extends RDSRepository<Family> {

	public FamilyRepository() {
		super(Family.class);
	}

}
