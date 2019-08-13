package net.shopxx.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * Json工具类
 * 
 * @author huanghao 2017年2月6日
 * @version 1.0
 * @since 1.0 2017年2月6日
 */
public class GsonUtil {
	
	private static Gson g = null;
	
	public static Gson Initialize(){
		if(g==null){
			g = new Gson();
		}
		return g;
	}

	/**
	 * 将 Object 转为 Json
	 * @param object
	 * @return
	 */
	public static String objectToJson(Object object){
		if(g==null){
			g = new Gson();
		}
		return g.toJson(object);
	}

	/**
	 * Map 转 Json
	 * 
	 * @param map
	 * @return
	 * @author huanghao 2017年2月6日
	 * @version 1.0
	 * @since 1.0 2017年2月6日
	 */
	public static String mapToJson(Map<String,Object> map){
		if(g==null){
			g = new Gson();
		}
		return g.toJson(map);
	}
	
	/**
	 * Map<String> 转 Json
	 * 
	 * @param map
	 * @return
	 * @author huanghao 2017年2月6日
	 * @version 1.0
	 * @since 1.0 2017年2月6日
	 */
	public static String mapToJson_String(Map<String,String> map){
		if(g==null){
			g = new Gson();
		}
		return g.toJson(map);
	}
	
	/**
	 * Json 转 Map
	 * 
	 * @param json
	 * @return
	 * @author huanghao 2017年2月6日
	 * @version 1.0
	 * @since 1.0 2017年2月6日
	 */
	public static Map<String,Object> jsonToMap(String json){
		if(g==null){
			g = new Gson();
		}
		return g.fromJson(json, new TypeToken<Map<String,Object>>(){}.getType());
	}
	
	/**
	 * Json 转 List<Map<String,Object>>
	 * 
	 * @param json
	 * @return
	 * @author huanghao 2017年2月7日
	 * @version 1.0
	 * @since 1.0 2017年2月7日
	 */
	public static List<Map<String,Object>> jsonToListMap(String json){
		if(g==null){
			g = new Gson();
		}
		return g.fromJson(json, new TypeToken<List<Map<String,Object>>>(){}.getType());
	}
	
	/**
	 * Json 转 List<Object[]>
	 * 
	 * @param json
	 * @return
	 * @author huanghao 2017年2月9日
	 * @version 1.0
	 * @since 1.0 2017年2月9日
	 */
	public static List<Object[]> jsonToListObj(String json){
		if(g==null){
			g = new Gson();
		}
		return g.fromJson(json, new TypeToken<List<Object[]>>(){}.getType());
	}
	
	/**
	 * Json 转 List<String>
	 * 
	 * @param json
	 * @return
	 * @author huanghao 2017年2月9日
	 * @version 1.0
	 * @since 1.0 2017年2月9日
	 */
	public static List<String> jsonToListStr(String json){
		if(g==null){
			g = new Gson();
		}
		return g.fromJson(json, new TypeToken<List<String>>(){}.getType());
	}

	/**
	 * List<Map<String,Object>> 转 json
	 *
	 */
	public static String listToJson(List<Map<String,Object>> data){
		if(g==null){
			g = new Gson();
		}
		return g.toJson(data);
	}



}