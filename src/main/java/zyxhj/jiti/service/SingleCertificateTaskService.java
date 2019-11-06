package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.SingleCertificateTask;
import zyxhj.jiti.repository.ORGRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.repository.SingleCertificateTaskRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class SingleCertificateTaskService {

	private static Logger log = LoggerFactory.getLogger(SingleCertificateTaskService.class);

	private ORGRepository orgRepository;
	private ORGUserRepository orgUserRepository;
	private UserRepository userRepository;
	private SingleCertificateTaskRepository scftRepository;

	public SingleCertificateTaskService() {
		try {
			orgRepository = Singleton.ins(ORGRepository.class);
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
			userRepository = Singleton.ins(UserRepository.class);
			scftRepository = Singleton.ins(SingleCertificateTaskRepository.class);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	///////////////////////////////////////////////////
	// 证书打印接口

	public List<ORG> getORGList(DruidPooledConnection conn, String orgName, Integer count, Integer offset)
			throws Exception {
		if (StringUtils.isBlank(orgName)) {
			return orgRepository.getList(conn, null, count, offset);
		} else {
			return orgRepository.getList(conn, EXP.INS().and(EXP.LIKE("name", orgName)), count, offset);
		}
	}

	// 查询所有户主信息（只查询返回需要显示的字段）（待修改）
	public JSONObject getFamilyMasterList(DruidPooledConnection conn, Long orgId, String familyMaster, Integer count,
			Integer offset) throws Exception {
		String sql = new String();
		if (StringUtils.isBlank(familyMaster)) {
			sql = StringUtils.join(
					"select org.family_number , user.real_name , user.id_number from tb_user user,tb_ecm_org_user org where user.id = org.user_id and  org.org_id = ",
					orgId,
					" and org.family_number is not null GROUP BY org.family_master ORDER BY org.family_number asc");
		} else {
			sql = StringUtils.join(
					"select org.family_number , user.real_name , user.id_number from tb_user user,tb_ecm_org_user org where user.id = org.user_id and  org.org_id = ",
					orgId, " and org.family_number is not null and org.family_master like '%", familyMaster,
					"%' GROUP BY org.family_master ORDER BY org.family_number asc");
		}
		JSONObject jo = new JSONObject();
		JSONArray ja = orgUserRepository.getFamilyMasterList(conn, sql, count, offset);
		jo.put("familyMaster", ja);
		int c = getORGUserCount(conn, orgId);
		jo.put("familyCount", c);
		return jo;
	}

	public JSONObject getFamilyInfo(DruidPooledConnection conn, Long orgId, Long familyNumber) throws Exception {
		
		ORG org = orgRepository.get(conn, EXP.INS().key("id", orgId));
		// 获取户成员所有信息
		String familyMemberSql = StringUtils.join(
				"SELECT user.real_name, user.id_number,user.sex,org.family_relations FROM tb_user user, tb_ecm_org_user org WHERE user.id  = org.user_id AND org.family_number = ",
				familyNumber, " AND org.org_id = ", orgId);
		// 获取当前户家庭住址 总资产股份数与总资源股份数
		String familyInfoSql = StringUtils.join(
				"SELECT org.family_master, org.address, user.sex, COUNT(org.asset_shares) AS asset_share , COUNT(org.resource_shares) AS resource_shares, org.share_cer_no FROM tb_user user, tb_ecm_org_user org WHERE user.id = org.user_id AND org.family_number = ",
				familyNumber, " AND org.org_id = ", orgId);
		JSONObject info = userRepository.getFamilyMenber(conn, familyMemberSql,familyInfoSql);
		info.put("org",org);
		return info;
	}

	public JSONArray getFamilyInfoArray(DruidPooledConnection conn, Long orgId, Long startFamilyNumber,
			Long endFamilyNumber, Integer count, Integer offset) throws Exception {
		JSONArray familyInfoArray = new JSONArray();
		if (startFamilyNumber != endFamilyNumber) {
			EXP exp = EXP.INS().key("org_id", orgId).and(" family_number ", " > ", startFamilyNumber)
					.and(" family_number ", " < ", endFamilyNumber)
					.append(" GROUP BY family_number ORDER BY family_number asc ");
			List<ORGUser> familyNumbers = orgUserRepository.getList(conn, exp, count, offset, "family_number");
			for (ORGUser u : familyNumbers) {
				familyInfoArray.add(getFamilyInfo(conn, orgId, u.familyNumber));
			}
		} else {
			familyInfoArray.add(getFamilyInfo(conn, orgId, startFamilyNumber));
		}
		return familyInfoArray;

	}

	public SingleCertificateTask createSCFT(DruidPooledConnection conn, Long orgId, Long userId) throws Exception {
		SingleCertificateTask scft = new SingleCertificateTask();
		scft.id = IDUtils.getSimpleId();
		scft.orgId = orgId;
		scft.userId = userId;
		scft.createTime = new Date();
		scft.status = SingleCertificateTask.STATUS_IN;
		scftRepository.insert(conn, scft);
		return scft;
	}

	public int editSCFT(DruidPooledConnection conn, Long taskId, String fileUrl, Integer totalNumber) throws Exception {
		SingleCertificateTask scft = new SingleCertificateTask();
		if (StringUtils.isBlank(fileUrl)) {
			scft.fileUrl = fileUrl;
			scft.status = SingleCertificateTask.STATUS_SUCCESS;
		} else {
			scft.status = SingleCertificateTask.STATUS_FAIL;
		}
		scft.totalNumber = totalNumber;
		return scftRepository.update(conn, EXP.INS().key("id", taskId), scft, true);
	}

	public List<SingleCertificateTask> getSCFTByUserId(DruidPooledConnection conn, Long userId, String title,
			Integer count, Integer offset) throws Exception {
		EXP exp = EXP.INS().key("user_id", userId);
		if (!StringUtils.isBlank(title)) {
			exp.and(EXP.LIKE("title", title));
		}
		return scftRepository.getList(conn, exp, count, offset);
	}

	// 查询当前组织所有成员总数
	public int getORGUserCount(DruidPooledConnection conn, Long orgId) throws Exception {
		String sql = "select count(*) from tb_ecm_org_user where family_number is not null";
		return scftRepository.getORGUserCount(conn, sql);
	}

	public JSONObject getFamilyInfoByCodeANDFamilyNumber(DruidPooledConnection conn, Long familyNumber, String code) throws Exception {
		
		String familyMemberSql = StringUtils.join(
				"select user.real_name, user.id_number,user.sex,orgu.family_relations from tb_user user, tb_ecm_org_user orgu, tb_ecm_org org where user.id = orgu.user_id and orgu.org_id = org.id and orgu.family_number = ",
				familyNumber, " AND org.code = '", code,"'");
		String familyInfoSql = StringUtils.join(
				"SELECT orgu.family_master, orgu.address, user.sex, COUNT(orgu.asset_shares) AS asset_share , COUNT(orgu.resource_shares) AS resource_shares, orgu.share_cer_no FROM tb_user user, tb_ecm_org_user orgu, tb_ecm_org org WHERE user.id = orgu.user_id and orgu.org_id = org.id AND orgu.family_number = ",
				familyNumber, " AND org.code = '", code,"'");
		return userRepository.getFamilyMenber(conn, familyMemberSql, familyInfoSql);
	}

}
