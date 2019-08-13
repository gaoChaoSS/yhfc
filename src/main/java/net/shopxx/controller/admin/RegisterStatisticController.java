/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: Gey8eAwK2Tlltpg06sEiLzQWLi03wnTt
 */
package net.shopxx.controller.admin;

import java.util.*;

import javax.inject.Inject;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.util.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopxx.Results;
import net.shopxx.entity.Statistic;
import net.shopxx.service.StatisticService;

/**
 * Controller - 注册统计
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminRegisterStatisticController")
@RequestMapping("/admin/register_statistic")
public class RegisterStatisticController extends BaseController {

    /**
     * 默认类型
     */
    private static final Statistic.Type DEFAULT_TYPE = Statistic.Type.REGISTER_MEMBER_COUNT;

    /**
     * 默认周期
     */
    private static final Statistic.Period DEFAULT_PERIOD = Statistic.Period.DAY;

    @Inject
    private StatisticService statisticService;

    /**
     * 列表
     */
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("types", Statistic.Type.getTypes(Statistic.Group.REGISTER));
        model.addAttribute("type", DEFAULT_TYPE);
        model.addAttribute("periods", Statistic.Period.values());
        model.addAttribute("period", DEFAULT_PERIOD);
        model.addAttribute("beginDate", DateUtils.addDays(new Date(), -7));
        model.addAttribute("endDate", new Date());
        return "admin/register_statistic/list";
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

        List<Statistic.Type> types = Statistic.Type.getTypes(Statistic.Group.REGISTER);
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

        return ResponseEntity.ok(statisticService.analyze(type, null, period, beginDate, endDate));
    }


    /***
     * 列表统计
     * @param beginDate
     * @param endDate
     * @return
     */
    @PostMapping("/data-count-list")
    public ResponseEntity<?> dataCountList(Pageable pageable, Date beginDate, Date endDate) {
//        if (type == null) {
//            type = DEFAULT_TYPE;
//        }

        if (beginDate == null || endDate == null) {
            beginDate = DateUtils.addDays(new Date(), -7);
            endDate = DateUtils.setOneDateTime(new Date(), 1);
        }

        Date now = DateUtils.setOneDateTime(new Date(), 0);

        Date endDate2 = DateUtils.setOneDateTime(endDate, 0);


        beginDate = DateUtils.addMonths(beginDate, -1);
        endDate = DateUtils.addMonths(endDate, -1);

        if (pageable == null) {
            pageable = new Pageable(0, 20);
        }

        String startDate = DateUtils.formatDateTime(beginDate, "yyyy-MM-dd");

        String enDate = DateUtils.formatDateTime(endDate, "yyyy-MM-dd");

        Page<Map<String, Object>> list = statisticService.analyzeMemberListPage(null, null, startDate, enDate, pageable);

        if (now.equals(endDate2)) {//查询当日用户数
            int num = statisticService.countUser(DateUtils.setOneDateStrTime(new Date(), 0), DateUtils.setOneDateStrTime(new Date(), 1));
            Map<String, Object> map = new HashMap<>();
            map.put("cdate", DateUtils.formatDateTime(new Date(), "yyyy-MM-dd"));
            map.put("num", num);
            list.getContent().add(0,map);
        }


        return ResponseEntity.ok(list);
    }


}