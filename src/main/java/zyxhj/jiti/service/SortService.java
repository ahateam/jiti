package zyxhj.jiti.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;

import zyxhj.jiti.domain.Advert;
import zyxhj.jiti.repository.AdvertRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.data.EXP;

public class SortService {

	private static Logger log = LoggerFactory.getLogger(SortService.class);

	private AdvertRepository advertRepository;

	public SortService() {
		try {

			advertRepository = Singleton.ins(AdvertRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	// 创建广告
	public Advert createAdvert(DruidPooledConnection conn, String title, String data, String tags, Byte type)
			throws Exception {
		Advert ad = new Advert();
		ad.id = IDUtils.getSimpleId();
		ad.title = title;
		ad.data = data;
		ad.tags = tags;
		advertRepository.insert(conn, ad);
		return ad;
	}

	// 修改广告
	public Advert editAdvert(DruidPooledConnection conn, Long advertId, String title, String data, String tags)
			throws Exception {
		Advert ad = new Advert();
		ad.title = title;
		ad.data = data;
		ad.tags = tags;
		advertRepository.update(conn,EXP.INS().key("id",advertId), ad, true);
		
		return ad;
	}

	// 删除广告
	public void delAdvert(DruidPooledConnection conn, Long advertId) throws Exception {
		advertRepository.delete(conn, EXP.INS().key("id", advertId));
	}

	// 广告查询
	public List<Advert> getAdverts(DruidPooledConnection conn, Integer count, Integer offset) throws Exception {
		return advertRepository.getList(conn, null, count, offset);
	}

}
