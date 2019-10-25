package zyxhj.jiti.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.PrimaryKey;

import zyxhj.core.domain.Examine;
import zyxhj.core.repository.ExamineRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGPermission;
import zyxhj.jiti.domain.ORGPermissionRel;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.repository.ORGPermissionRelaRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.utils.Singleton;
import zyxhj.utils.UploadFile;
import zyxhj.utils.data.EXP;
import zyxhj.utils.data.ts.ColumnBuilder;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.TSRepository;

/**
 * 
 * @author JXians
 *
 *
 *已废弃
 */






public class ExamineService {

	private static Logger log = LoggerFactory.getLogger(ExamineService.class);
	private ORGUserRepository orgUserRepository;
	private ORGService orgService;
	private ORGPermissionRelaRepository orgPermissionRelaRepository;
//	private WxDataService wxDataService;
	// private WxFuncService wxFuncService;
	private MessageService messageService;
	private ExamineRepository examineRepository;
	private ORGUserService orgUserService;
	private static UploadFile uploadFile;

	public ExamineService() {
		try {
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			orgService = Singleton.ins(ORGService.class);
			orgPermissionRelaRepository = Singleton.ins(ORGPermissionRelaRepository.class);
//			wxDataService = Singleton.ins(WxDataService.class);
			// wxFuncService = Singleton.ins(WxFuncService.class);
			messageService = Singleton.ins(MessageService.class);
			examineRepository = Singleton.ins(ExamineRepository.class);
			orgUserService = Singleton.ins(ORGUserService.class);
			uploadFile = Singleton.ins(UploadFile.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 创建户审核任务
	public List<Column> createExamine(SyncClient client, DruidPooledConnection conn, Long orgId, String data, Byte type,
			String remark) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("orgId", orgId).addAutoIncermentKey("examineId").build();
		ColumnBuilder cb = new ColumnBuilder();
		cb.add("data", data);
		cb.add("createDate", new Date());
		cb.add("examineDate", new Date());
		cb.add("type", (long) type);

		if (type == Examine.TYPE.FAMILY.v()) {
			// ORGPermissionRel orgPer = orgPermissionRelaRepository.getByANDKeys(conn,
			// new String[] { "org_id", "permission_id" },
			// new Object[] { orgId, ORGPermission.per_feparate_family.id });
			ORGPermissionRel orgPer = orgPermissionRelaRepository.get(conn,
					EXP.INS().key("org_id", orgId).andKey("permission_id", ORGPermission.per_feparate_family.id));
			if (orgPer != null) {
				cb.add("status", (long) Examine.STATUS.NOEXAMINE.v());
				// 给审核人员发送通知
				examineMessage(conn, orgId, data, Examine.STATUS.NOEXAMINE.v(), ORGPermission.per_feparate_family.id);

				// 平台内消息通知
				message(conn, data, ORGPermission.per_feparate_family.name, Examine.STATUS.NOEXAMINE.v());

			} else {
				cb.add("status", (long) Examine.STATUS.ORGEXAMINE.v());
				message(conn, data, ORGPermission.per_feparate_family.name, Examine.STATUS.ORGEXAMINE.v());
			}

		} else if (type == Examine.TYPE.SHARE.v()) {
			// ORGPermissionRel orgPer = orgPermissionRelaRepository.getByANDKeys(conn,
			// new String[] { "org_id", "permission_id" },
			// new Object[] { orgId, ORGPermission.per_share_change.id });
			ORGPermissionRel orgPer = orgPermissionRelaRepository.get(conn,
					EXP.INS().key("org_id", orgId).andKey("permission_id", ORGPermission.per_share_change.id));
			if (orgPer != null) {
				cb.add("status", (long) Examine.STATUS.NOEXAMINE.v());
				// 给审核人员发送通知
				examineMessage(conn, orgId, data, Examine.STATUS.NOEXAMINE.v(), ORGPermission.per_share_change.id);

				shareMessage(conn, data, ORGPermission.per_share_change.name, Examine.STATUS.NOEXAMINE.v());
			} else {
				cb.add("status", (long) Examine.STATUS.ORGEXAMINE.v());
				shareMessage(conn, data, ORGPermission.per_share_change.name, Examine.STATUS.ORGEXAMINE.v());
			}
		} else if (type == Examine.TYPE.ORG.v()) {
			cb.add("status", (long) Examine.STATUS.NOEXAMINE.v());
		}
		cb.add("remark", remark);
		List<Column> columns = cb.build();
		TSRepository.nativeInsert(client, examineRepository.getTableName(), pk, columns, true);
		return columns;

	}

	// 发送微信通知
	private void examineMessage(DruidPooledConnection conn, Long orgId, String da, Byte status, Long permissionId)
			throws Exception {
		JSONObject jo = JSONObject.parseObject(da);
		JSONArray json = new JSONArray();
		String type = "";
		String familyMaster = "";
		// 获取org信息
		ORG or = orgService.getORGById(conn, orgId);
		if (status == Examine.STATUS.NOEXAMINE.v()) {
			// List<ORGPermissionRel> orgPermission =
			// orgPermissionRelaRepository.getListByANDKeys(conn,
			// new String[] { "org_id", "permission_id" }, new Object[] { orgId,
			// permissionId }, 64, 0);
			List<ORGPermissionRel> orgPermission = orgPermissionRelaRepository.getList(conn,
					EXP.INS().key("org_id", orgId).andKey("permission_id", permissionId), 64, 0);
			for (ORGPermissionRel orgPermissionRel : orgPermission) {

				json.add(orgPermissionRel.roleId);
			}

			JSONArray user = orgUserRepository.getUserByRoles(conn, orgId, json);
			for (int i = 0; i < user.size(); i++) {
				JSONObject userInfo = user.getJSONObject(i);
				String openId = userInfo.getString("wxOpenId");
				if (openId == null) {
					continue;
				}
				if (permissionId == ORGPermission.per_feparate_family.id) {
					JSONObject ext = jo.getJSONObject("ext");
					Byte familyOperate = ext.getByte("familyOperate");
					if (familyOperate == Examine.OPERATE.ADDFAMILY.v()) {
						type = Examine.OPERATE.ADDFAMILY.txt();
						JSONArray newDatas = jo.getJSONArray("newData");
						JSONArray newData = JSONArray.parseArray(newDatas.getString(0));
						JSONObject familyInfo = newData.getJSONObject(0);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.HOUSEHOLD.v()) {
						type = Examine.OPERATE.HOUSEHOLD.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = data.getJSONObject(i);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.ADDFAMILYUSER.v()) {
						type = Examine.OPERATE.ADDFAMILYUSER.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = data.getJSONObject(i);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.DELFAMILYUSER.v()) {
						type = Examine.OPERATE.DELFAMILYUSER.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = data.getJSONObject(i);
						familyMaster = familyInfo.getString("familyMaster");
					} else if (familyOperate == Examine.OPERATE.MOVEFAMILYUSER.v()) {
						type = Examine.OPERATE.MOVEFAMILYUSER.txt();
						JSONArray data = jo.getJSONArray("oldData");
						JSONObject familyInfo = (data.getJSONArray(0)).getJSONObject(i);

						familyMaster = familyInfo.getString("familyMaster");
					}
				} else if (permissionId == ORGPermission.per_share_change.id) {
					type = Examine.OPERATE.UPSHARE.txt();
					JSONArray newDatas = jo.getJSONArray("newData");
					JSONObject newData = newDatas.getJSONObject(0);
					JSONObject orgUserInfo = newData.getJSONObject("user");
					familyMaster = orgUserInfo.getString("realName");
				}
				// wxFuncService.examineMessage(wxDataService.getWxMpService(), openId, or.name,
				// familyMaster, type,
				// new Date());
			}
//			messageService.createExamineMessages(conn, user, type, da, status);
		}

	}

	private void message(DruidPooledConnection conn, String data, String perName, Byte examineStatus) throws Exception {

		JSONObject jo = JSONObject.parseObject(data);
		JSONObject ext = jo.getJSONObject("ext");
		Byte familyOperate = ext.getByte("familyOperate");
		JSONArray oldDatas = jo.getJSONArray("oldData");
		if (familyOperate == Examine.OPERATE.MOVEFAMILYUSER.v()) {
			for (int i = 0; i < oldDatas.size(); i++) {
				JSONArray oldData = oldDatas.getJSONArray(i);
//				messageService.createExamineMessages(conn, oldData, perName, data, examineStatus);
			}
		} else {
//			messageService.createExamineMessages(conn, oldDatas, perName, data, examineStatus);
		}
	}

	private void shareMessage(DruidPooledConnection conn, String data, String perName, Byte examineStatus)
			throws Exception {
		JSONObject jo = JSONObject.parseObject(data);
		JSONArray oldDatas = jo.getJSONArray("oldData");
		for (int i = 0; i < oldDatas.size(); i++) {
			JSONObject userInfo = oldDatas.getJSONObject(i);
			JSONObject user = userInfo.getJSONObject("user");
			Long userId = user.getLong("id");
			JSONObject orgUser = userInfo.getJSONObject("orgUser");
			Long orgId = orgUser.getLong("orgId");
			messageService.createExamineMessage(conn, orgId, userId, perName, data, examineStatus);
		}
	}

	// 修改审核
	public JSONObject examine(SyncClient client, DruidPooledConnection conn, Long examineId, Long orgId, Byte status)
			throws Exception {

		PrimaryKey pk = new PrimaryKeyBuilder().add("orgId", orgId).add("examineId", examineId).build();
		JSONObject data = TSRepository.nativeGet(client, examineRepository.getTableName(), pk);
		JSONObject da = data.getJSONObject("data");
		ColumnBuilder cb = new ColumnBuilder();
		if (status == Examine.STATUS.ORGEXAMINE.v()) {
			// 组织审核
			cb.add("examineDate", new Date());
			cb.add("status", (long) status);
			List<Column> columns = cb.build();
			TSRepository.nativeUpdate(client, examineRepository.getTableName(), pk, true, columns);
			message(conn, da.toJSONString(), ORGPermission.per_feparate_family.name, status);
			return data;
		} else if (status == Examine.STATUS.DISEXAMINE.v() || status == Examine.STATUS.WAITEC.v()) {

			// TODO 目前没加事务，有隐患

			// 开始审核
			examineStart(client, conn, orgId, examineId, da);

			// 区级审核
			cb.add("examineDate", new Date());
			cb.add("status", (long) status);
			List<Column> columns = cb.build();
			TSRepository.nativeUpdate(client, examineRepository.getTableName(), pk, true, columns);
			message(conn, da.toJSONString(), ORGPermission.per_feparate_family.name, status);
			return data;
		} else {
			// 审核失败
			cb.add("examineDate", new Date());
			cb.add("status", (long) Examine.STATUS.FAIL.v());
			List<Column> columns = cb.build();
			TSRepository.nativeUpdate(client, examineRepository.getTableName(), pk, true, columns);
			message(conn, da.toJSONString(), ORGPermission.per_feparate_family.name, Examine.STATUS.FAIL.v());
			return data;
		}
	}

	// 审核 添加户/分户/新增户成员/删除户成员/移户操作
	private JSONArray examineStart(SyncClient client, DruidPooledConnection conn, Long orgId, Long examineId,
			JSONObject da) throws Exception {
		// 查询审核数据
		JSONObject ext = da.getJSONObject("ext");
		Byte familyOperate = ext.getByte("familyOperate");

		// 根据操作执行对应的方法
		if (familyOperate == Examine.OPERATE.ADDFAMILY.v()) {
			// 执行添加户操作
			return createFamily(client, conn, orgId, examineId, da);
		} else if (familyOperate == Examine.OPERATE.HOUSEHOLD.v()) {
			// 执行分户操作
			return household(client, conn, orgId, examineId, da);
		} else if (familyOperate == Examine.OPERATE.ADDFAMILYUSER.v()) {
			// 执行添加户成员操作
			editfamilyuser(conn, da);
			return null;
		} else if (familyOperate == Examine.OPERATE.DELFAMILYUSER.v()) {
			// 执行移除户成员操作
			editfamilyuser(conn, da);
			return null;
		} else if (familyOperate == Examine.OPERATE.MOVEFAMILYUSER.v()) {
			// 执行移户操作
			movefamilyuser(conn, da);
			return null;
		} else {
			return null;
		}
	}

	// 移户操作
	private void movefamilyuser(DruidPooledConnection conn, JSONObject jsonObj) throws Exception {
		JSONObject ext = jsonObj.getJSONObject("ext");
		String editHouseholder = ext.getString("editHouseholder");
		// 移户 一边增加人员一边删除人员 先去遍历删除人员 再去执行添加人员
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		// 先遍历删除
		for (int i = 0; i < newDatas.size(); i++) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
			for (int j = 0; j < newData.size(); j++) {
				JSONObject jo = newData.getJSONObject(j);
				// 获取到删除的标记
				Byte userTab = jo.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.REMOVE.v()) {
					// 移除户成员
					Long or = jo.getLong("orgId");
					Long userId = jo.getLong("userId");
					orgUserRepository.delete(conn, EXP.INS().key("org_id", or).andKey("user_id", userId));
				} else {
					continue;
				}
			}
		}

		// 遍历新增
		for (int i = 0; i < newDatas.size(); i++) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
			for (int j = 0; j < newData.size(); j++) {
				JSONObject jo = newData.getJSONObject(j);
				// 获取到新增的标记
				Byte userTab = jo.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.ADD.v()) {
					Long or = jo.getLong("orgId");
					String mobile = jo.getString("mobile");
					String realName = jo.getString("realName");
					String idNumber = jo.getString("idNumber");
					String address = jo.getString("address");
					String shareCerNo = jo.getString("shareCerNo");
					String shareCerImg = jo.getString("shareCerImg");
					Boolean shareCerHolder = jo.getBoolean("shareCerHolder");
					Double shareAmount = jo.getDouble("shareAmount");
					Integer weight = jo.getInteger("weight");
					JSONArray roles = jo.getJSONArray("roles");
					JSONArray groups = jo.getJSONArray("groups");
					Long familyNumber = jo.getLong("familyNumber");
					String familyMaster = jo.getString("familyMaster");
					JSONObject tags = jo.getJSONObject("tags");
					
					Double resourceShares = jo.getDouble("resourceShares");
					Double assetShares = jo.getDouble("assetShares");
					Boolean isOrgUser = jo.getBoolean("isOrgUser");
					Byte sex = jo.getByte("sex");
					String familyRelations = jo.getString("familyRelations");
					
//					orgUserService.oldcreateORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo,
//							shareCerImg, shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber,
//							familyMaster);
					

					orgUserService.createORGUser(conn, or, mobile, realName, idNumber, sex, familyRelations,
							resourceShares, assetShares, isOrgUser, address, shareCerNo, shareCerImg, shareCerHolder,
							shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);
				} else {
					continue;
				}
			}
		}

		// editHouseholder修改户主不为空 则表示需要修改户主
		if (StringUtils.isNotBlank(editHouseholder)) {
			for (int i = 0; i < newDatas.size(); i++) {
				// 获取到新数据里每一个户
				JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
				JSONObject jo = newData.getJSONObject(0);
				Long or = jo.getLong("orgId");
				Long familyNumber = jo.getLong("familyNumber");
				String familyMaster = jo.getString("familyMaster");
				ORGUser orgUser = new ORGUser();
				orgUser.familyMaster = familyMaster;
				orgUserRepository.update(conn, EXP.INS().key("org_id", or).andKey("family_number", familyNumber),
						orgUser, true);

			}
		}

	}

	// 修改户成员操纵 添加/删除
	private void editfamilyuser(DruidPooledConnection conn, JSONObject jsonObj) throws Exception {
		JSONObject ext = jsonObj.getJSONObject("ext");
		String editHouseholder = ext.getString("editHouseholder");
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		for (int i = 0; i < newDatas.size(); i++) {
			JSONArray newData = newDatas.getJSONArray(i);
			for (int j = 0; j < newData.size(); j++) {
				JSONObject json = newData.getJSONObject(j);
				// 取到用户标记
				Byte userTab = json.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.ADD.v()) {
					Long or = json.getLong("orgId");
					String mobile = json.getString("mobile");
					String realName = json.getString("realName");
					String idNumber = json.getString("idNumber");
					String address = json.getString("address");
					String shareCerNo = json.getString("shareCerNo");
					String shareCerImg = json.getString("shareCerImg");
					Boolean shareCerHolder = json.getBoolean("shareCerHolder");
					Double shareAmount = json.getDouble("shareAmount");
					Integer weight = json.getInteger("weight");
					JSONArray roles = json.getJSONArray("roles");
					JSONArray groups = json.getJSONArray("groups");
					Long familyNumber = json.getLong("familyNumber");
					String familyMaster = json.getString("familyMaster");
					JSONObject tags = json.getJSONObject("tags");
					
					Double resourceShares = json.getDouble("resourceShares");
					Double assetShares = json.getDouble("assetShares");
					Boolean isOrgUser = json.getBoolean("isOrgUser");
					Byte sex = json.getByte("sex");
					String familyRelations = json.getString("familyRelations");
					
//					orgUserService.oldcreateORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo,
//							shareCerImg, shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber,
//							familyMaster);
					
					orgUserService.createORGUser(conn, or, mobile, realName, idNumber, sex, familyRelations,
							resourceShares, assetShares, isOrgUser, address, shareCerNo, shareCerImg, shareCerHolder,
							shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);

					
					
					
				} else if (userTab != null && userTab == Examine.TAB.REMOVE.v()) {
					// 移除户成员
					Long or = json.getLong("orgId");
					Long userId = json.getLong("userId");
					// orgUserRepository.deleteByANDKeys(conn, new String[] { "org_id", "user_id" },
					// new Object[] { or, userId });
					orgUserRepository.delete(conn, EXP.INS().key("org_id", or).andKey("user_id", userId));
				} else {
					continue;
				}
			}
		}

		if (StringUtils.isNotBlank(editHouseholder)) {
			JSONArray js = newDatas.getJSONArray(0);
			// 获取到新数据里每一个户
			JSONObject jo = js.getJSONObject(0);
			Long or = jo.getLong("orgId");
			Long familyNumber = jo.getLong("familyNumber");
			String familyMaster = jo.getString("familyMaster");
			ORGUser orgUser = new ORGUser();
			orgUser.familyMaster = familyMaster;
			orgUserRepository.update(conn, EXP.INS().key("org_id", or).andKey("family_number", familyNumber), orgUser,
					true);

		}

	}

	// 分户
	private JSONArray household(SyncClient client, DruidPooledConnection conn, Long orgId, Long examineId,
			JSONObject jsonObj) throws Exception {
		JSONObject ext = jsonObj.getJSONObject("ext");
		String editHouseholder = ext.getString("editHouseholder");
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		JSONArray editData = new JSONArray();
		// 遍历分户新数据
		for (int i = 0; i < newDatas.size(); i++) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(i));
			JSONArray js = new JSONArray();
			// 计算户序号最大值
			Long max = 0L;
			String master = "";
			// 遍历第一户的数据
			for (int j = 0; j < newData.size(); j++) {
				JSONObject jo = newData.getJSONObject(j);
				// 先判断此用户标记 0 为移除户成员 1为新增的户成员
				Byte userTab = jo.getByte("userTab");
				if (userTab != null && userTab == Examine.TAB.ADD.v()) {
					// 添加户成员
					Long or = jo.getLong("orgId");
					String mobile = jo.getString("mobile");
					String realName = jo.getString("realName");
					String idNumber = jo.getString("idNumber");
					String address = jo.getString("address");
					String shareCerNo = jo.getString("shareCerNo");
					String shareCerImg = jo.getString("shareCerImg");
					Boolean shareCerHolder = jo.getBoolean("shareCerHolder");
					Double shareAmount = jo.getDouble("shareAmount");
					Integer weight = jo.getInteger("weight");
					JSONArray roles = jo.getJSONArray("roles");
					JSONArray groups = jo.getJSONArray("groups");
					Long familyNumber = jo.getLong("familyNumber");
					String familyMaster = jo.getString("familyMaster");
					if (familyNumber == null) {
						if (familyMaster.equals(master)) {
							familyNumber = max;
						} else {
							master = familyMaster;
							ORGUser maxFam = orgUserRepository.maxFamilyNumber(conn, orgId);
							max = maxFam.familyNumber + 1;
							familyNumber = max;
						}
					}
					JSONObject tags = jo.getJSONObject("tags");
					

					Double resourceShares = jo.getDouble("resourceShares");
					Double assetShares = jo.getDouble("assetShares");
					Boolean isOrgUser = jo.getBoolean("isOrgUser");
					Byte sex = jo.getByte("sex");
					String familyRelations = jo.getString("familyRelations");
					
					
//					orgUserService.oldcreateORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo,
//							shareCerImg, shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber,
//							familyMaster);
					
					orgUserService.createORGUser(conn, or, mobile, realName, idNumber, sex, familyRelations,
							resourceShares, assetShares, isOrgUser, address, shareCerNo, shareCerImg, shareCerHolder,
							shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);

					
					jo.put("familyNumber", familyNumber);
					js.add(jo);
				} else if (userTab != null && userTab == Examine.TAB.REMOVE.v()) {
					// 移除户成员
					Long or = jo.getLong("orgId");
					Long userId = jo.getLong("userId");
					// orgUserRepository.deleteByANDKeys(conn, new String[] { "org_id", "user_id" },
					// new Object[] { or, userId });
					orgUserRepository.delete(conn, EXP.INS().key("org_id", or).andKey("user_id", userId));
					js.add(jo);
				} else {
					js.add(jo);
					continue;
				}

			}
			editData.add(js);
		}
		PrimaryKey pk = new PrimaryKeyBuilder().add("orgId", orgId).add("examineId", examineId).build();
		jsonObj.put("newData", editData);
		ColumnBuilder cb = new ColumnBuilder();
		cb.add("data", jsonObj.toJSONString());
		List<Column> columns = cb.build();
		TSRepository.nativeUpdate(client, examineRepository.getTableName(), pk, true, columns);

		// editHouseholder修改户主不为空 则表示需要修改户主
		if (StringUtils.isNotBlank(editHouseholder)) {
			// 获取到新数据里每一个户
			JSONArray newData = JSONArray.parseArray(newDatas.getString(0));
			JSONObject jo = newData.getJSONObject(0);
			Long or = jo.getLong("orgId");
			Long familyNumber = jo.getLong("familyNumber");
			String familyMaster = jo.getString("familyMaster");
			ORGUser orgUser = new ORGUser();
			orgUser.familyMaster = familyMaster;
			orgUserRepository.update(conn, EXP.INS().key("org_id", or).andKey("family_number", familyNumber), orgUser,
					true);

		}
		return editData;

	}

	// 新增户
	private JSONArray createFamily(SyncClient client, DruidPooledConnection conn, Long orgId, Long examineId,
			JSONObject jsonObj) throws Exception {
		// 获取组织户序号 并加上1
		ORGUser faNum = orgUserRepository.maxFamilyNumber(conn, orgId);
		Long maxNum = faNum.familyNumber + 1;// TODO 线程不安全，目前不解决
		JSONArray addNewData = new JSONArray();
		JSONArray newDatas = jsonObj.getJSONArray("newData");
		for (int i = 0; i < newDatas.size(); i++) {
			JSONArray newData = newDatas.getJSONArray(i);
			JSONArray addFamily = new JSONArray();
			for (int j = 0; j < newData.size(); j++) {
				JSONObject json = newData.getJSONObject(j);
				Long or = json.getLong("orgId");
				String mobile = json.getString("mobile");
				String realName = json.getString("realName");
				String idNumber = json.getString("idNumber");
				String address = json.getString("address");
				String shareCerNo = json.getString("shareCerNo");
				String shareCerImg = json.getString("shareCerImg");
				Boolean shareCerHolder = json.getBoolean("shareCerHolder");
				Double shareAmount = json.getDouble("shareAmount");
				Integer weight = json.getInteger("weight");
				JSONArray roles = json.getJSONArray("roles");
				JSONArray groups = json.getJSONArray("groups");
				Long familyNumber = maxNum;
				String familyMaster = json.getString("familyMaster");
				JSONObject tags = json.getJSONObject("tags");

				Double resourceShares = json.getDouble("resourceShares");
				Double assetShares = json.getDouble("assetShares");
				Boolean isOrgUser = json.getBoolean("isOrgUser");
				Byte sex = json.getByte("sex");
				String familyRelations = json.getString("familyRelations");

				json.put("familyNumber", maxNum);
//				orgUserService.oldcreateORGUser(conn, or, mobile, realName, idNumber, address, shareCerNo, shareCerImg,
//						shareCerHolder, shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);

				orgUserService.createORGUser(conn, or, mobile, realName, idNumber, sex, familyRelations,
						resourceShares, assetShares, isOrgUser, address, shareCerNo, shareCerImg, shareCerHolder,
						shareAmount, weight, roles, groups, tags, familyNumber, familyMaster);

				addFamily.add(json);
			}
			addNewData.add(addFamily);
		}
		jsonObj.put("newData", addNewData);

		PrimaryKey pk = new PrimaryKeyBuilder().add("orgId", orgId).add("examineId", examineId).build();
		ColumnBuilder cb = new ColumnBuilder();
		cb.add("data", jsonObj.toJSONString());
		List<Column> columns = cb.build();
		TSRepository.nativeUpdate(client, examineRepository.getTableName(), pk, true, columns);

		return addNewData;
	}
	// 审批中添加无限文件上传
	// 直接将文件复制上传到OSS服务器中，下一个审批流程时，通过需求，直接重OSS服务器中获取文件到本地
	public void uploadFile(JSONArray fileUrls) {
		try {
			// 遍历数组，得到本地文件路径
			for (Object obj : fileUrls) {
				String fileUrl = (String) obj;
				// 通过文件路径生成文件对象（FILE）
				File file = new File(fileUrl);
				InputStream inputStream = new FileInputStream(file);
				JSONArray ja = getRoleArray(fileUrl,"\\" );
				System.out.println(ja.get(ja.size()-1));
				uploadFile.uploadFileToOSS(UploadFile.OSSCATALOGUE_EXAMINE, ja.getString(ja.size()-1), inputStream);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private JSONArray getRoleArray(String roleStr,String Separator) {
		JSONArray roleArray = new JSONArray();
		roleStr.trim();
		int start = roleStr.indexOf(Separator);
		if (start < 0) {
			roleArray.add(roleStr);
		} else {
			int end = roleStr.lastIndexOf(Separator);
			roleArray.add(roleStr.substring(0, start));
			if (end == start) {
				roleArray.add(roleStr.substring(start + 1, roleStr.length()));
			} else {
				String newStr = roleStr.substring(start + 1, roleStr.length());
				String newStr2 = newStr;
				for (int i = 0; i < 5; i++) {
					if (newStr2.indexOf(Separator) > 0) {
						newStr2 = newStr.substring(newStr.indexOf(Separator) + 1, newStr.length());
						newStr = newStr.substring(0, newStr.indexOf(Separator));
						roleArray.add(newStr);
						start = newStr.indexOf(Separator) + 1;
						newStr = newStr2;
					} else {
						newStr2 = newStr2.substring(newStr2.lastIndexOf(Separator) + 1, newStr2.length());
						roleArray.add(newStr2);
						break;
					}
				}
			}
		}
		return roleArray;
	}
	
	
	
}
