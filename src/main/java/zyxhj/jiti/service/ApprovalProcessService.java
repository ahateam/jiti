package zyxhj.jiti.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.PDConnection;
import zyxhj.jiti.domain.UserExamineRecord;
import zyxhj.jiti.repository.PDConnectionRepository;
import zyxhj.jiti.repository.UserExamineRecordRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class ApprovalProcessService {

	private static Logger log = LoggerFactory.getLogger(ApprovalProcessService.class);

	private UserExamineRecordRepository userExamineRecordRepository;
	private PDConnectionRepository pdConnectionRepository;

	public ApprovalProcessService() {
		try {
			userExamineRecordRepository = Singleton.ins(UserExamineRecordRepository.class);
			pdConnectionRepository = Singleton.ins(PDConnectionRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

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
	 * 创建用户操作记录
	 */
	public void createUserExaminRecord(DruidPooledConnection conn, Long orgId, Long userId, Long processId,
			String action,String remark) throws Exception {
		UserExamineRecord uer = new UserExamineRecord();
		uer.Id = IDUtils.getSimpleId();
		uer.orgId = orgId;
		uer.userId = userId;
		uer.processId = processId;
		uer.action = action;
		uer.remark = remark;
		uer.operatingTime = new Date();
		userExamineRecordRepository.insert(conn, uer);
	}

}
