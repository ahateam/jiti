package zyxhj.custom.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;

public class WxDataService {

	private static Logger log = LoggerFactory.getLogger(WxDataService.class);

	private WxMpInMemoryConfigStorage wxMpConfigStorage;
	private WxMpService wxMpService;

	public WxDataService() {
		try {
			// 微信参数配置
			wxMpConfigStorage = new WxMpInMemoryConfigStorage();
			wxMpConfigStorage.setAppId(WxDataService.APPID); // APPid
			wxMpConfigStorage.setSecret(WxDataService.APPSECRET); // AppSecret
			wxMpConfigStorage.setToken(WxDataService.TOKEN); // 设置微信公众号的token
			wxMpConfigStorage.setAesKey(WxDataService.AESKEY); // 设置微信公众号的EncodingAESKey
			wxMpService = new WxMpServiceImpl();
			wxMpService.setWxMpConfigStorage(wxMpConfigStorage);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public static final String APPID = "wx547972e25ec85006";
	public static final String APPSECRET = "0406d93e33c3400e3b4b673ea86a2679";
	public static final String TOKEN = "wx3ch";
	public static final String AESKEY = "XZ2ZdYwchouGBDzZEzpAJEKdAqTwKrcwiOMP7n2cNDJ";

	public WxMpInMemoryConfigStorage getWxMpConfigStorage() {
		return wxMpConfigStorage;
	}

	public WxMpService getWxMpService() {
		return wxMpService;
	}

}
