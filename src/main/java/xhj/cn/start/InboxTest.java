package xhj.cn.start;

import java.util.ArrayList;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.CapacityUnit;
import com.alicloud.openservices.tablestore.model.CreateTableRequest;
import com.alicloud.openservices.tablestore.model.DefinedColumnSchema;
import com.alicloud.openservices.tablestore.model.DefinedColumnType;
import com.alicloud.openservices.tablestore.model.IndexMeta;
import com.alicloud.openservices.tablestore.model.PrimaryKeySchema;
import com.alicloud.openservices.tablestore.model.PrimaryKeyType;
import com.alicloud.openservices.tablestore.model.ReservedThroughput;
import com.alicloud.openservices.tablestore.model.TableMeta;
import com.alicloud.openservices.tablestore.model.TableOptions;

//https://ZeroStore.cn-hangzhou.ots.aliyuncs.com
//LTAIJ9mYIjuW54Cj
//89EMlXLsP13H8mWKIvdr4iM1OvdVxs

public class InboxTest {

	public static void main(String[] args) {
		SyncClient client = new SyncClient("https://ZeroStore.cn-hangzhou.ots.aliyuncs.com", "LTAIJ9mYIjuW54Cj",
				"89EMlXLsP13H8mWKIvdr4iM1OvdVxs", "ZeroStore");

		createTable(client);
	}

	private static void createTable(SyncClient client) {

		TableMeta tableMeta = new TableMeta("TestTable");
		tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema("partId", PrimaryKeyType.STRING)); // 为主表添加主键列。
		tableMeta.addPrimaryKeyColumn(new PrimaryKeySchema("userId", PrimaryKeyType.INTEGER)); // 为主表添加主键列。

		tableMeta.addDefinedColumn(new DefinedColumnSchema("title", DefinedColumnType.STRING)); // 为主表添加预定义列。
		tableMeta.addDefinedColumn(new DefinedColumnSchema("content", DefinedColumnType.INTEGER)); // 为主表添加预定义列。

		ArrayList<IndexMeta> indexMetas = new ArrayList<IndexMeta>();
		IndexMeta indexMeta = new IndexMeta("IndexInbox");
		indexMeta.addPrimaryKeyColumn("title"); // 为索引表添加主键列。
		indexMeta.addDefinedColumn("content"); // 为索引表添加属性列。
		indexMetas.add(indexMeta);

		int timeToLive = -1; // 数据的过期时间，单位秒, -1代表永不过期，例如设置过期时间为一年, 即为 365 * 24 * 3600。
		int maxVersions = 1; // 保存的最大版本数，设置为3即代表每列上最多保存3个最新的版本。
		TableOptions tableOptions = new TableOptions(timeToLive, maxVersions);
		CreateTableRequest request = new CreateTableRequest(tableMeta, tableOptions, indexMetas);

		request.setReservedThroughput(new ReservedThroughput(new CapacityUnit(0, 0))); // 设置读写预留值，容量型实例只能设置为0，高性能实例可以设置为非零值。
		client.createTable(request);
	}

}
