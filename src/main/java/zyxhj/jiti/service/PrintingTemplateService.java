package zyxhj.jiti.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.PrintingTemplate;
import zyxhj.jiti.repository.PrintingTemplateRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class PrintingTemplateService {

	private static Logger log = LoggerFactory.getLogger(PrintingTemplateService.class);

	private PrintingTemplateRepository printingTemplateRepository;

	public PrintingTemplateService() {
		try {
			printingTemplateRepository = Singleton.ins(PrintingTemplateRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 创建成员股权证模板
	public PrintingTemplate createPrintingTemplate(DruidPooledConnection conn, Long orgId, String data, Byte type,
			Byte page) throws Exception {
		PrintingTemplate printingTemplate = new PrintingTemplate();
		printingTemplate.id = IDUtils.getSimpleId();
		printingTemplate.orgId = orgId;
		printingTemplate.data = data;
		printingTemplate.type = type;
		printingTemplate.page = page;
		printingTemplateRepository.insert(conn, printingTemplate);
		return printingTemplate;
	}

	// 创建成员股权证模板
	public PrintingTemplate editPrintingTemplate(DruidPooledConnection conn, Long prTeId, Long orgId, String data,
			Byte type, Byte page) throws Exception {
		PrintingTemplate printingTemplate = new PrintingTemplate();
		printingTemplate.orgId = orgId;
		printingTemplate.data = data;
		printingTemplate.type = type;
		printingTemplate.page = page;
		printingTemplateRepository.update(conn,EXP.ins().key("id",prTeId).andKey("org_id", orgId), printingTemplate, true);
		
		return printingTemplate;
	}

	// 获取单个模板
	public PrintingTemplate getPrintingTemplate(DruidPooledConnection conn, Long prTeId, Long orgId) throws Exception {
		return printingTemplateRepository.get(conn, EXP.ins().key("id", prTeId).andKey("org_id",orgId ));
		
	}

	// 获取模板列表
	public List<PrintingTemplate> getPrintingTemplates(DruidPooledConnection conn, Long orgId, Integer count,
			Integer offset) throws Exception {
		return printingTemplateRepository.getList(conn,EXP.ins().key( "org_id", orgId), count, offset);
	}

	// 删除模板
	public int delPrintingTemplate(DruidPooledConnection conn, Long prTeId) throws Exception {
		return printingTemplateRepository.delete(conn,EXP.ins().key("id", prTeId));
	}

	// 根据类型获取打印模板
	public PrintingTemplate getPrintingTemplateByType(DruidPooledConnection conn, Long orgId, Byte type, Byte page)
			throws Exception {
		return printingTemplateRepository.get(conn, EXP.ins().key("org_id", orgId).andKey("type", type).andKey("page", page));
	}

}
