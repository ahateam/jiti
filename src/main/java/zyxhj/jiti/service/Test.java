package zyxhj.jiti.service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;

public class Test {
	
	public static void main(String[] args) {
//		int j = 0;
//		/*
//		 * List<Long> list = new ArrayList<Long>(); list.add((long) 123);
//		 * list.add((long) 234);
//		 * 
//		 * String a= ""; for (Long log : list) { a += log + " "; }
//		 * System.out.println(a);
//		 */
//		Map<String,Integer> map = new HashMap<String, Integer>();
//		map.put("zhangsan", j);
//		map.put("lisi", 1);
//		
//		for(int i = 0 ; i<=9;i++) {
//			map.put("zhangsan", j++);
//		}
//		System.out.println(map);
		
		String a = "[123]";
		JSONArray json = JSONArray.parseArray(a);
		json.add(234);
		// json.add(103);
		
		System.out.println(json);
		}

}
