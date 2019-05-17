package zyxhj.jiti.repository;

import zyxhj.jiti.domain.NoticeTask;
import zyxhj.utils.data.rds.RDSRepository;

public class NoticeTaskRepository extends RDSRepository<NoticeTask> {

	public NoticeTaskRepository() {
		super(NoticeTask.class);
	}



}
