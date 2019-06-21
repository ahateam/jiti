package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Message;
import zyxhj.utils.data.rds.RDSRepository;

public class MessageRepository extends RDSRepository<Message> {

	public MessageRepository() {
		super(Message.class);
	}

	
}
