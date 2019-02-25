package zyxhj.jiti.domain;

import java.util.Date;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ORGLoginBo {

	// 用户信息
	public Long id;
	public String name;
	public String nickname;
	public String signature;
	public String mobile;

	public String email;
	public String qqOpenId;
	public String wxOpenId;
	public String wbOpenId;
	public String roles;

	// Session信息
	public Date loginTime;
	public String loginToken;

	// 组织信息
	public Long orgId;
	public String realName;
	public String idNumber;

	public String address;
	public String shareCerNo;
	public String shareCerImg;
	public Boolean shareCerHolder;

	public Integer shareAmount;
	public Integer weight;

	public JSONArray orgRoles;
	public JSONObject orgTags;
}
