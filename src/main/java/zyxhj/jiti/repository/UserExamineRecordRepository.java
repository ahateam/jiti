package zyxhj.jiti.repository;

import zyxhj.jiti.domain.UserExamineRecord;
import zyxhj.utils.data.rds.RDSRepository;

public class UserExamineRecordRepository extends RDSRepository<UserExamineRecord> {

	public UserExamineRecordRepository() {
		super(UserExamineRecord.class);
	}
	
}
