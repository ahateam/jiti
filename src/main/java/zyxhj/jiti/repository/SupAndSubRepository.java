package zyxhj.jiti.repository;

import zyxhj.jiti.domain.SupAndSub;
import zyxhj.utils.data.rds.RDSRepository;

public class SupAndSubRepository extends RDSRepository<SupAndSub> {

	public SupAndSubRepository() {
		super(SupAndSub.class);
	}

}
