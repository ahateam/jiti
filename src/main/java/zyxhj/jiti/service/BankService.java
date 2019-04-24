package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONArray;

import zyxhj.core.domain.User;
import zyxhj.core.repository.UserRepository;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.domain.ORGUser;
import zyxhj.jiti.domain.Superior;
import zyxhj.jiti.repository.ORGRepository;
import zyxhj.jiti.repository.ORGUserRepository;
import zyxhj.jiti.repository.SuperiorRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.BaseRC;
import zyxhj.utils.api.ServerException;

public class BankService {

	private static Logger log = LoggerFactory.getLogger(BankService.class);

	private ORGRepository orgRepository;
	private SuperiorRepository superiorRepository;
	private UserRepository userRepository;
	private ORGUserRepository orgUserRepository;

	public BankService() {
		try {
			orgRepository = Singleton.ins(ORGRepository.class);
			superiorRepository = Singleton.ins(SuperiorRepository.class);
			userRepository = Singleton.ins(UserRepository.class);
			orgUserRepository = Singleton.ins(ORGUserRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 查询省
	public List<ORG> getPro(DruidPooledConnection conn, Integer count, Integer offset) throws Exception {

		return orgRepository.getListByKey(conn, "level", ORG.LEVEL.PRO.v(), count, offset);
	}

	public ORG createBankORG(DruidPooledConnection conn, Long districtId, String name, String address, String code)
			throws Exception {
		ORG existORG = orgRepository.getByKey(conn, "code", code);
		if (null == existORG) {
			ORG org = new ORG();
			org.id = IDUtils.getSimpleId();
			org.createTime = new Date();
			org.name = name;
			org.address = address;
			org.code = code;
			org.level = ORG.LEVEL.FINANCIAL.v();
			org.type = ORG.TYPE.FINANCIAL.v();
			// 创建银行组织
			orgRepository.insert(conn, org);

			// 将银行组织归入当前区级下
			Superior su = new Superior();
			su.superiorId = districtId;
			su.orgId = org.id;
			superiorRepository.insert(conn, su);

			return org;
		} else {
			// 组织已存在
			throw new ServerException(BaseRC.ECM_ORG_EXIST);
		}
	}

	public int editBankORG(DruidPooledConnection conn, Long bankId, String name, String address) throws Exception {
		ORG org = new ORG();
		org.name = name;
		org.address = address;

		return orgRepository.updateByKey(conn, "id", "bankId", org, true);
	}

	public int deleteBankORG(DruidPooledConnection conn, Long bankId) throws Exception {
		return orgRepository.deleteByKey(conn, "id", bankId);
	}

	public ORGUser createBankAdmin(DruidPooledConnection conn, Long bankId, String address, String idNumber,
			String mobile, String pwd, String realName) throws Exception {
		User exisUser = userRepository.getByKey(conn, "id_number", idNumber);
		// 用户不存在再去添加用户
		if (exisUser == null) {
			User user = new User();
			user.id = IDUtils.getSimpleId();
			user.idNumber = idNumber;
			user.mobile = mobile;
			user.realName = realName;
			user.pwd = pwd;

			userRepository.insert(conn, user);

			ORGUser oru = new ORGUser();
			oru.orgId = bankId;
			oru.userId = user.id;
			oru.address = address;
			oru.roles = "[102]";

			orgUserRepository.insert(conn, oru);

			return oru;

		} else {
			ORGUser oru = new ORGUser();
			oru.orgId = bankId;
			oru.userId = exisUser.id;
			oru.address = address;
			oru.roles = "[102]";

			orgUserRepository.insert(conn, oru);

			return oru;
		}

	}

	public List<ORGUser> getBankAdmin(DruidPooledConnection conn, Long bankId, Integer count, Integer offset)
			throws Exception {
		return orgUserRepository.getListByKey(conn, "org_id", bankId, count, offset);

	}

	public int deleteBankAdmin(DruidPooledConnection conn, Long bankId, Long userId) throws Exception {
		return orgUserRepository.deleteByANDKeys(conn, new String[] { "org_id", "user_id" },
				new Object[] { bankId, userId });
	}

	public List<ORG> getBankList(DruidPooledConnection conn, Long districtId, String name, Integer count,
			Integer offset) throws Exception {
		JSONArray json = new JSONArray();
		List<Superior> superior = superiorRepository.getListByKey(conn, "superior_id", districtId, null, null);
		for (Superior su : superior) {
			json.add(su.orgId);
		}
		Byte type = ORG.TYPE.FINANCIAL.v();
		return orgRepository.getBankList(conn, json, name, type, count, offset);
	}

	public List<ORG> getORGByBank(DruidPooledConnection conn, Long bankId, String name, Integer count, Integer offset)
			throws Exception {
		JSONArray json = new JSONArray();
		Superior bankSup = superiorRepository.getByKey(conn, "org_id", bankId);
		List<Superior> su = superiorRepository.getListByKey(conn, "superior_id", bankSup.superiorId, null, null);

		for (Superior superior : su) {
			json.add(superior.orgId);
		}
		Byte type = ORG.TYPE.COOPERATIVE.v();
		return orgRepository.getBankList(conn, json, name, type, count, offset);

	}

}
