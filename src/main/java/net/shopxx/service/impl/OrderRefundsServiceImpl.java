/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: GjI+J8jOKmNMyMEOv/jHZjAvRbcrVSNH
 */
package net.shopxx.service.impl;

import javax.inject.Inject;

import net.shopxx.dao.OrderDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import net.shopxx.dao.SnDao;
import net.shopxx.entity.OrderRefunds;
import net.shopxx.entity.Sn;
import net.shopxx.service.OrderRefundsService;

import java.util.Date;

/**
 * Service - 订单退款
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Service
public class OrderRefundsServiceImpl extends BaseServiceImpl<OrderRefunds, Long> implements OrderRefundsService {

	@Inject
	private SnDao snDao;

	@Inject
	private OrderDao orderDao ;

	@Override
	@Transactional
	public OrderRefunds save(OrderRefunds orderRefunds) {
		Assert.notNull(orderRefunds, "[Assertion failed] - orderRefunds is required; it must not be null");

		orderRefunds.setSn(snDao.generate(Sn.Type.ORDER_REFUNDS));

		return super.save(orderRefunds);
	}


	@Override
	public void insertRefundLog( String orderId, String sn,Double refundMoney, String refundNum, String cause, int online) {
		orderDao.insertRefundLog(orderId,sn,refundMoney,refundNum,cause,online);
	}
}