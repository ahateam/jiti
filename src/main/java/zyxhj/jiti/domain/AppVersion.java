package zyxhj.jiti.domain;

import com.alibaba.fastjson.JSONObject;

public class AppVersion {

//		version:107,
//		android:"http://jiti-online.oss-cn-hangzhou.aliyuncs.com/app/107/android/jiti.apk",
//		ios:"http://jiti-online.oss-cn-hangzhou.aliyuncs.com/app/107/ios/jiti.ipa"

	private static String VERSION = "112";

	private static String ANDROID = "http://jiti-online.oss-cn-hangzhou.aliyuncs.com/app/112/android/jiti.apk";

	private static String IOS = "http://jiti-online.oss-cn-hangzhou.aliyuncs.com/app/112/ios/jiti.ipa";

	private static JSONObject APPVERSION = new JSONObject();

	public JSONObject getAppVersion() {
		APPVERSION.put("version", VERSION);
		APPVERSION.put("android", ANDROID);
		APPVERSION.put("ios", IOS);
		return APPVERSION;
	}

}
