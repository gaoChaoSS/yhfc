/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: iYzlUZpm78KtviDlp3hjC8nTbliAuOFZ
 */
package net.shopxx.dao.impl;

import java.util.Date;

import net.shopxx.entity.Store;
import org.springframework.stereotype.Repository;

import net.shopxx.dao.CartDao;
import net.shopxx.entity.Cart;

import javax.persistence.TypedQuery;

/**
 * Dao - 购物车
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Repository
public class CartDaoImpl extends BaseDaoImpl<Cart, Long> implements CartDao {

	@Override
	public void deleteExpired() {
		String cartItemJpql = "delete from CartItem cartItem where cartItem.cart.id in (select cart.id from Cart cart where cart.expire is not null and cart.expire <= :now)";
		String cartJpql = "delete from Cart cart where cart.expire is not null and cart.expire <= :now";
		Date now = new Date();
		entityManager.createQuery(cartItemJpql).setParameter("now", now).executeUpdate();
		entityManager.createQuery(cartJpql).setParameter("now", now).executeUpdate();
	}

	@Override
	public Cart findByUserId(Long userId) {
		TypedQuery<Cart> query;
		String hql="select cart from Cart cart where cart.member_id=:userId ";
		query=entityManager.createQuery(hql,Cart.class);
		query.setParameter("userId",userId);
		return query.getSingleResult();
	}


}