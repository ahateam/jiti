package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Feedback;
import zyxhj.utils.data.rds.RDSRepository;

public class FeedbackRepository extends RDSRepository<Feedback> {

	public FeedbackRepository() {
		super(Feedback.class);
	}

}
