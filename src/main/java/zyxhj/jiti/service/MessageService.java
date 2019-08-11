package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.Message;
import zyxhj.jiti.repository.MessageRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class MessageService {

	private static Logger log = LoggerFactory.getLogger(MessageService.class);

	private MessageRepository messageRepository;

	public MessageService() {
		try {
			messageRepository = Singleton.ins(MessageRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 查询需要发送的人/角色/分组/组织 现有接口

	// 创建审核消息通知
	public void createExamineMessage(DruidPooledConnection conn, Long orgId, Long userId, String title, String data,
			Byte examineStatus) throws Exception {
		// 根据需要发送的角色/分组/组织查询出对应的user

		// 获取到通知内容 根据userId 将通知信息一条一条插入到message表
		Message message = new Message();
		message.id = IDUtils.getSimpleId();
		message.orgId = orgId;
		message.userId = userId;
		message.title = title;
		message.content = data;
		message.createTime = new Date();
		message.examineStatus = examineStatus;
		message.type = Message.TYPE.EXAMINE.v();
		message.status = Message.STATUS.UNREAD.v();
		messageRepository.insert(conn, message);
	}

	// 批量创建审核消息通知
	public void createExamineMessages(DruidPooledConnection conn, JSONArray oldDatas, String title, String data,
			Byte examineStatus) throws Exception {
		// 根据需要发送的角色/分组/组织查询出对应的user
		List<Message> messages = new ArrayList<Message>();

		for (int i = 0; i < oldDatas.size(); i++) {
			JSONObject userInfo = oldDatas.getJSONObject(i);
			Long userId = userInfo.getLong("userId");
			Long orgId = userInfo.getLong("orgId");

			Message message = new Message();
			message.id = IDUtils.getSimpleId();
			message.orgId = orgId;
			message.userId = userId;
			message.title = title;
			message.content = data;
			message.createTime = new Date();
			message.examineStatus = examineStatus;
			message.type = Message.TYPE.EXAMINE.v();
			message.status = Message.STATUS.UNREAD.v();
			messages.add(message);
		}

		// 获取到通知内容 根据userId 将通知信息一条一条插入到message表

		messageRepository.insertList(conn, messages);
	}

	// 创建投票消息通知
	public void createVoteMessage(DruidPooledConnection conn, Long orgId, JSONArray openIds, String title, String data)
			throws Exception {
		// 根据需要发送的角色/分组/组织查询出对应的user
		List<Message> messages = new ArrayList<Message>();
		for (int j = 0; j < openIds.size(); j++) {
			JSONObject userInfo = openIds.getJSONObject(j);
			// 获取到通知内容 根据userId 将通知信息一条一条插入到message表
			Message message = new Message();
			message.id = IDUtils.getSimpleId();
			message.userId = userInfo.getLong("id");
			message.orgId = orgId;
			message.title = title;
			message.content = data;
			message.createTime = new Date();
			message.type = Message.TYPE.VOTE.v();
			message.status = Message.STATUS.UNREAD.v();
			messages.add(message);
		}

		messageRepository.insertList(conn, messages);
	}

	// 统计该用户有多少条消息没有看
	public Integer countMessageByUserId(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
//		List<Message> me = messageRepository.getListByANDKeys(conn, new String[] { "org_id", "user_id" },
//				new Object[] { orgId, userId }, 512, 0);
		List<Message> me = messageRepository.getList(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId), 512, 0);
		return me.size();

	}

	// 获取用户的消息通知
	public List<Message> getMessageByUserId(DruidPooledConnection conn, Long orgId, Long userId, Integer count,
			Integer offset) throws Exception {
		return messageRepository.getList(conn, EXP.INS().key("org_id", orgId).andKey("user_id", userId), count, offset);
	}

	// 修改消息状态为已读
	public void editMessageStatus(DruidPooledConnection conn, Long messageId) throws Exception {
		Message message = new Message();
		message.status = Message.STATUS.READ.v();
		messageRepository.update(conn,EXP.INS().key("id", messageId), message, true);

	}

}
