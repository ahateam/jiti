package zyxhj.jiti.repository;

import zyxhj.jiti.domain.Customer;
import zyxhj.utils.data.rds.RDSRepository;

public class CustomerRepository extends RDSRepository<Customer> {

	public CustomerRepository() {
		super(Customer.class);
		// TODO Auto-generated constructor stub
	}

	
}
