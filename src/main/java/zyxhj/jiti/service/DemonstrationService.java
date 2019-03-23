package zyxhj.jiti.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Asset;
import zyxhj.jiti.repository.DemonstrationRepository;
import zyxhj.utils.Singleton;

public class DemonstrationService {

	private static Logger log = LoggerFactory.getLogger(DemonstrationService.class);

	private DemonstrationRepository demonstrationRepository;

	public DemonstrationService() {
		try {
			demonstrationRepository = Singleton.ins(DemonstrationRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public List<Asset> getAsset(DruidPooledConnection conn, String groups, Integer count, Integer offset) throws Exception{
		return demonstrationRepository.getAsset(conn,groups,count,offset);
	}

	
	
	
}
