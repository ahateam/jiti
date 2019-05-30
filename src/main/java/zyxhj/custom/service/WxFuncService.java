package zyxhj.custom.service;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import io.vertx.core.Vertx;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.WxMpMassOpenIdsMessage;
import me.chanjar.weixin.mp.bean.WxMpMassTagMessage;
import me.chanjar.weixin.mp.bean.card.WxMpCardQrcodeCreateResult;
import me.chanjar.weixin.mp.bean.result.WxMpMassSendResult;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import zyxhj.jiti.domain.NoticeTask;
import zyxhj.jiti.domain.NoticeTaskRecord;
import zyxhj.jiti.repository.NoticeTaskRecordRepository;
import zyxhj.jiti.repository.NoticeTaskRepository;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.DataSource;
import zyxhj.utils.data.DataSourceUtils;

public class WxFuncService {

	private static Logger log = LoggerFactory.getLogger(WxFuncService.class);

	private NoticeTaskRecordRepository noticeTaskRecordRepository;
	private NoticeTaskRepository noticeTaskRepository;

	public WxFuncService() {
		try {
			noticeTaskRecordRepository = Singleton.ins(NoticeTaskRecordRepository.class);
			noticeTaskRepository = Singleton.ins(NoticeTaskRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/*
	 * 获取用户列表（test）
	 */
	public WxMpUserList getTest(WxMpService wxMpService) throws Exception {
		WxMpUserList wxUserList = wxMpService.getUserService().userList(null);
		return wxUserList;
	}

	/*
	 * 获取卡券列表
	 */
	public Map<String, Object> getTest2(WxMpService wxMpService) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> list = new ArrayList<String>();
		list.add("CARD_STATUS_VERIFY_OK");
		map.put("offset", 0);
		map.put("count", 10);
		map.put("status_list", list);
		String json = JSONObject.toJSONString(map);
		String reJson = post("https://api.weixin.qq.com/card/batchget?access_token=" + wxMpService.getAccessToken(),
				json);
		Map<String, Object> json2Map = parseJSON2Map(reJson);
		return json2Map;
	}

	/*
	 * 获取卡卷二维码
	 * 
	 * cardId 卡卷ID outerStr 场景值 expiresIn 失效时间，单位秒，不填默认365天
	 * 
	 */
	public WxMpCardQrcodeCreateResult getTest3(WxMpService wxMpService, String cardId, String outerStr, int expiresIn)
			throws WxErrorException {
		WxMpCardQrcodeCreateResult card = wxMpService.getCardService().createQrcodeCard(cardId, outerStr, expiresIn);
		return card;
	}

	/*
	 * 获取卡卷详情
	 * 
	 * cardId 卡卷ID
	 */
	public String getTest4(WxMpService wxMpService, String cardId) throws WxErrorException {
		String detail = wxMpService.getCardService().getCardDetail(cardId);
		return detail;
	}

	/*
	 * 根据用户反馈授权获取对应信息
	 */
	public WxMpUser getUserMessage(WxMpService wxMpService, String code) throws Exception {
		WxMpOAuth2AccessToken wxMpOAuth2AccessToken = wxMpService.oauth2getAccessToken(code);
		WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(wxMpOAuth2AccessToken, null);
		return wxMpUser;
	}

	/*
	 * 根据标签群发消息
	 */
	public WxMpMassSendResult messageToTags(WxMpService wxMpService, Long TagId) throws WxErrorException {
		WxMpMassTagMessage massMessage = new WxMpMassTagMessage();
		massMessage.setMsgType(WxConsts.MassMsgType.TEXT); // 设置消息类型
		massMessage.setContent("test messageToTags");
		massMessage.setTagId(TagId);
		WxMpMassSendResult massResult = wxMpService.getMassMessageService().massGroupMessageSend(massMessage);
		return massResult;
	}

	/*
	 * 消息群发
	 */
	public WxMpMassSendResult messageToMany(WxMpService wxMpService, List<String> openIds) throws WxErrorException {
		WxMpMassOpenIdsMessage massMessage = new WxMpMassOpenIdsMessage();
		massMessage.setMsgType(WxConsts.MassMsgType.TEXT);
		massMessage.setContent("test test test test");
		for (String openId : openIds) {
			massMessage.getToUsers().add(openId);
		}
		WxMpMassSendResult massResult = wxMpService.getMassMessageService().massOpenIdsMessageSend(massMessage);
		return massResult;
	}

	/*
	 * 模板消息测试
	 */
	public void templateMessageTest(WxMpService wxMpService, Long taskId, Long orgId) throws WxErrorException {

		// 异步方法，不会阻塞
		Vertx.vertx().executeBlocking(future -> {
			// 下面这行代码可能花费很长时间
			DataSource dsRds;
			DruidPooledConnection conn = null;
			try {
				dsRds = DataSourceUtils.getDataSource("rdsDefault");
				conn = (DruidPooledConnection) dsRds.openConnection();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				NoticeTaskRecord noticeTaskRec = new NoticeTaskRecord();
				NoticeTask notice = noticeTaskRepository.getByKey(conn, "id", taskId);
				for (int i = 0; i < (notice.sum / 100) + 1; i++) {
					List<NoticeTaskRecord> noticeRe = noticeTaskRecordRepository.getListByANDKeys(conn,
							new String[] { "task_id", "org_id", "status" },
							new Object[] { taskId, orgId, NoticeTaskRecord.STATUS.UNDETECTED.v() }, 100, 0);

					for (NoticeTaskRecord noticeTaskRecord : noticeRe) {
						WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
								.toUser(noticeTaskRecord.openId) // oppenid
								.templateId("nQ0-qyYKcvcwLeBN2_cwkj6yJjC2xgGA0lQr_4odvZE")
								.url("http://aha-element.oss-cn-hangzhou.aliyuncs.com/index.html").build();

						templateMessage.addData(new WxMpTemplateData("first", "Let us test this!!", "blue"));
						templateMessage.addData(new WxMpTemplateData("goods_name", "goods", "blue"));
						templateMessage.addData(new WxMpTemplateData("service_content", "牛逼", "blue"));
						templateMessage.addData(new WxMpTemplateData("fee_money", "100", "blue"));
						templateMessage.addData(new WxMpTemplateData("cost_standard", "200", "blue"));
						templateMessage.addData(new WxMpTemplateData("remark", "xxxxx", "blue"));
						try {
							wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
							noticeTaskRec.status = NoticeTaskRecord.STATUS.SUCCESS.v();
							noticeTaskRecordRepository.updateByKey(conn, "task_id", noticeTaskRecord.taskId,
									noticeTaskRec, true);
						} catch (Exception e) {
							noticeTaskRec.status = NoticeTaskRecord.STATUS.FAILURE.v();
							noticeTaskRecordRepository.updateByKey(conn, "task_id", noticeTaskRecord.taskId,
									noticeTaskRec, true);
						}
					}
				}
			} catch (Exception eee) {
				eee.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			future.complete("ok");
		}, res -> {
			System.out.println("The result is: " + res.result());
		});

	}

	/*
	 * 网页授权域名获取
	 */
	public String getUrl(WxMpService wxMpService, String url) {
		System.err.println("test：get url");
		String url2 = wxMpService.oauth2buildAuthorizationUrl(url, WxConsts.OAuth2Scope.SNSAPI_USERINFO, null);
		// ret(resp, url2);
		return url2;
	}

	/*
	 * 获取二维码
	 */
	public File getTicket(WxMpService wxMpService, String scene) throws WxErrorException {
		WxMpQrCodeTicket ticket = wxMpService.getQrcodeService().qrCodeCreateLastTicket(scene);
		File file = wxMpService.getQrcodeService().qrCodePicture(ticket);
		return file;
	}

	/**
	 * @描述 get方式获取外部接口返回json串
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String get(String url) throws IOException {
		String returnVal = "";
		// 定义httpClient的实例
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(url);
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();// 设置请求和传输超时时间
		httpGet.setConfig(requestConfig);
		try {
			CloseableHttpResponse response2 = httpclient.execute(httpGet);// 执行请求
			// log.info();
			HttpEntity entity2 = (HttpEntity) response2.getEntity();
			if (entity2 != null) {

				returnVal = EntityUtils.toString(entity2, "UTF-8");

			} else {
				returnVal = null;
			}

		} catch (ClientProtocolException e) {
			// log.info();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// log.info();

		} finally {

			if (httpclient != null) {

				httpclient.close();
			}

		}

		return returnVal;

	}

	/**
	 * @描述 json串转换map
	 * @param bizData
	 * @return
	 */
	public static Map<String, Object> parseJSON2Map(String bizData) {
		Map<String, Object> ret = new HashMap<String, Object>();
		try {
			JSONObject bizDataJson = JSONObject.parseObject(bizData);
			// 获取json对象值
			for (Object key : bizDataJson.keySet()) {
				Object value = bizDataJson.get(key);
				// 判断值是否为json数组类型
				if (value instanceof JSONArray) {
					// 如果为json数组类型迭代循环取值
					List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
					Iterator<Object> it = ((JSONArray) value).iterator();

					while (it.hasNext()) {
						JSONObject json2 = (JSONObject) it.next();
						list.add(parseJSON2Map(json2.toString()));
					}
					ret.put(String.valueOf(key), list);
				} else {
					ret.put(String.valueOf(key), String.valueOf(value));
				}
			}
		} catch (Exception e) {
			// log.info();
		}
		return ret;
	}

	/**
	 * post请求（用于请求json格式的参数）
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String post(String url, String params) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);// 创建httpPost
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json");
		String charSet = "UTF-8";
		StringEntity entity = new StringEntity(params, charSet);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(httpPost);
			StatusLine status = response.getStatusLine();
			int state = status.getStatusCode();
			if (state == HttpStatus.SC_OK) {
				HttpEntity responseEntity = response.getEntity();
				String jsonString = EntityUtils.toString(responseEntity);
				return jsonString;
			} else {
				// log.error("请求返回:"+state+"("+url+")");
			}
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
