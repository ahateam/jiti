package zyxhj.jiti.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.AppVersion;
import zyxhj.utils.api.APIResponse;
import zyxhj.utils.api.Controller;

public class VersionController extends Controller {

	private static Logger log = LoggerFactory.getLogger(VoteController.class);

	public VersionController(String node) {
		super(node);
	}

	@POSTAPI(//
			path = "getVersion", //
			des = "获取app版本信息", //
			ret = "版本信息JSONObject对象")
	public JSONObject getVersion() {
		AppVersion ver = new AppVersion();
		return ver.getAppVersion();
	}

}
