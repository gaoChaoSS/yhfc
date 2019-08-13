/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: RK3t9wZPmja1rEQ0btHyGYgKHahRGICC
 */
package net.shopxx.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import net.shopxx.Filter;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.entity.*;
import net.shopxx.entity.Order.CommissionType;

/**
 * Dao - 订单
 *
 * @author SHOP++ Team
 * @version 6.1
 */
public interface OrderDao extends BaseDao<Order, Long> {

	/**
	 * 查找订单
	 *
	 * @param type
	 *            类型
	 * @param status
	 *            状态
	 * @param store
	 *            店铺
	 * @param member
	 *            会员
	 * @param product
	 *            商品
	 * @param isPendingReceive
	 *            是否等待收款
	 * @param isPendingRefunds
	 *            是否等待退款
	 * @param isUseCouponCode
	 *            是否已使用优惠码
	 * @param isExchangePoint
	 *            是否已兑换积分
	 * @param isAllocatedStock
	 *            是否已分配库存
	 * @param hasExpired
	 *            是否已过期
	 * @param count
	 *            数量
	 * @param filters
	 *            筛选
	 * @param orders
	 *            排序
	 * @return 订单
	 */
	List<Order> findList(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired, Integer count, List<Filter> filters,
						 List<net.shopxx.Order> orders);

	/**
	 * 查找订单
	 *
	 * @param type
	 *            类型
	 * @param status
	 *            状态
	 * @param store
	 *            店铺
	 * @param member
	 *            会员
	 * @param product
	 *            商品
	 * @param isPendingReceive
	 *            是否等待收款
	 * @param isPendingRefunds
	 *            是否等待退款
	 * @param isUseCouponCode
	 *            是否已使用优惠码
	 * @param isExchangePoint
	 *            是否已兑换积分
	 * @param isAllocatedStock
	 *            是否已分配库存
	 * @param hasExpired
	 *            是否已过期
	 * @param first
	 *            起始记录
	 * @param count
	 *            数量
	 * @param filters
	 *            筛选
	 * @param orders
	 *            排序
	 * @return 订单
	 */
	List<Order> findList(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired, Integer first, Integer count,
						 List<Filter> filters, List<net.shopxx.Order> orders);

	/**
	 * 查找订单分页
	 *
	 * @param type
	 *            类型
	 * @param status
	 *            状态
	 * @param store
	 *            店铺
	 * @param member
	 *            会员
	 * @param product
	 *            商品
	 * @param isPendingReceive
	 *            是否等待收款
	 * @param isPendingRefunds
	 *            是否等待退款
	 * @param isUseCouponCode
	 *            是否已使用优惠码
	 * @param isExchangePoint
	 *            是否已兑换积分
	 * @param isAllocatedStock
	 *            是否已分配库存
	 * @param hasExpired
	 *            是否已过期
	 * @param pageable
	 *            分页信息
	 * @return 订单分页
	 */
	Page<Order> findPage(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired, Pageable pageable);

	/**
	 * 查询订单数量
	 *
	 * @param type
	 *            类型
	 * @param status
	 *            状态
	 * @param store
	 *            店铺
	 * @param member
	 *            会员
	 * @param product
	 *            商品
	 * @param isPendingReceive
	 *            是否等待收款
	 * @param isPendingRefunds
	 *            是否等待退款
	 * @param isUseCouponCode
	 *            是否已使用优惠码
	 * @param isExchangePoint
	 *            是否已兑换积分
	 * @param isAllocatedStock
	 *            是否已分配库存
	 * @param hasExpired
	 *            是否已过期
	 * @return 订单数量
	 */
	Long count(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired);

	/**
	 * 查询订单创建数
	 *
	 * @param store
	 *            店铺
	 * @param beginDate
	 *            起始日期
	 * @param endDate
	 *            结束日期
	 * @return 订单创建数
	 */
	Long createOrderCount(Store store, Date beginDate, Date endDate,Order.Status... status);

	/**
	 * 查询订单完成数
	 *
	 * @param store
	 *            店铺
	 * @param beginDate
	 *            起始日期
	 * @param endDate
	 *            结束日期
	 * @return 订单完成数
	 */
	Long completeOrderCount(Store store, Date beginDate, Date endDate);

	/**
	 * 查询订单创建金额
	 *
	 * @param store
	 *            店铺
	 * @param beginDate
	 *            起始日期
	 * @param endDate
	 *            结束日期
	 * @return 订单创建金额
	 */
	BigDecimal createOrderAmount(Store store, Date beginDate, Date endDate,Order.Status... status);

	/**
	 * 查询订单完成金额
	 *
	 * @param store
	 *            店铺
	 * @param beginDate
	 *            起始日期
	 * @param endDate
	 *            结束日期
	 * @return 订单完成金额
	 */
	BigDecimal completeOrderAmount(Store store, Date beginDate, Date endDate);

	/**
	 * 查询已发放佣金总额
	 *
	 * @param store
	 *            店铺
	 * @param commissionType
	 *            佣金类型
	 * @param beginDate
	 *            起始日期
	 * @param endDate
	 *            结束日期
	 * @param statuses
	 *            订单状态
	 * @return 已发放佣金总额
	 */
	BigDecimal grantedCommissionTotalAmount(Store store, CommissionType commissionType, Date beginDate, Date endDate, Order.Status... statuses);

	/**
	 *	查询未付款并且超时的订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-17 13:38
	 */
	List<Order> findOverTimes(Order.Status status);

	/**
	 *	查询订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-17 13:38
	 */
	List<OrderDetailsByStore> findAllByDate(String begin, String end, Store store);

	/**
	 *	查询所有已付款还未发货的订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-22 16:47
	 */
	List<Order> findPayNotSend(String limit);


	/**
	 *	根据取货取货码查询订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-23 09:51
	 */
	Order findByCodeAndStatus(String code,Order.Status status,Store store);


	/**
	 *	查询门店当月订单总数(有效订单)
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-24 14:18
	 */
	Integer findStoreMonthCount(Store store);


	/**
	 *	查询门店当天待取订单数
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-24 14:23
	 */
	Integer findStoreDayNoTakeCount(Store store);

	/**
	 *	查询门店当天已取订单数
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-24 14:24
	 */
	Integer findStoreDayTakeCount(Store store);

	/**
	 * 修改订单退款金额
	 * @param refundMoney
	 * @param refund
	 * @return
	 */
	int updateRefundAmount(String orderId, double refundMoney, int refund);

	/**
	 *	根据状态查询用户订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-27 14:57
	 */
	List<Order> findByStatus(Order.Status status,Member member);


	void insertRefundLog( String orderId, String sn, Double refundMoney,String refundNum, String cause, int online);

	/**
	 *	根据查询商品当天购买数量
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-30 15:14
	 */
	Integer findSkuBuyCount(Member member, Sku sku);


	/**
	 *	查询门店还未取货并且没有发送取货短信的订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-08-01 10:46
	 */
	List<Order> findNoTakeOrderByStore(Store store);

	/**
	 * 用户取货后多少时间内的可退订单
	 * @param memberId
	 * @param hours
	 * @return
	 */
    List queryByMemberId(Long memberId, String hours,int page,int length);

	/**
	 *	保存支付记录
	 *  @Auther: Demaxiya
	 *  @Create: 2019-08-07 10:53
	 */
	void savePayLog(int type,String param,BigDecimal amount,int status,String orderSn,Long memberId,String transaction_id);
}