package zyxhj.jiti.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import zyxhj.jiti.domain.PrintingType;

/**
 * 第三方用户自定义角色service
 *
 */
public class PrintingTypeService {

	private static Logger log = LoggerFactory.getLogger(PrintingTypeService.class);

//	private static Cache<Long, ORGUserRole> PRINTING_TYPE_ALL = CacheBuilder.newBuilder()//
//			.expireAfterAccess(5, TimeUnit.MINUTES)//
//			.maximumSize(1000)//
//			.build();

	private static HashMap<Long, PrintingType> ORG_PRINTING_TYPE_MAP = new HashMap<>();
	private static HashMap<Long, PrintingType> USER_PRINTING_TYPE_MAP = new HashMap<>();
	public static ArrayList<PrintingType> ORG_PRINTING_TYPE_LIST = new ArrayList<>();
	public static ArrayList<PrintingType> USER_PRINTING_TYPE_LIST = new ArrayList<>();
	public static JSONObject data = new JSONObject();

	static {
		ORG_PRINTING_TYPE_MAP.put(PrintingType.org_name.printingId, PrintingType.org_name);
		ORG_PRINTING_TYPE_MAP.put(PrintingType.org_address.printingId, PrintingType.org_address);
		ORG_PRINTING_TYPE_MAP.put(PrintingType.org_create_time.printingId, PrintingType.org_create_time);
		ORG_PRINTING_TYPE_MAP.put(PrintingType.org_code.printingId, PrintingType.org_code);

		USER_PRINTING_TYPE_MAP.put(PrintingType.share_cer_no.printingId, PrintingType.share_cer_no);
		USER_PRINTING_TYPE_MAP.put(PrintingType.family_master.printingId, PrintingType.family_master);
		USER_PRINTING_TYPE_MAP.put(PrintingType.org_user_address.printingId, PrintingType.org_user_address);
		USER_PRINTING_TYPE_MAP.put(PrintingType.org_user_name.printingId, PrintingType.org_user_name);
		USER_PRINTING_TYPE_MAP.put(PrintingType.org_user.printingId, PrintingType.org_user);
		USER_PRINTING_TYPE_MAP.put(PrintingType.id_number.printingId, PrintingType.id_number);
		USER_PRINTING_TYPE_MAP.put(PrintingType.share_amount.printingId, PrintingType.share_amount);
		USER_PRINTING_TYPE_MAP.put(PrintingType.remark.printingId, PrintingType.remark);

		Iterator<PrintingType> orgPrinting = ORG_PRINTING_TYPE_MAP.values().iterator();
		while (orgPrinting.hasNext()) {
			ORG_PRINTING_TYPE_LIST.add(orgPrinting.next());
		}
		data.put("orgInfo", ORG_PRINTING_TYPE_LIST);
		Iterator<PrintingType> userPrinting = USER_PRINTING_TYPE_MAP.values().iterator();
		while (userPrinting.hasNext()) {
			USER_PRINTING_TYPE_LIST.add(userPrinting.next());
		}
		data.put("userInfo", USER_PRINTING_TYPE_LIST);

	}

}
