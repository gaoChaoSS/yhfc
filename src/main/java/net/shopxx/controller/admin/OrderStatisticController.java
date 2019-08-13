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
 * Controller - 订单统计
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminOrderStatisticController")
@RequestMapping("/admin/order_statistic")
public class OrderStatisticController extends BaseController {

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
    public String list(Model model) {
//        model.addAttribute("types", Statistic.Type.getTypes(Statistic.Group.ORDER));
        model.addAttribute("types", new String[]{"CREATE_ORDER_COUNT", "CREATE_ORDER_AMOUNT"});
        model.addAttribute("type", DEFAULT_TYPE);
        model.addAttribute("periods", Statistic.Period.values());
        model.addAttribute("period", DEFAULT_PERIOD);
//		model.addAttribute("beginDate", DateUtils.addMonths(new Date(), -1));
        Date begin = DateUtils.getOrderLimitDate(-1, this.order_limit_time);
        Date end = DateUtils.getOrderLimitDate(0, this.order_limit_time);
        model.addAttribute("beginDate", begin);
        model.addAttribute("endDate", end);
        model.addAttribute("order_limit_time", this.order_limit_time);
        return "admin/order_statistic/list";
    }

    /**
     * 数据
     */
    @GetMapping("/data")
    public ResponseEntity<?> data(Statistic.Type type, Statistic.Period period, Date beginDate, Date endDate) {
        if (type == null) {
            type = DEFAULT_TYPE;
        }
        if (period == null) {
            period = DEFAULT_PERIOD;
        }

        List<Statistic.Type> types = Statistic.Type.getTypes(Statistic.Group.ORDER);
        if (!types.contains(type)) {
            return Results.UNPROCESSABLE_ENTITY;
        }

        Date now = new Date();
        if (beginDate == null) {
            switch (period) {
                case YEAR:
                    beginDate = DateUtils.addYears(now, -10);
                    break;
                case MONTH:
                    beginDate = DateUtils.addYears(now, -1);
                    break;
                case DAY:
                    beginDate = DateUtils.addMonths(now, -1);
            }
        }
        if (endDate == null) {
            endDate = now;
        }
        switch (period) {
            case YEAR:
                beginDate = DateUtils.truncate(beginDate, Calendar.YEAR);
                Date nextYearMinumumDate = DateUtils.ceiling(endDate, Calendar.YEAR);
                endDate = DateUtils.addMilliseconds(nextYearMinumumDate, -1);
                break;
            case MONTH:
                beginDate = DateUtils.truncate(beginDate, Calendar.MONTH);
                Date nextMonthMinumumDate = DateUtils.ceiling(endDate, Calendar.MONTH);
                endDate = DateUtils.addMilliseconds(nextMonthMinumumDate, -1);
                break;
            case DAY:
                beginDate = DateUtils.truncate(beginDate, Calendar.DAY_OF_MONTH);
                Date tomorrowMinumumDate = DateUtils.ceiling(endDate, Calendar.DAY_OF_MONTH);
                endDate = DateUtils.addMilliseconds(tomorrowMinumumDate, -1);
        }
        return ResponseEntity.ok(statisticService.analyze(type, null, period, beginDate, endDate, this.order_limit_time));
    }

