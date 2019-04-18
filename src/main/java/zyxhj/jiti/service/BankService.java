package zyxhj.jiti.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Asset;
import zyxhj.jiti.domain.ORG;
import zyxhj.jiti.repository.AssetRepository;
import zyxhj.jiti.repository.ORGRepository;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.ServerException;

public class BankService {

	private static Logger log = LoggerFactory.getLogger(BankService.class);

	private AssetRepository assetRepository;
	private ORGRepository orgRepository;

	public BankService() {
		try {
			assetRepository = Singleton.ins(AssetRepository.class);
			orgRepository = Singleton.ins(ORGRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 查询省
	public List<ORG> getPro(DruidPooledConnection conn, Integer count, Integer offset) throws Exception {

		return orgRepository.getListByKey(conn, "level", ORG.LEVEL.PRO.v(), count, offset);
	}


}
