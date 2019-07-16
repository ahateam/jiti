package zyxhj.jiti.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSONObject;
import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.Column;
import com.alicloud.openservices.tablestore.model.PrimaryKey;
import com.alicloud.openservices.tablestore.model.search.SearchQuery;

import zyxhj.core.domain.Car;
import zyxhj.core.domain.Inquire;
import zyxhj.core.repository.CarRepository;
import zyxhj.core.repository.InquireRepository;
import zyxhj.jiti.domain.Advert;
import zyxhj.jiti.repository.AdvertRepository;
import zyxhj.utils.IDUtils;
import zyxhj.utils.Singleton;
import zyxhj.utils.api.ServerException;
import zyxhj.utils.data.ts.ColumnBuilder;
import zyxhj.utils.data.ts.PrimaryKeyBuilder;
import zyxhj.utils.data.ts.TSQL;
import zyxhj.utils.data.ts.TSQL.OP;
import zyxhj.utils.data.ts.TSRepository;

public class SortService {

	private static Logger log = LoggerFactory.getLogger(SortService.class);

	private AdvertRepository advertRepository;
	private CarRepository carRepository;
	private InquireRepository inquireRepository;

	public SortService() {
		try {

			advertRepository = Singleton.ins(AdvertRepository.class);
			carRepository = Singleton.ins(CarRepository.class);
			inquireRepository = Singleton.ins(InquireRepository.class);
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
	public Car createCar(SyncClient client, String title, String content, String province, String departure,
			String destination, String city, String region, String pos, Byte type, String tags) throws Exception {
		Car c = new Car();
		long id = IDUtils.getSimpleId();
		c._id = IDUtils.simpleId2Hex(id).substring(0, 4);
		c.id = id;

		c.title = title;
		c.content = content;
		c.province = province;
		c.city = city;
		c.region = region;
		c.departure = departure;
		c.destination = destination;
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
			String departure, String destination, String city, String region, String pos, Byte type, String tags)
			throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("_id", id).add("id", carId).build();
		ColumnBuilder cb = new ColumnBuilder();
		cb.add("title", title);
		cb.add("content", content);
		cb.add("province", province);
		cb.add("city", city);
		cb.add("region", region);
		cb.add("departure", departure);
		cb.add("destination", destination);
		cb.add("pos", pos);
		cb.add("type", (long) type);
		cb.add("tags", tags);
		cb.add("status", (long) Car.STATUS.NOTFIND.v());
		List<Column> columns = cb.build();
		TSRepository.nativeUpdate(client, carRepository.getTableName(), pk, columns);
	}

	// 获取打车信息 根据条件获取
	public JSONObject getCars(SyncClient client, Byte status, Byte type, String region, Integer count, Integer offset)
			throws Exception {
		TSQL ts = new TSQL();
		ts.Term(OP.AND, "status", (long) status).Term(OP.AND, "type", (long) type);
		if (region != null) {
			ts.Term(OP.AND, "region", region);
		}
		ts.setLimit(count);
		ts.setOffset(offset);
		ts.setGetTotalCount(true);
		SearchQuery query = ts.build();
		return TSRepository.nativeSearch(client, carRepository.getTableName(), "CarInfoIndex", query);
	}

	// 根据输入的地址查询打车信息
	public JSONObject getCarByAdress(SyncClient client, String departure, String destination, Integer count,
			Integer offset) throws Exception {
		TSQL ts = new TSQL();
		ts.Term(OP.AND, "departure", departure).Term(OP.AND, "destination", destination);
		ts.setLimit(count);
		ts.setOffset(offset);
		ts.setGetTotalCount(true);

		SearchQuery query = ts.build();
		return TSRepository.nativeSearch(client, carRepository.getTableName(), "CarInfoIndex", query);
	}

	// 创建打听
	public Inquire createInquire(SyncClient client, String title, String content, String province, Byte type,
			String city, String region, String tags) throws Exception {
		Inquire in = new Inquire();
		long id = IDUtils.getSimpleId();
		in._id = IDUtils.simpleId2Hex(id).substring(0, 4);
		in.id = id;

		in.title = title;
		in.content = content;
		in.type = (long) type;
		in.province = province;
		in.city = city;
		in.region = region;
		in.tags = tags;
		in.time = new Date();

		inquireRepository.insert(client, in, false);
		return in;
	}

	public JSONObject getInquire(SyncClient client, String region, Byte type, Integer count, Integer offset)
			throws Exception {
		TSQL ts = new TSQL();
		ts.Term(OP.AND, "region", region).Term(OP.AND, "type", type);
		ts.setLimit(count);
		ts.setOffset(offset);
		ts.setGetTotalCount(true);
		SearchQuery query = ts.build();
		return TSRepository.nativeSearch(client, inquireRepository.getTableName(), "InquireInfoIndex", query);
	}

	public void delInquire(SyncClient client, Long id, Long inquireId) throws Exception {
		PrimaryKey pk = new PrimaryKeyBuilder().add("_id", id).add("id", inquireId).build();
		TSRepository.nativeDel(client, inquireRepository.getTableName(), pk);
	}

}