    /**
     * 导出到Excel
     *
     * @param model
     * @param beginDate
     * @param endDate
     * @return
     */
    @GetMapping("/exportExcel")
    public ModelAndView exportExcel(ModelMap model, Date beginDate, Date endDate) {
        String filename = "订单汇总" + DateFormatUtils.format(beginDate, "yyyyMMdd") + "_" + DateFormatUtils.format(endDate, "yyyyMMdd") + ".xls";

        String beginDateFormat = DateFormatUtils.format(beginDate, "yyyy-MM-dd");
        String endDateFormat = DateFormatUtils.format(endDate, "yyyy-MM-dd");
        model.addAttribute("beginDate", beginDateFormat + " " + this.order_limit_time);
        model.addAttribute("endDate", endDateFormat + " " + this.order_limit_time);

        List<OrderCountByStore> orderCountList = statisticService.ordersCountByStore(beginDateFormat + " " + this.order_limit_time, endDateFormat + " " + this.order_limit_time, null, null, null);

        if (orderCountList.size() == 0) {
            OrderCountByStore order = new OrderCountByStore();
            order.setStoreName("选择的时间没有已支付的订单数据");
            orderCountList.add(order);
        }

        List<OrderSheets> sheetsList = individual(orderCountList);

        model.addAttribute("pages", sheetsList);

        model.addAttribute("sheetNames", getSheetName(sheetsList));

        return new ModelAndView(new ExcelView("/admin/order_statistic/download.xls", filename), model);
    }

    private List<OrderSheets> individual(List<OrderCountByStore> list) {
        List<OrderSheets> lists = new ArrayList<OrderSheets>();
        Map<String, List<OrderCountByStore>> map = new HashMap<String, List<OrderCountByStore>>();
        List<OrderCountByStore> orderList = null;
        for (OrderCountByStore store : list) {

            if (map.containsKey(store.getStoreName())) {
                orderList = map.get(store.getStoreName());
                Integer status = store.getStatus();
                if (status == 2) {
                    store.setStatusDesc("已付款(待发货)");
                } else if (status == 3) {
                    store.setStatusDesc("已发货(待收获)");
                } else if (status == 5) {
                    store.setStatusDesc("已取货(已完成)");
                } else {
                    store.setStatusDesc("未知状态");
                }

                orderList.add(store);
            } else {
                orderList = new ArrayList<OrderCountByStore>();
                Integer status = store.getStatus();
                if (status == 2) {
                    store.setStatusDesc("已付款(待发货)");
                } else if (status == 3) {
                    store.setStatusDesc("已发货(待收获)");
                } else if (status == 5) {
                    store.setStatusDesc("已取货(已完成)");
                } else {
                    store.setStatusDesc("未知状态");
                }
                orderList.add(store);
            }

            map.put(store.getStoreName(), orderList);
        }
        for (Map.Entry<String, List<OrderCountByStore>> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            OrderSheets sheet = new OrderSheets();
            sheet.setStoreName(mapKey);
            sheet.setList(entry.getValue());
            lists.add(sheet);
        }
        return lists;
    }

    /**
     * 导出明细转换成excel需要的数据格式
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-30 13:25
     */
    private List<OrderSheetDetails> dataChange(List<OrderDetailsByStore> list) {
        List<OrderSheetDetails> lists = new ArrayList<OrderSheetDetails>();
        Map<String, List<OrderDetailsByStore>> map = new HashMap<String, List<OrderDetailsByStore>>();
        List<OrderDetailsByStore> orderList = null;

        for (OrderDetailsByStore orderDetailsByStore : list) {
            if (map.containsKey(orderDetailsByStore.getStoreName())) {
                orderList = map.get(orderDetailsByStore.getStoreName());
                orderList.add(orderDetailsByStore);
            } else {
                orderList = new ArrayList<OrderDetailsByStore>();
                orderList.add(orderDetailsByStore);
            }
            map.put(orderDetailsByStore.getStoreName(), orderList);
        }

        for (Map.Entry<String, List<OrderDetailsByStore>> entry : map.entrySet()) {
            String mapKey = entry.getKey();
            OrderSheetDetails sheet = new OrderSheetDetails();
            sheet.setStoreName(mapKey);
            sheet.setList(entry.getValue());
            lists.add(sheet);
        }
        return lists;
    }


