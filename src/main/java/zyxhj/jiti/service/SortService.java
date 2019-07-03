package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.PrimaryKey;

import zyxhj.core.domain.Car;
import zyxhj.core.repository.CarRepository;
import zyxhj.jiti.domain.Advert;
import zyxhj.jiti.repository.AdvertRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.ts.ColumnBuilder;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.TSRepository;

public class SortService {

	private static Logger log = LoggerFactory.getLogger(SortService.class);

	private AdvertRepository advertRepository;
	private CarRepository carRepository;

	public SortService() {
		try {

			advertRepository = Singleton.ins(AdvertRepository.class);
			carRepository = Singleton.ins(CarRepository.class);
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
		advertRepository.updateByKey(conn, "id", advertId, ad, true);
		return ad;
	}

	// 删除广告
	public void delAdvert(DruidPooledConnection conn, Long advertId) throws Exception {
		advertRepository.deleteByKey(conn, "id", advertId);
	}

	// 广告查询
	public List<Advert> getAdverts(DruidPooledConnection conn, Integer count, Integer offset) throws Exception {
		return advertRepository.getList(conn, count, offset);
	}

	// 创建打车信息
	public Car createCar(SyncClient client, String title, String content, String province, String city, String region,
			String pos, Byte type, String tags) throws Exception {
		Car c = new Car();
		long id = IDUtils.getSimpleId();
		c._id = IDUtils.simpleId2Hex(id).substring(0, 4);
		c.id = id;

		c.title = title;
		c.content = content;
		c.province = province;
		c.city = city;
		c.region = region;
		c.pos = pos;
		c.type = (long) type;
		c.time = new Date();
		c.tags = tags;
		c.status = (long) Car.STATUS.NOTFIND.v();
		carRepository.insert(client, c, false);
		return c;

	}

	// 删除打车信息
	public void delCar(SyncClient client, Long id, Long carId) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("_id", id).add("id", carId).build();
		TSRepository.nativeDel(client, carRepository.getTableName(), pk);
	}

	// 修改打车信息
	public void editCar(SyncClient client, Long id, Long carId, String title, String content, String province,
			String city, String region, String pos, Byte type, String tags) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("_id", id).add("id", carId).build();
		ColumnBuilder cb = new ColumnBuilder();
		cb.add("title", title);
		cb.add("content", content);
		cb.add("province", province);
		cb.add("city", city);
		cb.add("region", region);
		cb.add("pos", pos);
		cb.add("type", (long) type);
		cb.add("tags", tags);
		cb.add("status", (long) Car.STATUS.NOTFIND.v());
		List<Column> columns = cb.build();
		TSRepository.nativeUpdate(client, carRepository.getTableName(), pk, columns);
	}

	// 获取打车信息 根据条件获取
	public List<Car> getCars(SyncClient client,Byte status,Byte type) {
		
		
		return null;

	}
}
