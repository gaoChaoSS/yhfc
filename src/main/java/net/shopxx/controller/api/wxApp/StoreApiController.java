package net.shopxx.controller.api.wxApp;

import com.fasterxml.jackson.annotation.JsonView;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.*;
import net.shopxx.service.*;
import net.shopxx.util.DateUtils;
import net.shopxx.util.JsonUtils;
import net.shopxx.util.MapUtil;
import net.shopxx.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 小程序首页banner
 *
 * @Auther: Demaxiya
 * @Create: 2019-07-08 09:58
 */
@Controller("wxAppStoreController")
@RequestMapping("/api/wxapp/store")
public class StoreApiController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(StoreApiController.class);

    @Inject
    private AdService adService;

    @Inject
    private StoreService storeService;

    @Inject
    private AreaService areaService;

    @Inject
    private MemberService memberService;

    @Inject
    private CartService cartService;

    /**
     * 获取所有门店广告信息
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 11:09
     */
    @GetMapping(path = "/getBanner", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getBanner(HttpServletRequest request) {
        try {
            //  查询广告信息
            List<Ad> datas = adService.findAll();
            List<Map<String, Object>> result = new ArrayList<>();
            if (datas.size() > 0) {
                for (Ad item : datas) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("adId", item.getId());
                    map.put("adTitle", item.getTitle());
                    map.put("adPath", item.getPath());
                    map.put("adBeginDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getBeginDate()));
                    map.put("adEndDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(item.getEndDate()));
                    map.put("adLinkUrl", item.getUrl());
                    map.put("order", item.getOrder() == null ? 0 : item.getOrder());
                    result.add(map);
                }
            }
            Collections.sort(result, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Integer name1 = (Integer) (o1.get("order"));
                    Integer name2 = (Integer) (o2.get("order"));
                    return name1.compareTo(name2);
                }
            });
            logger.info("获取所有门店广告信息,{} ", JsonUtils.toJson(result));
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功！", result));
        } catch (Exception e) {
            logger.error("获取所有门店广告信息失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 获取门店详情
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 14:53
     */
    @GetMapping(path = "/getStoreInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getStoreInfo(HttpServletRequest request) {
        try {
            // 获取参数
            String id = request.getParameter("storeId");
            // 查询门店信息
            Store store = storeService.find(Long.parseLong(id));
            Business b = store.getBusiness();
            Map<String, Object> data = new HashMap<>();
            if (store != null) {
                data.put("storeId", store.getId());          // ID
                data.put("storeName", store.getName());      // 门店名称
                data.put("businessName", b.getAttributeValue1() == null ? "" : b.getAttributeValue1()); // 商家名称
                data.put("mobile", b.getMobile());           // 电话
                data.put("storeMapImg", b.getAttributeValue3()); // 门店地图图片
                data.put("storeImg", b.getAttributeValue4());    // 门店图片
                data.put("openTime", b.getAttributeValue2() == null ? "" : b.getAttributeValue2()); // 营业时间
                data.put("businessImage", b.getAttributeValue9() == null ? "" : b.getAttributeValue9());// 店长头像
                data.put("limitTime", new SimpleDateFormat("HH:mm:ss").format(store.getLimitTime()));
                data.put("address", store.getAddress());     // 地址
                //获取门店发货说明
                String shipDesc = store.getSendDescDef();
                if (DateUtils.compareTime(store.getSpecSendTime()) && StringUtils.isNotBlank(store.getSpecSendDesc())) {
                    shipDesc = store.getSpecSendDesc();
                }

                data.put("shipDesc", shipDesc);
                data.put("isEnabled", store.getIsEnabled()); // 门店是否启用

            }
            logger.info("获取门店信息,{} ", JsonUtils.toJson(data));
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功！", data));
        } catch (Exception e) {
            logger.error("获取门店信息失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 地区列表
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 15:34
     */
    @GetMapping(path = "/areaList", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getAreaList() {
        try {
            // 查询地区
            List<Area> data = areaService.getCurrentCity();
            List<Map<String, Object>> result = new ArrayList<>();
            if (data.size() > 0) {
                for (Area a : data) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("areaId", a.getId());        // 地区ID
                    map.put("areaName", a.getName());    // 地区名称
                    result.add(map);
                }
            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功！", result));
        } catch (Exception e) {
            logger.error("获取地区信息失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 门店列表
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 18:25
     */
    @GetMapping(path = "/getStoreList", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getStoreList(HttpServletRequest request, char[] lati, char[] lngi) {
        try {
            // 解析参数
            Double lat = Double.valueOf(new String(lati));
            Double lng = Double.valueOf(new String(lngi));

            Long areaId = Long.parseLong(request.getParameter("areaId"));
            String storeName = "";
            if (!(request.getParameter("storeName") == null)) {
                byte[] aa = request.getParameter("storeName").getBytes("ISO-8859-1");
                storeName = request.getParameter("storeName");
                storeName = new String(aa, "UTF-8");
            }

            List<Map<String, Object>> result = new ArrayList<>();

            // 默认显示列表
            Area area = areaService.find(areaId);
            if ((request.getParameter("storeName")) == null) {
                Set<Area> areas = area.getChildren();
                for (Area a : areas) {
                    Set<Store> stores = a.getStores();
                    for (Store s : stores) {
                        if (s.getStatus() == Store.Status.SUCCESS && s.getIsEnabled()) {
                            Map<String, Object> item = new HashMap<>();
                            Business b = s.getBusiness();
                            item.put("storeId", s.getId());
                            item.put("businessName", b.getUsername());
                            item.put("businessMobile", b.getMobile());
                            item.put("limitTime", new SimpleDateFormat("HH:mm:ss").format(s.getLimitTime()));
                            item.put("storeAddress", s.getAddress());
                            item.put("openTime", b.getAttributeValue2() == null ? "" : b.getAttributeValue2()); // 营业时间
                            item.put("distance", MapUtil.getDistance(lat + "," + lng, s.getLatitude() + "," + s.getLongitude()));
                            item.put("storeName", s.getName());
                            result.add(item);
                        }
                    }
                }
                Collections.sort(result, new Comparator<Map<String, Object>>() {
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Double name1 = (Double) (o1.get("distance"));
                        Double name2 = (Double) (o2.get("distance"));
                        return name1.compareTo(name2);
                    }
                });
                return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功！", result));
            }
            Set<Area> areas = area.getChildren();
            for (Area a : areas) {
                Set<Store> stores = a.getStores();
                for (Store s : stores) {
                    if (s.getStatus() == Store.Status.SUCCESS && s.getIsEnabled()) {
                        if (s.getName().indexOf(storeName) > -1) {
                            Map<String, Object> item = new HashMap<>();
                            Business b = s.getBusiness();
                            item.put("storeId", s.getId());
                            item.put("businessName", b.getUsername());
                            item.put("businessMobile", b.getMobile());
                            item.put("limitTime", new SimpleDateFormat("HH:mm:ss").format(s.getLimitTime()));
                            item.put("openTime", b.getAttributeValue2() == null ? "" : b.getAttributeValue2()); // 营业时间
                            item.put("storeAddress", s.getAddress());
                            item.put("distance", MapUtil.getDistance(lat + "," + lng, s.getLatitude() + "," + s.getLongitude()));
                            item.put("storeName", s.getName());
                            result.add(item);
                        }
                    }
                }
            }


            Collections.sort(result, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Double name1 = (Double) (o1.get("distance"));
                    Double name2 = (Double) (o2.get("distance"));
                    return name1.compareTo(name2);
                }
            });

            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功！", result));

//            // 查询满足条件的门店
//            List<Store> data=storeService.findList(areaId,storeName);
//            // 数据组装
//            if(data.size()>0){
//                for(Store s :data) {
//                    Map<String, Object> item = new HashMap<>();
//                    Business b=s.getBusiness();
//                    item.put("storeId", s.getId());
//                    item.put("businessName",b.getUsername());
//                    item.put("businessMobile",b.getMobile());
//                    item.put("limitTime",new SimpleDateFormat("HH:mm:ss").format(s.getLimitTime()));
//                    item.put("storeAddress",s.getAddress());
//                    item.put("storeName",s.getName());
//                    result.add(item);
//                }
//            }
        } catch (Exception e) {
            logger.error("获取门店列表信息失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


    /**
     * 查询当前用户距离最近的门店
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 21:39
     */
    @GetMapping(path = "/getMinLengthStore", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getMinLengthStore(HttpServletRequest request, char[] lati, char[] lngi) {
        try {
            // 根据用户坐标获取经纬度最大最小范围
            Double lat = Double.valueOf(new String(lati));
            Double lng = Double.valueOf(new String(lngi));
//            double lat=30.59199;
//            double lng=104.06052;
            // 范围距离，后期会调整
            Double scope = 1.0;
            Map<String, Double> scopes = MapUtil.getMinAndMax(lng, lat, scope);
            // 查询符合条件的门店信息
            List<Store> data = storeService.findMeetStore(scopes.get("minlat"), scopes.get("maxlat"), scopes.get("minlng"), scopes.get("maxlng"));
            List<Map<String, Object>> result = new ArrayList<>();
            if (data.size() > 0) {
                for (Store s : data) {
                    if (s.getStatus() == Store.Status.SUCCESS && s.getIsEnabled()) {
                        Map<String, Object> item = new HashMap<>();
                        Business b = s.getBusiness();
                        item.put("storeId", s.getId());
                        item.put("businessName", b.getUsername());
                        item.put("businessMobile", b.getMobile());
                        item.put("limitTime", new SimpleDateFormat("HH:mm:ss").format(s.getLimitTime()));
                        item.put("storeAddress", s.getAddress());
                        item.put("storeName", s.getName());
                        item.put("distance", MapUtil.getDistance(lat + "," + lng, s.getLatitude() + "," + s.getLongitude()) + "m");

                        //获取门店发货说明
                        String shipDesc = s.getSendDescDef();
                        if (DateUtils.compareTime(s.getSpecSendTime()) && StringUtils.isNotBlank(s.getSpecSendDesc())) {
                            shipDesc = s.getSpecSendDesc();
                        }

                        item.put("shipDesc", shipDesc);
                        result.add(item);
                    }
                }
            }
            logger.info("查询当前用户距离最近的门店,{} ", JsonUtils.toJson(result));
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", result));
        } catch (Exception e) {
            logger.error("查询当前用户距离最近的门店失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 根据地址查询经纬度
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-08 21:32
     */
    @GetMapping(path = "/getLongitudeAndLatitude", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getLongitudeAndLatitude(HttpServletRequest request) {
        try {
            // 解析参数
            byte[] a = request.getParameter("address").getBytes("ISO-8859-1");
            String address = new String(a, "UTF-8");
            String key = "MWDBZ-APEKD-F4Q4Y-P3K5N-FXCA7-SLBUB";       // 腾讯地图密钥 自己申请 要配置IP白名单
            // 请求腾讯地图
            JSONObject cityJson = MapUtil.geocoderAddress(address, key);
            if (!cityJson.getString("status").equals("0")) {
                // 获取异常
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, cityJson.getString("message")));
            }

            // 获取坐标
            JSONObject cityJsonLocation = cityJson.getJSONObject("result").getJSONObject("location");
            String lat = cityJsonLocation.getString("lat");         // 纬度
            String lng = cityJsonLocation.getString("lng");         // 经度
            Map<String, String> data = new HashMap<>();
            data.put("lat", lat);
            data.put("lng", lng);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", data));
        } catch (Exception e) {
            logger.error("根据地址查询经纬度失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 门店选择
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-09 19:11
     */
    @GetMapping(path = "/chooseStore", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object chooseStore(String storeId, String sessionId, HttpServletRequest request) {
        try {
            Store store = storeService.find(Long.parseLong(storeId));
            Member member = this.getCurrentWxInfo(request);
            member.setBuyStore(store);
            memberService.update(member);
            Store s = member.getBuyStore();
            // 删除购物车
            Cart cart = member.getCart();
            if (null != cart) {
                cartService.delete(cart);
            }
            // 数据返回
            Map<String, Object> data = new HashMap<>();
            if (s == null) {
                data.put("isStore", 0);
            } else {
                data.put("storeId", store.getId());
                data.put("storeName", store.getName());
                data.put("storeAddress", store.getAddress());
                data.put("isStore", 1);
                //获取门店发货说明
                String shipDesc = s.getSendDescDef();
                if (DateUtils.compareTime(s.getSpecSendTime()) && StringUtils.isNotBlank(s.getSpecSendDesc())) {
                    shipDesc = s.getSpecSendDesc();
                }

                data.put("shipDesc", shipDesc);
                data.put("isEnabled", store.getIsEnabled()); // 门店是否启用

            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "选择成功！", data));
        } catch (Exception e) {
            logger.error("门店选择失败, ", e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


}