    private ArrayList<String> getSheetName(List<OrderSheets> page) {
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 0; i < page.size(); i++) {
            al.add(page.get(i).getStoreName());
        }
        return al;
    }

    /**
     * 获取sheet名称
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-30 13:42
     */
    private ArrayList<String> getSheetsName(List<OrderSheetDetails> page) {
        ArrayList<String> al = new ArrayList<String>();
        for (int i = 0; i < page.size(); i++) {
            al.add(page.get(i).getStoreName());
        }
        return al;
    }

    /**
     * 导出到Excel
     *
     * @param model
     * @param beginDate
     * @param endDate
     * @return
     */
    @GetMapping("/exportDetails")
    public ModelAndView exportDetails(ModelMap model, Date beginDate, Date endDate, HttpServletResponse response) {
        String filename = "订单明细" + "_" + DateFormatUtils.format(beginDate, "yyyyMMdd") + ".xls";
        String begin = new SimpleDateFormat("yyyy-MM-dd").format(beginDate) + " " + this.order_limit_time;
        String end = new SimpleDateFormat("yyyy-MM-dd").format(endDate) + " " + this.order_limit_time;

        model.addAttribute("beginDate", begin);
        model.addAttribute("endDate", end);
        // 查出信息并放到map中
        List<OrderDetailsByStore> objects = orderService.findAllByDate(begin, end, null);
        List<OrderSheetDetails> sheetsList = dataChange(objects);

        model.addAttribute("pages", sheetsList);
        model.addAttribute("sheetNames", getSheetsName(sheetsList));
        return new ModelAndView(new ExcelView("/admin/order_statistic/download_details.xls", filename), model);

    }


    /***
     * 订单数量汇总统计
     * @param type
     * @param period
     * @param beginDate
     * @param endDate
     * @return
     */
    @PostMapping("/count-total")
    public ResponseEntity<?> dataOrderTotalCount(Statistic.Type type, Statistic.Period period, Date beginDate, Date endDate) {

        if (beginDate == null || endDate == null) {
            beginDate = DateUtils.getOrderLimitDate(-1, this.order_limit_time);
            endDate = DateUtils.getOrderLimitDate(0, this.order_limit_time);
        }

        String startDate = net.shopxx.util.DateUtils.formatDateTime(beginDate, "");

        String enDate = net.shopxx.util.DateUtils.formatDateTime(endDate, "");

        return ResponseEntity.ok(statisticService.analyzeTotalOrderCount("", startDate, enDate));
    }


    /***
     * 订单和订单明细
     * @param type
     * @param period
     * @param beginDate
     * @param endDate
     * @return
     */
    @PostMapping("/data-count")
    public ResponseEntity<?> dataOrderCountList(String type, Statistic.Period period,Pageable pageable, Date beginDate, Date endDate) {
        if (StringUtils.isBlank(type)) {
            type = "1";
        }

        if ("1".equals(type) && (beginDate == null || endDate == null)) {
             beginDate = DateUtils.getOrderLimitDate(-1, this.order_limit_time);
             endDate = DateUtils.getOrderLimitDate(0, this.order_limit_time);
        }


        String startDate = net.shopxx.util.DateUtils.formatDateTime(beginDate, "");

        String enDate = net.shopxx.util.DateUtils.formatDateTime(endDate, "");

        if (pageable == null) {
            pageable = new Pageable(0, 15);
        }

        return ResponseEntity.ok(statisticService.analyzeListPage(type, null, startDate, enDate, pageable));
    }


    @GetMapping("/detaillist")
    public String detaillist(Model model,String type,Date beginDate, Date endDate,Pageable pageable) {

        Page<Map<String, Object>> page = null;

        if (beginDate == null || endDate == null) {

            page = new Page<>();

        } else {
            String startDate = DateUtils.formatDateTime(beginDate, "");

            String enDate = DateUtils.formatDateTime(endDate, "");

            if (pageable == null) {
                pageable = new Pageable(0, 15);
            }

            page = statisticService.analyzeListPage(type, null, startDate, enDate, pageable);
        }


        model.addAttribute("type", type);
        model.addAttribute("beginDate", beginDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("page", page);
        return "admin/order_statistic/detaillist";
    }


}