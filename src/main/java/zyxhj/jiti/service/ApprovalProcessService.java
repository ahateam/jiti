package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.SyncClient;

import io.vertx.core.Vertx;
import zyxhj.core.domain.Mail;
import zyxhj.core.service.MailService;
import zyxhj.flow.domain.Process;
import zyxhj.flow.domain.ProcessActivity;
import zyxhj.flow.domain.ProcessDefinition;
import zyxhj.flow.service.FlowService;
import zyxhj.flow.service.ProcessService;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.ORGUserRole;
import zyxhj.jiti.domain.PDConnection;
import zyxhj.jiti.domain.UserExamineRecord;
import zyxhj.jiti.repository.PDConnectionRepository;
import zyxhj.jiti.repository.UserExamineRecordRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.EXP;

public class ApprovalProcessService {

	private static Logger log = LoggerFactory.getLogger(ApprovalProcessService.class);

	private UserExamineRecordRepository userExamineRecordRepository;
	private PDConnectionRepository pdConnectionRepository;
	private FlowService flowService;// 流程定义service
	private ProcessService processService;// 流程定义service
	private ORGUserService orgUserService;
	private static MailService mailService;

	public ApprovalProcessService() {
		try {
			userExamineRecordRepository = Singleton.ins(UserExamineRecordRepository.class);
			pdConnectionRepository = Singleton.ins(PDConnectionRepository.class);
			flowService = Singleton.ins(FlowService.class);
			orgUserService = Singleton.ins(ORGUserService.class);
			mailService = Singleton.ins(MailService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// TODO 审批流程定义部分

	/**
	 * 关联流程定义与组织关系
	 */
	public PDConnection createPDConnection(DruidPooledConnection conn, Long orgId, Long pdId, String remark)
			throws Exception {
		PDConnection PDConn = new PDConnection();
		PDConn.id = IDUtils.getSimpleId();
		PDConn.orgId = orgId;
		PDConn.pdId = pdId;
		PDConn.remark = remark;
		pdConnectionRepository.insert(conn, PDConn);
		return PDConn;
	}

	/**
	 * 取消流程定义与组织关联
	 */
	public int deletePDConnection(DruidPooledConnection conn, Long pdConnId) throws Exception {
		return pdConnectionRepository.delete(conn, EXP.INS().key("id", pdConnId));
	}

	/**
	 * 控制台创建流程定义
	 */
	public void createApprovalProcessDefintion() {
//		flowService.c
	}

	/**
	 * 控制台修改流程定义
	 */
	public void editApprovalProcessDefintion() {

	}

	/**
	 * 控制台删除流程定义
	 */
	public void delApprovalProcessDefintion() {

	}

	// TODO 流程实例部分

	/**
	 * 创建一个新的流程实例
	 */
	public UserExamineRecord createApprovaProcess(DruidPooledConnection conn, Long orgId, Long pdId, Long userId,
			String title, String remark) throws Exception {
		Process process = processService.createProcess(pdId, userId, title, remark);
		return createUserExaminRecord(conn, orgId, userId, process.id, "创建审批流程实例", remark);
	}

	/**
	 * 执行节点跳转操作Action(控制权限)
	 */
	public int executeAction(Long orgId, JSONObject receivers, Long processId, Long activityId, Long actionId,
			Long userId, String type) throws Exception {

		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			DruidDataSource dds;
			DruidPooledConnection conn = null;
			try {
				dds = DataSource.getDruidDataSource("rdsDefault.prop");
				conn = (DruidPooledConnection) dds.getConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				processService.execAction(processId, activityId, actionId, null, userId, type);
				createUserExaminRecord(conn, orgId, userId, processId, type, null);

				Long moduleId = Mail.JITI_MODULEID;
				// 获取当前节点中的接收者权限
				Process pro = processService.getProcessById(processId);
				if (pro != null) {
					ProcessDefinition pd = flowService.getPDById(pro.pdId);
					ProcessActivity pa = flowService.getPDActivityById(pro.pdId, activityId);
					if (pa != null) {
						JSONArray roles = JSON.parseArray(pa.receivers);
						if (roles != null && roles.size() > 0) {
							JSONArray uarray = new JSONArray();
							// 判断当前组织是否有处理节点的权限
							for (int rol = 0; rol < roles.size(); rol++) {
								JSONObject jo = JSON.parseObject(roles.getString(rol));
								String roleType = jo.getString("type");
								// 判断权限类型
								if ("user".equals(roleType)) {
									uarray.add(Long.parseLong(jo.getString("id")));
								} else if ("role".equals("roleType")) {
									// 判断系统默认权限
									Long role = Long.parseLong(jo.getString("id"));
									if (ORGUserRole.SYS_ORG_USER_ROLE_MAP.containsKey(role)) {
										List<ORGUser> userList = orgUserService.getOrgUserByRole(conn, orgId, role);
										for (int i = 0; i < userList.size(); i++) {
											uarray.add(userList.get(i).userId);
										}
									}
								} else {// TODO
										// 其他权限
								}
							}
							// 判断是否有用户
							if (uarray != null && uarray.size() > 0) {
								//发送消息
								mailService.mailSend(moduleId, uarray, pd.tags, userId.toString(), pro.title, "content",
										"操作下一步", "备用字段");
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			future.complete("ok");
		}, res -> {
			System.out.println("The result is: " + res.result());
		});
		return 1;
	}

	/**
	 * 创建用户操作记录
	 */
	public UserExamineRecord createUserExaminRecord(DruidPooledConnection conn, Long orgId, Long userId, Long processId,
			String action, String remark) throws Exception {
		UserExamineRecord uer = new UserExamineRecord();
		uer.Id = IDUtils.getSimpleId();
		uer.orgId = orgId;
		uer.userId = userId;
		uer.processId = processId;
		uer.action = action;
		uer.remark = remark;
		uer.operatingTime = new Date();
		userExamineRecordRepository.insert(conn, uer);
		return uer;
	}

}
