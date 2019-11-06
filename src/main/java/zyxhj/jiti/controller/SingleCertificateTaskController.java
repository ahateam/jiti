package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.service.SingleCertificateTaskService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class SingleCertificateTaskController extends Controller {

	private static Logger log = LoggerFactory.getLogger(SingleCertificateTaskController.class);

	private DruidDataSource dds;
	private SingleCertificateTaskService scftService;
	
	public SingleCertificateTaskController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

			scftService = Singleton.ins(SingleCertificateTaskService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 *	单证书打印接口 
	 */
	@POSTAPI(//
			path = "getORGList",//
			des = "获取所有组织",//
			ret = "组织列表"
			)
	public APIResponse getORGList(//
			@P(t = "组织名称", r = false)String orgName,//
			Integer count,//
			Integer offset//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.getORGList(conn, orgName, count,offset));
		}
	}
	
	@POSTAPI(//
			path = "getFamilyMasterList",//
			des = "获取户主列表",//
			ret = "户主列表List<ORGUser>"
			)
	public APIResponse getFamilyMasterList(//
			@P(t = "组织编号")Long orgId,//
			@P(t = "户主姓名",r = false) String familyMaster,//
			Integer count,//
			Integer offset//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.getFamilyMasterList(conn, orgId, familyMaster, count,offset));
		}
	}
	

	@POSTAPI(//
			path = "getFamilyInfo",//
			des = "获取当前户所有成员信息",//
			ret = "组织列表"
			)
	public APIResponse getFamilyInfo(//
			@P(t = "组织编号")Long orgId,//
			@P(t = "户序号") Long familyNumber//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.getFamilyInfo(conn, orgId, familyNumber));
		}
	}
	
	@POSTAPI(//
			path = "getFamilyInfoArray",//
			des = "通过户序号区间查询户所有成员信息",//
			ret = "户成员信息列表"
			)
	public APIResponse getFamilyInfoArray(//
			@P(t = "组织编号")Long orgId,//
			@P(t = "起始户序号") Long startFamilyNumber,//
			@P(t = "终止户序号") Long endFamilyNumber,//
			Integer count,//
			Integer offset//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.getFamilyInfoArray(conn, orgId, startFamilyNumber,endFamilyNumber,count,offset));
		}
	}
	
	@POSTAPI(//
			path = "createSCFT",//
			des = "创建证书打印任务"//
			)
	public APIResponse createSCFT(//
			@P(t = "组织编号")Long orgId,//
			@P(t = "操作用户编号") Long userId//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.createSCFT(conn, orgId,userId));
		}
	}
	

	@POSTAPI(//
			path = "editSCFT",//
			des = "修改证书打印任务",//
			ret = "受影响行数"
			)
	public APIResponse editSCFT(//
			@P(t = "任务编号")Long taskId,//
			@P(t = "文件下载地址", r = false) String fileUrl,//
			@P(t = "打印总数") Integer totalNumber//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.editSCFT(conn, taskId,fileUrl,totalNumber));
		}
	}
	
	@POSTAPI(//
			path = "getSCFTByUserId",//
			des = "获取证书打印任务列表（支持标题模糊查询）",//
			ret = "List<SingleCertificateTask>"
			)
	public APIResponse getSCFTByUserId(//
			@P(t = "用户编号")Long userId,//
			@P(t = "标题", r = false) String title,//
			Integer count,
			Integer offset
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.getSCFTByUserId(conn, userId,title,count,offset));
		}
	}
	
	@POSTAPI(//
			path = "getFamilyInfoByCodeANDFamilyNumber",//
			des = "通过组织机构代码和户序号查询户信息",//
			ret = "户成员信息列表"
			)
	public APIResponse getFamilyInfoByCodeANDFamilyNumber(//
			@P(t = "户序号")Long familyNumber,//
			@P(t = "组织机构代码") String code//
			) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(scftService.getFamilyInfoByCodeANDFamilyNumber(conn, familyNumber,code));
		}
	}
	
	
	
}
