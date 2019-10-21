package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.PrintingTemplate;
import zyxhj.jiti.service.PrintingTemplateService;
import zyxhj.jiti.service.PrintingTypeService;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class ExternalController extends Controller {

	private static Logger log = LoggerFactory.getLogger(ExternalController.class);

	private DruidDataSource dds;
	private PrintingTemplateService printingTemplateService;

	public ExternalController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");

			printingTemplateService = Singleton.ins(PrintingTemplateService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@ENUM(des = "模板类型")
	public PrintingTemplate.TYPE[] prTeType = PrintingTemplate.TYPE.values();

	@ENUM(des = "模板页码")
	public PrintingTemplate.PAGE[] prTePage = PrintingTemplate.PAGE.values();

	/**
	 * 
	 */
	@POSTAPI(//
			path = "createPrintingTemplate", //
			des = "创建打印模板", //
			ret = "")
	public APIResponse createPrintingTemplate(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "模板数据") String data, //
			@P(t = "模板类型") Byte type, //
			@P(t = "左右页  1为左  2为右") Byte page //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse
					.getNewSuccessResp(printingTemplateService.createPrintingTemplate(conn, orgId, data, type, page));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editPrintingTemplate", //
			des = "修改打印模板", //
			ret = "")
	public APIResponse editPrintingTemplate(//
			@P(t = "模板编号") Long prTeId, //
			@P(t = "组织编号") Long orgId, //
			@P(t = "模板数据") String data, //
			@P(t = "模板类型") Byte type, //
			@P(t = "左右页  1为左  2为右") Byte page //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(
					printingTemplateService.editPrintingTemplate(conn, prTeId, orgId, data, type, page));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPrintingTemplate", //
			des = "获取打印模板", //
			ret = "返回打印模板")
	public APIResponse getPrintingTemplate(//
			@P(t = "组织编号") Long prTeId, //
			@P(t = "组织编号") Long orgId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(printingTemplateService.getPrintingTemplate(conn, prTeId, orgId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPrintingTemplateByType", //
			des = "根据类型获取打印模板", //
			ret = "返回打印模板")
	public APIResponse getPrintingTemplateByType(//
			@P(t = "组织编号") Long orgId, //
			@P(t = "打印模板类型") Byte type, //
			@P(t = "页码  1左 2右") Byte page //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse
					.getNewSuccessResp(printingTemplateService.getPrintingTemplateByType(conn, orgId, type, page));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPrintingTemplates", //
			des = "获取打印模板", //
			ret = "返回打印模板")
	public APIResponse getPrintingTemplates(//
			@P(t = "组织编号") Long orgId, //
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse
					.getNewSuccessResp(printingTemplateService.getPrintingTemplates(conn, orgId, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "delPrintingTemplate", //
			des = "删除打印模板", //
			ret = "")
	public APIResponse delPrintingTemplate(//
			@P(t = "组织编号") Long prTeId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(printingTemplateService.delPrintingTemplate(conn, prTeId));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getPrintingType", //
			des = "查询打印类型", //
			ret = "返回打印类型"//
	)
	public APIResponse getPrintingType(//
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(PrintingTypeService.data);
		}
	}
	
	
	

}
