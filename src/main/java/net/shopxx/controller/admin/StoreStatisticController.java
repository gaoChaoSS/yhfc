/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: f3QxYHlFTsB1NgyhjE1okwOrKCSrYX1g
 */
package net.shopxx.controller.admin;

import net.shopxx.ExcelView;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.entity.*;
import net.shopxx.service.OrderService;
import net.shopxx.service.StatisticService;
import net.shopxx.service.StoreService;
import net.shopxx.util.DateUtils;
import net.shopxx.util.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Controller - 店铺订单统计
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminStoreStatisticController")
@RequestMapping("/admin/store_statistic")
public class StoreStatisticController extends BaseController {

    /**
     * 默认类型
     */
    private static final Statistic.Type DEFAULT_TYPE = Statistic.Type.CREATE_ORDER_COUNT;

    /**
     * 默认周期
     */
    private static final Statistic.Period DEFAULT_PERIOD = Statistic.Period.DAY;

    @Inject
    private StatisticService statisticService;

    @Inject
    private StoreService storeService;

    @Inject
    private OrderService orderService;


    /**
     * 列表
     */
    @GetMapping("/list")
    public String list(Model model, Pageable pageable, String type, Date beginDate, Date endDate) {
        if (StringUtils.isBlank(type)) {
            type = "3";
        }


        if ("3".equals(type) && (endDate == null || endDate == null)) {
            beginDate = DateUtils.getOrderLimitDate(-1, this.order_limit_time);
            endDate = DateUtils.getOrderLimitDate(0, this.order_limit_time);
        }

        String startDate = DateUtils.formatDateTime(beginDate, "");

        String enDate = DateUtils.formatDateTime(endDate, "");

        model.addAttribute("type", type);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("page", statisticService.analyzeListPage(type, null, startDate, enDate, pageable));

        return "admin/store_statistic/list";
    }


    /***
     * 店铺销售top 10 统计
     * @param type
     * @param period
     * @param beginDate
     * @param endDate
     * @return
     */
    @PostMapping("/count-top")
    public ResponseEntity<?> dataOrderTotalCount(Statistic.Type type, Date beginDate, Date endDate) {

        if (beginDate == null || endDate == null) {
            beginDate = DateUtils.getOrderLimitDate(-1, this.order_limit_time);
            endDate = DateUtils.getOrderLimitDate(0, this.order_limit_time);
        }

        String startDate = DateUtils.formatDateTime(beginDate, "");

        String enDate = DateUtils.formatDateTime(endDate, "");

        return ResponseEntity.ok(statisticService.analyzeTotalPaidCount("", startDate, enDate));
    }


    /***
     *店铺统计明细
     * @param model
     * @param pageable
     * @param type
     * @param beginDate
     * @param endDate
     * @return
     */
    @GetMapping("/detail-list")
    public String detaillist(Model model, String type, Pageable pageable, Integer storeId, Date beginDate, Date endDate) {

        Page<Map<String, Object>> page = null;

        if (storeId == null || beginDate == null || endDate == null) {

            page = new Page<>();

        } else {
            String startDate = DateUtils.formatDateTime(beginDate, "");

            String enDate = DateUtils.formatDateTime(endDate, "");

            if (pageable == null) {
                pageable = new Pageable(0, 15);
            }

            page = statisticService.analyzeListPage(type, storeId, startDate, enDate, pageable);
        }


        //查询store

        Store store = storeService.find(Long.parseLong(storeId+""));

        model.addAttribute("store", store);
        model.addAttribute("type", type);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("page", page);

        return "admin/store_statistic/detaillist";
    }


}