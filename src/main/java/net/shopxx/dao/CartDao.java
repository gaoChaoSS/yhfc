/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: sw/ND8IMf1McSDVGAxbp9yhfhsS1FrL8
 */
package net.shopxx.dao;

import net.shopxx.entity.Cart;

/**
 * Dao - 购物车
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface CartDao extends BaseDao<Cart, Long> {

	/**
	 * 删除过期购物车
	 */
	void deleteExpired();


	/**
	 *	查询当前用户是否有购物车
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-12 14:38
	 */
	Cart findByUserId(Long userId);

}