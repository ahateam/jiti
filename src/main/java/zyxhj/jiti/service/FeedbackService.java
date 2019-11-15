package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Feedback;
import zyxhj.jiti.repository.FeedbackRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class FeedbackService {
	private static Logger log = LoggerFactory.getLogger(FeedbackService.class);

	private FeedbackRepository feedbackRepository;

	public FeedbackService() {
		try {
			feedbackRepository = Singleton.ins(FeedbackRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void createFeedback(DruidPooledConnection conn, Long userId, String feedbackContent,String phone) throws Exception { 
		Feedback f = new Feedback();
		f.id = IDUtils.getSimpleId();
		f.fbUserId = userId;
		f.feedbackContent = feedbackContent;
		f.phone = phone;
		f.feedbackTime = new Date();
		feedbackRepository.insert(conn, f);
	}

	public List<Feedback> getFeedbackList(DruidPooledConnection conn, Integer count, Integer offset) throws Exception {
		return feedbackRepository.getList(conn, null, count, offset);
	}
	
	public Feedback getFeedback(DruidPooledConnection conn,Long fbId) throws Exception {
		return feedbackRepository.get(conn, EXP.INS().key("id", fbId));
	}
	
	public int deleteFeedback(DruidPooledConnection conn,Long fbId) throws Exception {
		return feedbackRepository.delete(conn, EXP.INS().key("id", fbId));
	}


}
