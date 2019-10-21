package jiti;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.jiti.controller.ORGController;
import zyxhj.jiti.service.ExamineService;
import zyxhj.jiti.service.ExportTaskService;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;

public class TestExportTaskService {
	private static DruidPooledConnection conn;
	
	private static ExportTaskService taskService;

	private static ExamineService examineService;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			conn = DataSource.getDruidDataSource("rdsDefault.prop").getConnection();
			taskService = Singleton.ins(ExportTaskService.class);
			examineService = Singleton.ins(ExamineService.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		conn.close();
	}
	
	private static Long orgId = 397652553337218L;//松林合作社
	private static Long userId = 123L;
	private static Long taskId = 401765457105356L;
	
	@Test
	public void testCreateExportTask() throws Exception {
		String title = "测试导出数据任务";
		taskService.createExportTask(conn, title, orgId, userId, 397718468923094L);
		
	}
	
	@Test
	public void testGetTaskList() throws Exception {
		taskService.getExportTaskList(conn, 397718468923094L, 10,0);
	}

	@Test
	public void testExportDataIntoOSS() {
		try {
			taskService.exportData(399908727332361L, 401788152039380L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			taskService.exportData(399908727332361L, 401788152039380L);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testUploadFile() {
		JSONArray fileUrls = new JSONArray();
		fileUrls.add("C:\\Users\\Admin\\Desktop\\集体经济账号.txt");
		fileUrls.add("C:\\Users\\Admin\\Desktop\\样表.docx");
		System.out.println(fileUrls.toJSONString());
		examineService.uploadFile(fileUrls);
		
	}
}
