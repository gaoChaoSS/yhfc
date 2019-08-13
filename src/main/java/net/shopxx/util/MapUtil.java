package net.shopxx.util;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MapUtil {
    private final static Logger logger = LoggerFactory.getLogger(MapUtil.class);

    /**
     * 根据经纬度解析地址
     *
     * @param location location=lat<纬度>,lng<经度>
     * @param key      地图Key
     * @Auther: Demaxiya
     * @Create: 2019-07-08 20:17
     */
    public static JSONObject geocoderLatLng(String location, String key) {
        String url = "https://apis.map.qq.com/ws/geocoder/v1/?location=$Location&key=$Key&get_poi=0";
        url = url.replace("$Location", location).replace("$Key", key);
        String result = HttpUtils.httpsRequest(url, "GET", null);
        return JSONObject.fromObject(result);
    }

    /**
     * 地址解析 - 地址转坐标
     */
    public static JSONObject geocoderAddress(String address, String key) {
        String url = "https://apis.map.qq.com/ws/geocoder/v1/?address=$Address&key=$Key";
        url = url.replace("$Address", address).replace("$Key", key);

        String result = HttpUtils.httpsRequest(url, "GET", null);
        return JSONObject.fromObject(result);
    }

    /**
     * 根据地址查询经纬度
     */
    public static Map<String, Double> getLatAndLng(String address, String key) {
        String url = "https://apis.map.qq.com/ws/geocoder/v1/?address=$Address&key=$Key";
        url = url.replace("$Address", address).replace("$Key", key);

        String result = HttpUtils.httpsRequest(url, "GET", null);
        JSONObject data = JSONObject.fromObject(result);
        Map<String, Double> dataChang = new HashMap<>();
        if (data.getString("status").equals("0")) {
            JSONObject cityJsonLocation = data.getJSONObject("result").getJSONObject("location");
            String lat = cityJsonLocation.getString("lat");         // 纬度
            String lng = cityJsonLocation.getString("lng");         // 经度
            dataChang.put("lat", Double.valueOf(lat));
            dataChang.put("lng", Double.valueOf(lng));
        }
        return dataChang;
    }

    /**
     * 根据经纬度获取城市Code信息
     */
    public static Map<String, String> getCityCodeInfo(String location, String key) {
        JSONObject mapJson = MapUtil.geocoderLatLng(location, key);
        if (!mapJson.getString("status").equals("0")) {
            // 请求失败
            Map<String, String> resultMap = new HashedMap();
            resultMap.put("result", "0");
            resultMap.put("message", mapJson.getString("message").toString());
            return resultMap;
        }

        // 解析城市信息
        JSONObject cityJson = mapJson.getJSONObject("result").getJSONObject("ad_info");

        String cityCode = cityJson.getString("city_code");      // 城市Code
        String cityName = cityJson.getString("city");           // 城市名称

        // 返回组装
        Map<String, String> resultMap = new HashedMap();
        resultMap.put("result", "0");
        resultMap.put("cityCode", cityCode);
        resultMap.put("cityName", cityName);
        return resultMap;
    }

    /**
     * 根据给定的经纬度以及范围 查询对应范围的经纬度
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 20:57
     */
    public static Map<String, Double> getMinAndMax(Double longitude, Double latitude, Double scope) {
        Map<String, Double> result = new HashMap<>();
        // 先计算查询点的经纬度范围
        double r = 6371;                      // 地球半径 单位千米
        double dis = scope;                 // 给定范围 单位千米
        double dlng = 2 * Math.asin(Math.sin(dis / (2 * r)) / Math.cos(latitude * Math.PI / 180));
        dlng = dlng * 180 / Math.PI;//角度转为弧度
        double dlat = dis / r;
        dlat = dlat * 180 / Math.PI;
        double minlat = latitude - dlat;       // 最小纬度
        double maxlat = latitude + dlat;      // 最大纬度
        double minlng = longitude - dlng;    // 最小经度
        double maxlng = longitude + dlng;   // 最大经度
        // 返回数据
        result.put("minlat", minlat);
        result.put("maxlat", maxlat);
        result.put("minlng", minlng);
        result.put("maxlng", maxlng);
        return result;
    }

    /**
     * 计算两点之间的距离 单位 米
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-10 17:03
     */
    public static Double getDistance(String from, String to) {

        Double distance = 5000.00;
        try {
            String url = "https://apis.map.qq.com/ws/distance/v1/?parameters?mode=walking&from=$from&to=$to&key=MWDBZ-APEKD-F4Q4Y-P3K5N-FXCA7-SLBUB";
            url = url.replace("$from", from).replace("$to", to);
            String result = HttpUtils.httpsRequest(url, "GET", null);
            JSONObject data = JSONObject.fromObject(result);
            if (data == null) {
                return distance;
            }
            if (data.getJSONObject("result") != null && data.getJSONObject("result").get("elements") != null) {
                JSONArray a = (JSONArray) data.getJSONObject("result").get("elements");
                if (a != null && a.get(0) != null && ((JSONObject) a.get(0)).getString("distance") != null) {
                    return new Double(((JSONObject) a.get(0)).getString("distance"));
                }


            }

        } catch (Exception e) {

            logger.error("计算距离失败", e);

        }

        return distance;
    }


}
