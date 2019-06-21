package zyxhj.movie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.utils.ServiceUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;
import zyxhj.utils.data.DataSource;

public class VideoController extends Controller {

	private static Logger log = LoggerFactory.getLogger(VideoController.class);

	private DruidDataSource dds;

	private VideoService videoService;

	public VideoController(String node) {
		super(node);
		try {
			dds = DataSource.getDruidDataSource("rdsDefault.prop");
			videoService = Singleton.ins(VideoService.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "addVideo", //
			des = "添加视频", //
			ret = "返回添加信息"//
	)
	public APIResponse addVideo(//
			@P(t = "标题") String title, //
			@P(t = "类型") String type, //
			@P(t = "图片地址") String imageUrl, //
			@P(t = "地址类型") Byte urlType, //
			@P(t = "视频地址") String videoUrl //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(
					ServiceUtils.checkNull(videoService.addVideo(conn, title, type, imageUrl, urlType, videoUrl)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "editVideo", //
			des = "修改", //
			ret = "返回修改信息"//
	)
	public APIResponse editVideo(//
			@P(t = "id") Long videoId, //
			@P(t = "标题") String title, //
			@P(t = "类型") String type, //
			@P(t = "图片地址") String imageUrl, //
			@P(t = "地址类型") Byte urlType, //
			@P(t = "视频地址") String videoUrl //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(ServiceUtils
					.checkNull(videoService.editVideo(conn, videoId, title, type, imageUrl, urlType, videoUrl)));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "deleteVideo", //
			des = "删除" //
	)
	public APIResponse deleteVideo(//
			@P(t = "id") Long videoId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			videoService.deleteVideo(conn, videoId);
			return APIResponse.getNewSuccessResp();
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVideo", //
			des = "获取所有", //
			ret = "返回获取信息"//
	)
	public APIResponse getVideo(//
			Integer count, //
			Integer offset //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {
			return APIResponse.getNewSuccessResp(videoService.getVideo(conn, count, offset));
		}
	}

	/**
	 * 
	 */
	@POSTAPI(//
			path = "getVideById", //
			des = "根据id获取", //
			ret = "返回获取信息"//
	)
	public APIResponse getVideById(//
			@P(t = "id") Long videoId //
	) throws Exception {
		try (DruidPooledConnection conn = dds.getConnection()) {

			return APIResponse.getNewSuccessResp(videoService.getVideById(conn, videoId));
		}
	}
}
