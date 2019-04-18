package zyxhj.jiti.repository;

import zyxhj.jiti.domain.ORGDistrict;
import zyxhj.utils.data.rds.RDSRepository;

public class ORGDistrictRepository extends RDSRepository<ORGDistrict> {

	public ORGDistrictRepository() {
		super(ORGDistrict.class);
	}

}
