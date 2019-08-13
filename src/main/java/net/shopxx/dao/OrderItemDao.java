/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: 7MtCq7WKhs3Vlzmww5D8I8F71+Lp9mma
 */
package net.shopxx.dao;

import net.shopxx.entity.Order;
import net.shopxx.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * Dao - 订单项
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface OrderItemDao extends BaseDao<OrderItem, Long> {

    /**
     *  查询用户所有订单的明细列表
     *  @Auther: Demaxiya
     *  @Create: 2019-07-19 10:39
     */
    List<OrderItem> findAllByUser(List<Order> orders);

    List<Map<String,Object>> queryInventoryList(Long storeId,String goodsName,int startIndex,int length);

    void updateProductCheck(int id ,int productCheck);

    List findByOrderId(String orderId);

    List findById(Long orderItemId);

    List findByMemberId(Long memberId);

    List findByMemberId1(Long memberId,String orderItemId);
}