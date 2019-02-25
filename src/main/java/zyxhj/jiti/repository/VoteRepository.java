package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Vote;
import zyxhj.utils.data.rds.RDSRepository;

public class VoteRepository extends RDSRepository<Vote> {

	public VoteRepository() {
		super(Vote.class);
	}

}
