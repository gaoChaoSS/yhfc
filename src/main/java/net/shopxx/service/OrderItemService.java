/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: 8OmcBSl2WsXx+WQocD97WqxrNQChyqXT
 */
package net.shopxx.service;

import net.shopxx.entity.Order;
import net.shopxx.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * Service - 订单项
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface OrderItemService extends BaseService<OrderItem, Long> {


    /**
     *  查询用户所有订单的明细列表
     *  @Auther: Demaxiya
     *  @Create: 2019-07-19 10:39
     */
    List<OrderItem> findAllByUser(List<Order> orders);

    /**
     * 查询该商铺配送（已付款、已发货）订单的对应商品各自的数量
     * @param storeId
     * @param goodsName
     * @param startIndex
     * @param length
     * @return
     */
    List<Map<String,Object>> queryInventoryList(Long storeId,String goodsName,int startIndex ,int length);

    void updateProductCheck(int id ,int productCheck);

    List findByOrderId(String orderId);

    List findById(Long orderItemId);

    List findByMemberId(Long memberId);

    List findByMemberId1(Long memberId,String orderItemId);
}