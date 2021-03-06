/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: 7H/znPb44dJ/d+AaFKaE21lmYE7Krlu+
 */
package net.shopxx.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.entity.Area;
import net.shopxx.entity.OrderBuy;
import net.shopxx.entity.OrderCountByStore;
import net.shopxx.entity.Statistic;
import net.shopxx.entity.Store;

/**
 * Dao - 统计
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface StatisticDao extends BaseDao<Statistic, Long> {

	/**
	 * 判断统计是否存在
	 * 
	 * @param type
	 *            类型
	 * @param store
	 *            店铺
	 * @param year
	 *            年
	 * @param month
	 *            月
	 * @param day
	 *            日
	 * @return 统计是否存在
	 */
	boolean exists(Statistic.Type type, Store store, int year, int month, int day);

	/**
	 * 分析
	 * 
	 * @param type
	 *            类型
	 * @param store
	 *            店铺
	 * @param period
	 *            周期
	 * @param beginDate
	 *            起始日期
	 * @param endDate
	 *            结束日期
	 * @return 统计
	 */
	List<Statistic> analyze(Statistic.Type type, Store store, Statistic.Period period, Date beginDate, Date endDate);
	
	
	/**
	 * 
	 * @param beginDate 开始时间
	 * @param endDate 结束时间
	 * @param area 区域
	 * @param isAreaChildren 是否包含子区域
	 * @param store 店铺
	 * @return
	 */
	List<OrderCountByStore> ordersCountByStore(String beginDate, String endDate, Area area, Boolean isAreaChildren, Store store);
	
	/**
	 *  订单统计
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	List<OrderBuy> orderBuyList(String beginDate, String endDate);

	/***
	 * 分页统计会员注册
	 * @param type
	 * @param period
	 * @param beginDate
	 * @param endDate
	 * @param pageable
	 * @return
	 */
	Page<Map<String, Object>> analyzeMemberListPage(Statistic.Type type, Statistic.Period period, String beginDate, String endDate, Pageable pageable);


	int  countUser(String beginDate,String endDate);

	/***
	 * 统计订单（有效订单、无效订单、总订单-COUNT_ORDER）
	 * @param type
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	Map<String,Object> analyzeTotalOrderCount(String type,String beginDate, String endDate);

	/***
	 * 统计店铺的订单销售
	 * @param type
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	List<Map<String,Object>> analyzeTotalPaidCount(String type,String beginDate, String endDate);

	/***
	 * 统计列表
	 * @param type 1-订单统计2-订单统计明细3-店铺统计4-店铺统计明细
	 * @param beginDate
	 * @param endDate
	 * @param pageable
	 * @return
	 */

	Page<Map<String,Object>> analyzeListPage(String type,Integer storeId ,String beginDate, String endDate,Pageable pageable);




}