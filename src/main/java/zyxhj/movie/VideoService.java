package zyxhj.movie;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;

public class VideoService {

	private static Logger log = LoggerFactory.getLogger(VideoService.class);

	private VideoRepository videoRepository;

	public VideoService() {
		try {

			videoRepository = Singleton.ins(VideoRepository.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public Video addVideo(DruidPooledConnection conn, String title, String type, String imageUrl, Byte urlType,
			String videoUrl) throws Exception {
		Video vi = new Video();
		vi.id = IDUtils.getSimpleId();
		vi.title = title;
		vi.type = type;
		vi.imageUrl = imageUrl;
		vi.urlType = urlType;
		vi.videoUrl = videoUrl;
		videoRepository.insert(conn, vi);
		return vi;
	}

	public void deleteVideo(DruidPooledConnection conn, Long videoId) throws Exception {
		videoRepository.deleteByKey(conn, "id", videoId);
	}

	public Video editVideo(DruidPooledConnection conn, Long videoId, String title, String type, String imageUrl,
			Byte urlType, String videoUrl) throws Exception {
		Video vi = new Video();
		vi.id = IDUtils.getSimpleId();
		vi.title = title;
		vi.type = type;
		vi.imageUrl = imageUrl;
		vi.urlType = urlType;
		vi.videoUrl = videoUrl;
		videoRepository.updateByKey(conn, "id", videoId, vi, true);
		return vi;
	}

	public List<Video> getVideo(DruidPooledConnection conn, Integer count, Integer offset) throws Exception {
		return videoRepository.getList(conn, count, offset);
	}

	public Video getVideById(DruidPooledConnection conn, Long videoId) throws Exception {
		return videoRepository.getByKey(conn, "id", videoId);
	}

}
