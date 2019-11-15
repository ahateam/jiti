package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.service.CustomerService;
import zyxhj.jiti.service.FeedbackService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class CustomerController extends Controller {

	private static Logger log = LoggerFactory.getLogger(CustomerController.class);

	private DruidDataSource dds;
	private CustomerService customerService;
	private FeedbackService feedbackService;

	public CustomerController(String name) {
		super(name);
		try {

			dds = DataSource.getDruidDataSource("rdsDefault.prop");
			customerService = Singleton.ins(CustomerService.class);
			feedbackService = Singleton.ins(FeedbackService.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@POSTAPI(//
			path = "createCustomer", //
			des = "创建客服"//
	)
	public APIResponse createCustomer(//
			@P(t = "客服姓名") String name, //
			@P(t = "手机号") String phone, //
			@P(t = "QQ号") String qqNumber, //
			@P(t = "微信号") String wxNumber, //
			@P(t = "邮箱") String Email, //
			@P(t = "备注") String remark//
	) throws Exception {

		try (DruidPooledConnection conn = dds.getConnection()) {
			customerService.createCustomer(conn, name, phone, qqNumber, wxNumber, Email, remark);
			return APIResponse.getNewSuccessResp();
		}
	}

	@POSTAPI(//
			path = "editCustomer", //
			des = "修改客服信息"//
	)
	public APIResponse editCustomer(//
			@P(t = "客服编号") Long cusId, //
			@P(t = "客服姓名") String name, //
			@P(t = "手机号") String phone, //
			@P(t = "QQ号") String qqNumber, //
			@P(t = "微信号") String wxNumber, //
			@P(t = "邮箱") String Email, //
			@P(t = "备注") String remark//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					customerService.editCustomer(conn, cusId, name, phone, qqNumber, wxNumber, Email, remark));
		}
	}

	@POSTAPI(//
			path = "enableORDisableCustomer", //
			des = "启用或禁用客服"//
	)
	public APIResponse enableORDisableCustomer(//
			@P(t = "客服编号") Long cusId, //
			@P(t = "启用或禁用") Byte status//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(customerService.enableORDisableCustomer(conn, cusId, status));
		}
	}

	@POSTAPI(//
			path = "deleteCustomer", //
			des = "删除客服"//
	)
	public APIResponse deleteCustomer(//
			@P(t = "客服编号") Long cusId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(customerService.deleteCustomer(conn, cusId));
		}
	}

	@POSTAPI(//
			path = "getCustomer", //
			des = "获取客服列表"//
	)
	public APIResponse getCustomer() throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(customerService.getCustomer(conn));
		}
	}

	@POSTAPI(//
			path = "getCustomerByName", //
			des = "获取客服列表"//
	)
	public APIResponse getCustomerByName(//
			@P(t = "客户姓名", r = false) String name, //
			Integer count, //
			Integer offset//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(customerService.getCustomerByName(conn, name, count, offset));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// feedback反馈
	@POSTAPI(//
			path = "createFeedback", //
			des = "创建反馈信息"//
	)
	public APIResponse createFeedback(//
			@P(t = "反馈者编号") Long userId, //
			@P(t = "反馈者内容") String feedbackContent, //
			@P(t = "手机号") String phone//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			feedbackService.createFeedback(conn, userId, feedbackContent, phone);
			return APIResponse.getNewSuccessResp();
		}
	}

	@POSTAPI(//
			path = "getFeedbackList", //
			des = "获取反馈信息列表"//
	)
	public APIResponse getFeedbackList(//
			Integer count, //
			Integer offset//
	) {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(feedbackService.getFeedbackList(conn, count, offset));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@POSTAPI(//
			path = "getFeedbackByfbId", //
			des = "获取反馈信息"//
	)
	public APIResponse getFeedbackByfbId(//
			@P(t = "反馈id") Long fbId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(feedbackService.getFeedback(conn, fbId));
		}
	}

	@POSTAPI(//
			path = "deleteFeedback", //
			des = "获取反馈信息"//
	)
	public APIResponse deleteFeedback(//
			@P(t = "反馈id") Long fbId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(feedbackService.deleteFeedback(conn, fbId));
		}
	}

}
