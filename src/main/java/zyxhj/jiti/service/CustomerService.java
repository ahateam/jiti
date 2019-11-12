package zyxhj.jiti.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Customer;
import zyxhj.jiti.repository.CustomerRepository;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class CustomerService {

	private static Logger log = LoggerFactory.getLogger(CustomerService.class);

	private CustomerRepository customerRepository;

	public CustomerService() {
		try {
			customerRepository = Singleton.ins(CustomerRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 创建客服
	 */
	public void createCustomer(DruidPooledConnection conn, String name, String phone, String qqNumber, String wxNumber,
			String Email, String remark) throws Exception {
		Customer cus = new Customer();
		cus.name = name;
		cus.phone = phone;
		cus.qqNumber = qqNumber;
		cus.wxNumber = wxNumber;
		cus.Email = Email;
		cus.remark = remark;
		customerRepository.insert(conn, cus);
	}

	/**
	 * 修改客服信息
	 */
	public int editCustomer(DruidPooledConnection conn, Long cusId, String name, String phone, String qqNumber,
			String wxNumber, String Email, String remark) throws Exception {
		Customer cus = new Customer();
		cus.name = name;
		cus.phone = phone;
		cus.qqNumber = qqNumber;
		cus.wxNumber = wxNumber;
		cus.Email = Email;
		cus.remark = remark;
		return customerRepository.update(conn, EXP.INS().key("id", cusId), cus, true);
	}
	/**
	 * 启用或禁用客服
	 */
	public int enableORDisableCustomer(DruidPooledConnection conn, Long cusId,Byte status) throws Exception {
		EXP set = EXP.INS().key("status", status);
		EXP where = EXP.INS().key("id", cusId);
		return customerRepository.update(conn, set, where);
	}
	/**
	 * 查询客服
	 */
	public Customer getCustomer(DruidPooledConnection conn) throws Exception{
		return customerRepository.get(conn, null);
	}

	/**
	 * 删除客服
	 */
	public int deleteCustomer(DruidPooledConnection conn, Long cusId) throws Exception {
		return customerRepository.delete(conn, EXP.INS().key("id", cusId));
	}
}
