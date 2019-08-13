/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: AqkaYXUzpDRIiD0AlvfUhD+MKlvmrbfM
 */
package net.shopxx.service.impl;

import net.shopxx.dao.OrderItemDao;
import net.shopxx.entity.Order;
import org.springframework.stereotype.Service;

import net.shopxx.entity.OrderItem;
import net.shopxx.service.OrderItemService;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Service - 订单项
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Service
public class OrderItemServiceImpl extends BaseServiceImpl<OrderItem, Long> implements OrderItemService {

    @Inject
    private OrderItemDao orderItemDao;

    @Override
    public List<OrderItem> findAllByUser(List<Order> orders) {
        return orderItemDao.findAllByUser(orders);
    }

    @Override
    public List<Map<String, Object>> queryInventoryList(Long storeId,String goodsName,int startIndex,int length) {
        return orderItemDao.queryInventoryList(storeId,goodsName,startIndex,length);
    }

    @Override
    public void updateProductCheck(int id ,int productCheck) {
        orderItemDao.updateProductCheck(id ,productCheck);
    }

    @Override
    public List findByOrderId(String orderId) {
        return orderItemDao.findByOrderId(orderId);
    }

    @Override
    public List findById(Long orderItemId) {
        return orderItemDao.findById(orderItemId);
    }

    @Override
    public List findByMemberId(Long memberId) {
        return orderItemDao.findByMemberId(memberId);
    }

    @Override
    public List findByMemberId1(Long memberId,String orderItemId) {
        return orderItemDao.findByMemberId1(memberId,orderItemId);
    }
}