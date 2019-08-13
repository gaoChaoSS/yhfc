/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: JRXubIo4yu6533rBuQIfkBztapACiBCh
 */
package net.shopxx.controller.admin;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import net.shopxx.entity.*;
import net.shopxx.service.*;
import net.shopxx.util.MapUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.shopxx.Pageable;
import net.shopxx.Results;

/**
 * Controller - 店铺
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminStoreController")
@RequestMapping("/admin/store")
public class StoreController extends BaseController {

    @Inject
    private StoreService storeService;
    @Inject
    private BusinessService businessService;
    @Inject
    private StoreRankService storeRankService;
    @Inject
    private StoreCategoryService storeCategoryService;
    @Inject
    private ProductCategoryService productCategoryService;
    @Inject
    private AreaService areaService;

    @Inject
    private ProductService productService;

    /**
     * 检查名称是否唯一
     */
    @GetMapping("/check_name")
    public @ResponseBody
    boolean checkName(Long id, String name) {
        return StringUtils.isNotEmpty(name) && storeService.nameUnique(id, name);
    }

    /**
     * 商家选择
     */
    @GetMapping("/business_select")
    public ResponseEntity<?> businessSelect(String keyword, Integer count) {
        List<Map<String, Object>> data = new ArrayList<>();
        if (StringUtils.isEmpty(keyword)) {
            return ResponseEntity.ok(data);
        }
        List<Business> businesses = businessService.search(keyword, count);
        for (Business businesse : businesses) {
            if (businesse.getStore() == null) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put("id", businesse.getId());
                item.put("username", businesse.getUsername());
                data.add(item);
            }
        }
        return ResponseEntity.ok(data);
    }

    /**
     * 查看
     */
    @GetMapping("/view")
    public String view(Long id, ModelMap model) {
        Store store = storeService.find(id);
        model.addAttribute("store", store);
        model.addAttribute("productCategoryRoots", productCategoryService.findRoots());
        model.addAttribute("allowedProductCategories", productCategoryService.findList(store, null, null, null));
        model.addAttribute("allowedProductCategoryParents", getAllowedProductCategoryParents(store));
        return "admin/store/view";
    }

    /**
     * 添加
     */
    @GetMapping("/add")
    public String add(ModelMap model) {
        model.addAttribute("types", Store.Type.values());
        model.addAttribute("storeRanks", storeRankService.findAll());
        model.addAttribute("storeCategories", storeCategoryService.findAll());
        model.addAttribute("productCategoryTree", productCategoryService.findTree());
        return "admin/store/add";
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public ResponseEntity<?> save(Store store, Long businessId, Long storeRankId, Long storeCategoryId, Long[] productCategoryIds, Long areaId) {
        store.setBusiness(businessService.find(businessId));
        store.setStoreRank(storeRankService.find(storeRankId));
        store.setStoreCategory(storeCategoryService.find(storeCategoryId));
        // 组装地址
        Area area = areaService.find(areaId);
        String address = area.getFullName() + store.getAddress();
        // 调用腾讯地图查询经纬度
        Map<String, Double> ll = MapUtil.getLatAndLng(address, "MWDBZ-APEKD-F4Q4Y-P3K5N-FXCA7-SLBUB");
        if (ll != null && ll.size() > 0) {
            store.setLatitude(ll.get("lat"));
            store.setLongitude(ll.get("lng"));
        } else {//默认到成都
            store.setLatitude(30.5763307666);
            store.setLongitude(104.0712219292);
        }
        store.setArea(areaService.find(areaId));
        store.setProductCategories(new HashSet<>(productCategoryService.findList(productCategoryIds)));
        store.setStatus(Store.Status.PENDING);
        store.setEndDate(new Date());
        store.setBailPaid(BigDecimal.ZERO);
        store.setStoreAdImages(null);
        store.setInstantMessages(null);
        store.setStoreProductCategories(null);
        store.setCategoryApplications(null);
        store.setStoreProductTags(null);
        store.setProducts(null);
        store.setPromotions(null);
        store.setCoupons(null);
        store.setStorePluginStatus(null);
        store.setOrders(null);
        store.setStoreFavorites(null);
        store.setDeliveryTemplates(null);
        store.setDeliveryCenters(null);
        store.setDefaultFreightConfigs(null);
        store.setAreaFreightConfigs(null);
        store.setSvcs(null);
        store.setPaymentTransactions(null);
        store.setConsultations(null);
        store.setReviews(null);
        store.setStatistics(null);
        Date now = new Date();
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(now);
        cal1.set(Calendar.HOUR_OF_DAY, 22);
        cal1.set(Calendar.MINUTE, 00);
        cal1.set(Calendar.SECOND, 00);
        store.setLimitTime(cal1.getTime());
        if (storeService.nameExists(store.getName())) {
            return Results.UNPROCESSABLE_ENTITY;
        }
        if (!isValid(store, BaseEntity.Save.class)) {
            return Results.UNPROCESSABLE_ENTITY;
        }


        Set<Product> set = new HashSet<Product>();
        List<Product> products = productService.findBindAllStore();
        set.addAll(products);
        store.setProducts(set);


        storeService.save(store);
        storeService.review(store, true, null);
        return Results.OK;
    }

    /**
     * 编辑
     */
    @GetMapping("/edit")
    public String edit(Long id, ModelMap model) {
        model.addAttribute("store", storeService.find(id));
        model.addAttribute("types", Store.Type.values());
        model.addAttribute("storeRanks", storeRankService.findAll());
        model.addAttribute("storeCategories", storeCategoryService.findAll());
        model.addAttribute("productCategoryTree", productCategoryService.findTree());
        return "admin/store/edit";
    }

    /**
     * 更新
     */
    @PostMapping("/update")
    public ResponseEntity<?> update(Store store, Long id, Long storeRankId, Long storeCategoryId, Long[] productCategoryIds, Long areaId, String limitTimeStr) {
        if (!storeService.nameUnique(id, store.getName())) {
            return Results.UNPROCESSABLE_ENTITY;
        }
        Store pStore = storeService.find(id);
        pStore.setName(store.getName());
        pStore.setLogo(store.getLogo());
        pStore.setEmail(store.getEmail());
        pStore.setMobile(store.getMobile());
        pStore.setPhone(store.getPhone());
        pStore.setAddress(store.getAddress());
        pStore.setZipCode(store.getZipCode());
        pStore.setIntroduction(store.getIntroduction());
        pStore.setKeyword(store.getKeyword());
        pStore.setEndDate(store.getEndDate());
        pStore.setIsEnabled(store.getIsEnabled());
        pStore.setStoreRank(storeRankService.find(storeRankId));
        pStore.setStoreCategory(storeCategoryService.find(storeCategoryId));
        pStore.setProductCategories(new HashSet<>(productCategoryService.findList(productCategoryIds)));


        pStore.setSendDescDef(store.getSendDescDef());
        pStore.setSpecSendDesc(store.getSpecSendDesc());
        pStore.setSpecSendTime(store.getSpecSendTime());

        // 组装地址
        Area area = areaService.find(areaId);
        String address = area.getFullName() + pStore.getAddress();
        // 调用腾讯地图查询经纬度
        Map<String, Double> ll = MapUtil.getLatAndLng(address, "MWDBZ-APEKD-F4Q4Y-P3K5N-FXCA7-SLBUB");
        if (ll != null && ll.size() > 0) {
            pStore.setLatitude(ll.get("lat"));
            pStore.setLongitude(ll.get("lng"));
        } else {//默认到成都
            store.setLatitude(30.5763307666);
            store.setLongitude(104.0712219292);
        }
        pStore.setArea(areaService.find(areaId));
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date limitTime = formatter.parse(limitTimeStr, pos);
        pStore.setLimitTime(limitTime);

        if (!isValid(pStore, BaseEntity.Update.class)) {
            return Results.UNPROCESSABLE_ENTITY;
        }
//		if(store.getIsEnabled()){
//			Set<Product> set = new HashSet<Product>();
//			List<Product> products=productService.findBindAllStore();
//			set.addAll(products);
//			store.setProducts(set);
//		}
        storeService.update(pStore);
        return Results.OK;
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    public String list(Store.Type type, Store.Status status, Boolean isEnabled, Boolean hasExpired, Pageable pageable, ModelMap model) {
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        model.addAttribute("isEnabled", isEnabled);
        model.addAttribute("hasExpired", hasExpired);
        model.addAttribute("page", storeService.findPage(type, status, isEnabled, hasExpired, pageable));
        return "admin/store/list";
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    public ResponseEntity<?> delete(Long[] ids) {
        if (ids != null) {
            for (Long id : ids) {
                Store store = storeService.find(id);
                if (store != null && Store.Status.SUCCESS.equals(store.getStatus())) {
                    return Results.unprocessableEntity("admin.store.deleteSuccessNotAllowed", store.getName());
                }
            }
            storeService.delete(ids);
        }
        return Results.OK;
    }

    /**
     * 审核
     */
    @GetMapping("/review")
    public String review(Long id, ModelMap model) {
        Store store = storeService.find(id);
        model.addAttribute("store", store);
        model.addAttribute("productCategoryRoots", productCategoryService.findRoots());
        model.addAttribute("allowedProductCategories", productCategoryService.findList(store, null, null, null));
        model.addAttribute("allowedProductCategoryParents", getAllowedProductCategoryParents(store));
        return "admin/store/review";
    }

    /**
     * 审核
     */
    @PostMapping("/review")
    public ResponseEntity<?> review(Long id, Boolean passed, String content) {
        Store store = storeService.find(id);
        if (store == null || !Store.Status.PENDING.equals(store.getStatus()) || passed == null || (!passed && StringUtils.isEmpty(content))) {
            return Results.UNPROCESSABLE_ENTITY;
        }
        storeService.review(store, passed, content);
        return Results.OK;
    }

    /**
     * 获取允许发布商品分类上级分类
     *
     * @param store 店铺
     * @return 允许发布商品分类上级分类
     */
    private Set<ProductCategory> getAllowedProductCategoryParents(Store store) {
        Assert.notNull(store, "[Assertion failed] - store is required; it must not be null");

        Set<ProductCategory> result = new HashSet<>();
        List<ProductCategory> allowedProductCategories = productCategoryService.findList(store, null, null, null);
        for (ProductCategory allowedProductCategory : allowedProductCategories) {
            result.addAll(allowedProductCategory.getParents());
        }
        return result;
    }

}