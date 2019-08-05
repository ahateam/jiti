package zyxhj.jiti.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Asset;
import zyxhj.jiti.domain.ORGUserTagGroup;
import zyxhj.jiti.repository.DemonstrationRepository;
import zyxhj.jiti.repository.ORGUserTagGroupRepository;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class DemonstrationService {

	private static Logger log = LoggerFactory.getLogger(DemonstrationService.class);

	private DemonstrationRepository demonstrationRepository;
	private ORGUserTagGroupRepository orgUserTagGroupRepository;

	public DemonstrationService() {
		try {
			demonstrationRepository = Singleton.ins(DemonstrationRepository.class);
			orgUserTagGroupRepository = Singleton.ins(ORGUserTagGroupRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public List<Asset> getAsset(DruidPooledConnection conn, Long orgId, String groups) throws Exception {
		return demonstrationRepository.getAsset(conn, orgId, groups, 500, 0);
	}

	public List<ORGUserTagGroup> getGroup(DruidPooledConnection conn, Long orgId) throws Exception {
		return orgUserTagGroupRepository.getList(conn,EXP.ins().key("org_id", orgId), null, null);
	}

	public Asset getAssetById(DruidPooledConnection conn, Long assetId, Long orgId) throws Exception {
		return demonstrationRepository.get(conn, EXP.ins().key("id", assetId).andKey("org_id", orgId));
	}

}
