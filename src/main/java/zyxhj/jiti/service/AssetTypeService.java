package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import zyxhj.jiti.domain.AssetType;
import zyxhj.jiti.domain.ORGUserRole;

/**
 * 第三方用户自定义角色service
 *
 */
public class AssetTypeService {

	private static Logger log = LoggerFactory.getLogger(AssetTypeService.class);

	private static Cache<Long, ORGUserRole> ASSET_TYPE_ALL = CacheBuilder.newBuilder()//
			.expireAfterAccess(5, TimeUnit.MINUTES)//
			.maximumSize(1000)//
			.build();

	/**
	 * 系统级第三方权限，会被
	 */
	private static HashMap<Long, AssetType> ASSET_TYPE_MAP = new HashMap<>();
	private static HashMap<Long, AssetType> RES_TYPE_MAP = new HashMap<>();
	private static HashMap<Long, AssetType> BUSINESS_TYPE_MAP = new HashMap<>();

	public static ArrayList<AssetType> ASSET_TYPE_LIST = new ArrayList<>();
	public static ArrayList<AssetType> RES_TYPE_LIST = new ArrayList<>();
	public static ArrayList<AssetType> BUSINESS_TYPE_LIST = new ArrayList<>();

	static {
		
		//添加assetType,resType,bussinessType类型到类型系统中
		ASSET_TYPE_MAP.put(AssetType.assetType_movables.typeId, AssetType.assetType_movables);
		ASSET_TYPE_MAP.put(AssetType.assetType_immovables.typeId, AssetType.assetType_immovables);
		
		RES_TYPE_MAP.put(AssetType.resType_woodland.typeId, AssetType.resType_woodland);
		RES_TYPE_MAP.put(AssetType.resType_plough.typeId, AssetType.resType_plough);
		RES_TYPE_MAP.put(AssetType.resType_ntslss.typeId, AssetType.resType_ntslss);
		RES_TYPE_MAP.put(AssetType.mountain_pond.typeId, AssetType.mountain_pond);
		RES_TYPE_MAP.put(AssetType.resType_ditch.typeId, AssetType.resType_ditch);
		RES_TYPE_MAP.put(AssetType.pump_house.typeId, AssetType.pump_house);
		RES_TYPE_MAP.put(AssetType.resType_highway.typeId, AssetType.resType_highway);
		RES_TYPE_MAP.put(AssetType.house_vacant.typeId, AssetType.house_vacant);
		RES_TYPE_MAP.put(AssetType.office_room.typeId, AssetType.office_room);
		
		BUSINESS_TYPE_MAP.put(AssetType.businessType_rent.typeId, AssetType.businessType_rent);
		BUSINESS_TYPE_MAP.put(AssetType.businessType_ownchoice.typeId, AssetType.businessType_ownchoice);
		BUSINESS_TYPE_MAP.put(AssetType.businessType_idle.typeId, AssetType.businessType_idle);
		BUSINESS_TYPE_MAP.put(AssetType.public_welfare.typeId, AssetType.public_welfare);
		
		
		// 添加admin，member，股东，董事，监事等角色到系统中
		

		Iterator<AssetType> asset = ASSET_TYPE_MAP.values().iterator();
		while (asset.hasNext()) {
			ASSET_TYPE_LIST.add(asset.next());
		}
		
		Iterator<AssetType> res = RES_TYPE_MAP.values().iterator();
		while (res.hasNext()) {
			RES_TYPE_LIST.add(res.next());
		}
		
		Iterator<AssetType> business = BUSINESS_TYPE_MAP.values().iterator();
		while (business.hasNext()) {
			BUSINESS_TYPE_LIST.add(business.next());
		}
	}

	//private ORGUserRoleRepository orgUserRoleRepository;

	public AssetTypeService() {
		try {
		//	orgUserRoleRepository = Singleton.ins(ORGUserRoleRepository.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}


}
