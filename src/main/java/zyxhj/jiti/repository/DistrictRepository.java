package zyxhj.jiti.repository;

import zyxhj.jiti.domain.District;
import zyxhj.utils.data.rds.RDSRepository;

public class DistrictRepository extends RDSRepository<District> {

	public DistrictRepository() {
		super(District.class);
	}


}
