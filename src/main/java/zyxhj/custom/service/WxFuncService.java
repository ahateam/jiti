package zyxhj.custom.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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

public class WxFuncService {

	private static Logger log = LoggerFactory.getLogger(WxFuncService.class);

	public WxFuncService() {
		try {
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
	 * 模板消息
	 */
	public void templateMessage(WxMpService wxMpService, String openId, String title, String content, Date createDate)
			throws WxErrorException {
		WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(openId) // oppenid
				.templateId("nQ0-qyYKcvcwLeBN2_cwkj6yJjC2xgGA0lQr_4odvZE")
				.url("http://jiti.online.3ch.org.cn/wap/index.html").build();

		templateMessage.addData(new WxMpTemplateData("first", title, "blue"));
		templateMessage.addData(new WxMpTemplateData("keynote1", content, "blue"));
		templateMessage.addData(new WxMpTemplateData("keynote2", createDate.toString(), "blue"));

		wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

	}

	/*
	 * 投票模板消息
	 */
	public void voteMessage(WxMpService wxMpService, String openId, String title, String options, Date startTime,
			Date expiryTime) throws WxErrorException {
		WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(openId) // oppenid
				.templateId("jOSl7ivdeibf2_axLpr5w8Jdo3Jq-OPjSiqsqyKKhfI")
				.url("http://jiti.online.3ch.org.cn/wap/index.html").build();

		templateMessage.addData(new WxMpTemplateData("first", "您有一条新的投票"));
		templateMessage.addData(new WxMpTemplateData("keyword1", title));
		templateMessage.addData(new WxMpTemplateData("keyword2", options));
		templateMessage.addData(new WxMpTemplateData("keyword3", startTime.toString()));
		templateMessage.addData(new WxMpTemplateData("keyword4", expiryTime.toString()));
		templateMessage.addData(new WxMpTemplateData("remark", "请及时查看并投出您宝贵的一票"));

		wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

	}

	/*
	 * 审核通知模板消息
	 */
	public void examineMessage(WxMpService wxMpService, String openId, String orgName, String familyMaster, String type,
			Date createTime) throws WxErrorException {
		WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder().toUser(openId) // oppenid
				.templateId("TdZQ3y4_2mXVzJFFgqbnAMwnNShEXHGrFJFZbt67V-U")
				.url("http://jiti.online.3ch.org.cn/wap/index.html").build();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss");
		String date2 = sdf.format(createTime);

		templateMessage.addData(new WxMpTemplateData("first", "您有一条新的审核"));
		templateMessage.addData(new WxMpTemplateData("keyword1", orgName));
		templateMessage.addData(new WxMpTemplateData("keyword2", familyMaster));
		templateMessage.addData(new WxMpTemplateData("keyword3", type));
		templateMessage.addData(new WxMpTemplateData("keyword4", date2));
		templateMessage.addData(new WxMpTemplateData("remark", "请及时处理"));

		wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);

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
