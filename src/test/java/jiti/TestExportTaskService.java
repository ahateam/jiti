package jiti;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.service.ExportTaskService;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;

public class TestExportTaskService {
	private static DruidPooledConnection conn;
	
	private static ExportTaskService taskService;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		try {
			conn = DataSource.getDruidDataSource("rdsDefault.prop").getConnection();
			taskService = Singleton.ins(ExportTaskService.class);
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
		taskService.createExportTask(conn, title, orgId, userId);
		
	}

	@Test
	public void testExportDataIntoOSS() {
		try {
			taskService.ExportDataIntoOSS(conn, orgId, taskId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAddALL() {
		List<String> a = new ArrayList<String>();
		List<String> b = new ArrayList<String>();
		a.add("1");
		a.add("2");
		a.add("3");
		a.add("4");
		b.add("5");
		b.add("6");
		b.add("7");
		b.addAll(a);
		for(String s : b) {
			System.out.println(s);
		}
		
		
	}
}
