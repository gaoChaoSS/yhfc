/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: NWO74j9wij/TfGQjeABgTAZGBVqkpeUj
 */
package net.shopxx.job;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import net.shopxx.controller.admin.BaseController;
import net.shopxx.controller.api.store.StorerAfterSalesController;
import net.shopxx.entity.OrderCountByStore;
import net.shopxx.entity.ProductCheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.shopxx.service.OrderService;
import net.shopxx.service.ProductCheckService;
import net.shopxx.service.StatisticService;
import net.shopxx.service.StoreService;
import net.shopxx.util.DateUtils;

/**
 * Job - 订单
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Lazy(false)
@Component
public class OrderJob {
	private final static Logger logger = LoggerFactory.getLogger(OrderJob.class);

	@Inject
	private OrderService orderService;
	
	@Inject
	private StatisticService statisticService;
	
	@Inject
	private ProductCheckService productCheckService;
	
	@Inject
	private StoreService storeService;

	@Value("${order_limit_time}")
	protected String order_limit_time;


	

//	/**
//	 * 过期订单处理
//	 */
//	@Scheduled(cron = "${job.order_expired_processing.cron}")
//	public void expiredProcessing() {
//		System.out.println("执行订单过期扫描任务————————————————————");
//		orderService.expiredRefundHandle();
//		orderService.undoExpiredUseCouponCode();
//		orderService.undoExpiredExchangePoint();
//		orderService.releaseExpiredAllocatedStock();
//	}

	/**
	 * 自动收货
	 */
	@Scheduled(cron = "${job.order_automatic_receive.cron}")
	public void automaticReceive() {
		orderService.automaticReceive();
	}

	/**
	 *	关闭订单
	 *	只针对未付款的订单
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-16 13:43
	 */
	@Scheduled(cron = "${job.order_expired_closeOrder.cron}")
	public void closeOrder(){
		orderService.closeOrder();
	}

	/**
	 *	将已付款的订单设为待发货
	 *	每天23点执行任务
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-22 16:59
	 */
	@Scheduled(cron = "${job.order_expired_sendOrder.cron}")
	public void sendOrder(){
		logger.info("执行发货任务,时间参数{}",order_limit_time);
		orderService.upateOrderStatus(order_limit_time);
	}
	
	/**
	 * 待发货订单生成盘点数据
	 * 每天23点30执行
	 * @author yangli
	 */
	@Scheduled(cron = "${job.order_product_check.cron}")
	//@Scheduled(cron = "*/5 * * * * ?")
	public void orderCheck()
	{
		logger.info("执行盘点任务,时间参数{}",order_limit_time);
		Date startDate = DateUtils.getOrderLimitDate(-1, order_limit_time);
		Date endDate = DateUtils.getOrderLimitDate(0, order_limit_time);
		List<OrderCountByStore> list = statisticService.ordersCountByStore(DateUtils.getDateFormat(2, startDate), DateUtils.getDateFormat(2, endDate), null, null, null);

		//仓库已发货订单  还需添加商品图片（未添加）
		ProductCheck pc = null;
		for(OrderCountByStore ocbs:list)
		{
			pc = new ProductCheck();
			pc.setStore(storeService.find(ocbs.getId().longValue()));
			pc.setProductName(ocbs.getProductName());
			pc.setSn(ocbs.getSn());

			//取的压缩图片
			pc.setImg(productCheckService.queryImgBySn(ocbs.getSn()));

			pc.setSpecifications(ocbs.getSpecifications());
			pc.setCount(ocbs.getCount());
			pc.setCountCheck(null);
			productCheckService.save(pc);
		}
	}
}