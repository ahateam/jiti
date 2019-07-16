package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Advert;
import zyxhj.utils.data.rds.RDSRepository;

public class AdvertRepository extends RDSRepository<Advert> {

	public AdvertRepository() {
		super(Advert.class);
	}

}
