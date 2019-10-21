package zyxhj.jiti.repository;

import zyxhj.jiti.domain.PDConnection;
import zyxhj.utils.data.rds.RDSRepository;

public class PDConnectionRepository extends RDSRepository<PDConnection> {

	public PDConnectionRepository() {
		super(PDConnection.class);
	}
	
}
