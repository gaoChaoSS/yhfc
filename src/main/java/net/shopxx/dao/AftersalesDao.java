/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: JqzXDomykJ97Q3wA18T0HGPonfCDXEu4
 */
package net.shopxx.dao;

import java.util.List;
import java.util.Map;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.entity.Aftersales;
import net.shopxx.entity.Member;
import net.shopxx.entity.OrderItem;
import net.shopxx.entity.Store;

/**
 * Dao - 售后
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface AftersalesDao extends BaseDao<Aftersales, Long> {

	/**
	 * 查找售后列表
	 * 
	 * @param orderItems
	 *            订单项
	 * @return 售后列表
	 */
	List<Aftersales> findList(List<OrderItem> orderItems);

	/**
	 * 查找售后分页
	 * 
	 * @param type
	 *            类型
	 * @param status
	 *            状态
	 * @param member
	 *            会员
	 * @param store
	 *            店铺
	 * @param pageable
	 *            分页信息
	 * @return 售后分页
	 */
	Page<Aftersales> findPage(Aftersales.Type type, Aftersales.Status status, Member member, Store store, Pageable pageable);

	/**
	 * 售后记录
	 * @param memberId 用户id
	 * @return
	 */
    List<Map<String,Object>> queryAfterSaleRecord(Long memberId,int page ,int length);

	List<Map<String,Object>> queryRefundCause();

	List<Map<String,Object>> queryAfterSaleRecordByOrderId(Long orderId);

	/**
	 * 保存售后
	 * @param afterSaleId
	 * @param memberId
	 * @param storeId
	 * @param status
	 * @param afterSaleCause
	 * @param des
	 * @param pic
	 * @param orderId
	 */
	void saveAfterSale(String afterSaleId, Long memberId, String storeId, int status, String afterSaleCause, String des, String pic, Long orderId);

	/**
	 * 保存售后项
	 * @param id
	 * @param orderItemId
	 * @param afterSaleId
	 * @param afterQuantity
	 */
	void saveAfterSaleItems(String id, String orderItemId, String afterSaleId, int afterQuantity);
}