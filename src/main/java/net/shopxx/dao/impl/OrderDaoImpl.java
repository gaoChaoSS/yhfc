/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: vpyumrmDtBe8TmCcI3ImOydXvtlscJwd
 */
package net.shopxx.dao.impl;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import net.shopxx.entity.*;
import net.shopxx.util.DateUtils;
import net.shopxx.util.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import net.shopxx.Filter;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.dao.OrderDao;
import net.shopxx.entity.Order.CommissionType;
import net.shopxx.entity.Order.Status;

/**
 * Dao - 订单
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Repository
public class OrderDaoImpl extends BaseDaoImpl<Order, Long> implements OrderDao {

	@Override
	public List<Order> findList(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired, Integer count, List<Filter> filters,
								List<net.shopxx.Order> orders) {
		return findList(type, status, store, member, product, isPendingReceive, isPendingRefunds, isUseCouponCode, isExchangePoint, isAllocatedStock, hasExpired, null, count, filters, orders);
	}

	@Override
	public List<Order> findList(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired, Integer first, Integer count,
								List<Filter> filters, List<net.shopxx.Order> orders) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(root);
		Predicate restrictions = criteriaBuilder.conjunction();
		if (type != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("type"), type));
		}
		if (status != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("status"), status));
		}
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (member != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("member"), member));
		}
		if (product != null) {
			Subquery<Sku> skuSubquery = criteriaQuery.subquery(Sku.class);
			Root<Sku> skuSubqueryRoot = skuSubquery.from(Sku.class);
			skuSubquery.select(skuSubqueryRoot);
			skuSubquery.where(criteriaBuilder.equal(skuSubqueryRoot.get("product"), product));

			Subquery<OrderItem> orderItemSubquery = criteriaQuery.subquery(OrderItem.class);
			Root<OrderItem> orderItemSubqueryRoot = orderItemSubquery.from(OrderItem.class);
			orderItemSubquery.select(orderItemSubqueryRoot);
			orderItemSubquery.where(criteriaBuilder.equal(orderItemSubqueryRoot.get("order"), root), criteriaBuilder.in(orderItemSubqueryRoot.get("sku")).value(skuSubquery));
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.exists(orderItemSubquery));
		}
		if (isPendingReceive != null) {
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.or(root.get("expire").isNull(), criteriaBuilder.greaterThan(root.<Date>get("expire"), new Date())), criteriaBuilder.equal(root.get("paymentMethodType"), PaymentMethod.Type.CASH_ON_DELIVERY),
					criteriaBuilder.notEqual(root.get("status"), Order.Status.COMPLETED), criteriaBuilder.notEqual(root.get("status"), Order.Status.FAILED), criteriaBuilder.notEqual(root.get("status"), Order.Status.CANCELED), criteriaBuilder.notEqual(root.get("status"), Order.Status.DENIED),
					criteriaBuilder.lessThan(root.<BigDecimal>get("amountPaid"), root.<BigDecimal>get("amount")));
			if (isPendingReceive) {
				restrictions = criteriaBuilder.and(restrictions, predicate);
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.not(predicate));
			}
		}
		if (isPendingRefunds != null) {
			Predicate predicate = criteriaBuilder.or(
					criteriaBuilder.and(criteriaBuilder.or(criteriaBuilder.and(root.get("expire").isNotNull(), criteriaBuilder.lessThanOrEqualTo(root.<Date>get("expire"), new Date())), criteriaBuilder.equal(root.get("status"), Order.Status.FAILED),
							criteriaBuilder.equal(root.get("status"), Order.Status.CANCELED), criteriaBuilder.equal(root.get("status"), Order.Status.DENIED)), criteriaBuilder.greaterThan(root.<BigDecimal>get("amountPaid"), BigDecimal.ZERO)),
					criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), Order.Status.COMPLETED), criteriaBuilder.greaterThan(root.<BigDecimal>get("amountPaid"), root.<BigDecimal>get("amount"))));
			if (isPendingRefunds) {
				restrictions = criteriaBuilder.and(restrictions, predicate);
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.not(predicate));
			}
		}
		if (isUseCouponCode != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isUseCouponCode"), isUseCouponCode));
		}
		if (isExchangePoint != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isExchangePoint"), isExchangePoint));
		}
		if (isAllocatedStock != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isAllocatedStock"), isAllocatedStock));
		}
		if (hasExpired != null) {
			if (hasExpired) {
				restrictions = criteriaBuilder.and(restrictions, root.get("expire").isNotNull(), criteriaBuilder.lessThanOrEqualTo(root.<Date>get("expire"), new Date()));
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.or(root.get("expire").isNull(), criteriaBuilder.greaterThan(root.<Date>get("expire"), new Date())));
			}
		}
		criteriaQuery.where(restrictions);
		return super.findList(criteriaQuery, first, count, filters, orders);
	}

	@Override
	public Page<Order> findPage(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired, Pageable pageable) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(root);
		Predicate restrictions = criteriaBuilder.conjunction();
		if (type != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("type"), type));
		}
		if (status != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("status"), status));
		}
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (member != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("member"), member));
		}
		if (product != null) {
			Subquery<Sku> skuSubquery = criteriaQuery.subquery(Sku.class);
			Root<Sku> skuSubqueryRoot = skuSubquery.from(Sku.class);
			skuSubquery.select(skuSubqueryRoot);
			skuSubquery.where(criteriaBuilder.equal(skuSubqueryRoot.get("product"), product));

			Subquery<OrderItem> orderItemSubquery = criteriaQuery.subquery(OrderItem.class);
			Root<OrderItem> orderItemSubqueryRoot = orderItemSubquery.from(OrderItem.class);
			orderItemSubquery.select(orderItemSubqueryRoot);
			orderItemSubquery.where(criteriaBuilder.equal(orderItemSubqueryRoot.get("order"), root), criteriaBuilder.in(orderItemSubqueryRoot.get("sku")).value(skuSubquery));
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.exists(orderItemSubquery));
		}
		if (isPendingReceive != null) {
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.or(root.get("expire").isNull(), criteriaBuilder.greaterThan(root.<Date>get("expire"), new Date())), criteriaBuilder.equal(root.get("paymentMethodType"), PaymentMethod.Type.CASH_ON_DELIVERY),
					criteriaBuilder.notEqual(root.get("status"), Order.Status.COMPLETED), criteriaBuilder.notEqual(root.get("status"), Order.Status.FAILED), criteriaBuilder.notEqual(root.get("status"), Order.Status.CANCELED), criteriaBuilder.notEqual(root.get("status"), Order.Status.DENIED),
					criteriaBuilder.lessThan(root.<BigDecimal>get("amountPaid"), root.<BigDecimal>get("amount")));
			if (isPendingReceive) {
				restrictions = criteriaBuilder.and(restrictions, predicate);
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.not(predicate));
			}
		}
		if (isPendingRefunds != null) {
			Predicate predicate = criteriaBuilder.or(
					criteriaBuilder.and(criteriaBuilder.or(criteriaBuilder.and(root.get("expire").isNotNull(), criteriaBuilder.lessThanOrEqualTo(root.<Date>get("expire"), new Date())), criteriaBuilder.equal(root.get("status"), Order.Status.FAILED),
							criteriaBuilder.equal(root.get("status"), Order.Status.CANCELED), criteriaBuilder.equal(root.get("status"), Order.Status.DENIED)), criteriaBuilder.greaterThan(root.<BigDecimal>get("amountPaid"), BigDecimal.ZERO)),
					criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), Order.Status.COMPLETED), criteriaBuilder.greaterThan(root.<BigDecimal>get("amountPaid"), root.<BigDecimal>get("amount"))));
			if (isPendingRefunds) {
				restrictions = criteriaBuilder.and(restrictions, predicate);
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.not(predicate));
			}
		}
		if (isUseCouponCode != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isUseCouponCode"), isUseCouponCode));
		}
		if (isExchangePoint != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isExchangePoint"), isExchangePoint));
		}
		if (isAllocatedStock != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isAllocatedStock"), isAllocatedStock));
		}
		if (hasExpired != null) {
			if (hasExpired) {
				restrictions = criteriaBuilder.and(restrictions, root.get("expire").isNotNull(), criteriaBuilder.lessThanOrEqualTo(root.<Date>get("expire"), new Date()));
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.or(root.get("expire").isNull(), criteriaBuilder.greaterThan(root.<Date>get("expire"), new Date())));
			}
		}
		criteriaQuery.where(restrictions);
		return super.findPage(criteriaQuery, pageable);
	}

	@Override
	public Long count(Order.Type type, Order.Status status, Store store, Member member, Product product, Boolean isPendingReceive, Boolean isPendingRefunds, Boolean isUseCouponCode, Boolean isExchangePoint, Boolean isAllocatedStock, Boolean hasExpired) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(root);
		Predicate restrictions = criteriaBuilder.conjunction();
		if (type != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("type"), type));
		}
		if (status != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("status"), status));
		}
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (member != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("member"), member));
		}
		if (product != null) {
			Subquery<Sku> skuSubquery = criteriaQuery.subquery(Sku.class);
			Root<Sku> skuSubqueryRoot = skuSubquery.from(Sku.class);
			skuSubquery.select(skuSubqueryRoot);
			skuSubquery.where(criteriaBuilder.equal(skuSubqueryRoot.get("product"), product));

			Subquery<OrderItem> orderItemSubquery = criteriaQuery.subquery(OrderItem.class);
			Root<OrderItem> orderItemSubqueryRoot = orderItemSubquery.from(OrderItem.class);
			orderItemSubquery.select(orderItemSubqueryRoot);
			orderItemSubquery.where(criteriaBuilder.equal(orderItemSubqueryRoot.get("order"), root), criteriaBuilder.in(orderItemSubqueryRoot.get("sku")).value(skuSubquery));
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.exists(orderItemSubquery));
		}
		if (isPendingReceive != null) {
			Predicate predicate = criteriaBuilder.and(criteriaBuilder.or(root.get("expire").isNull(), criteriaBuilder.greaterThan(root.<Date>get("expire"), new Date())), criteriaBuilder.equal(root.get("paymentMethodType"), PaymentMethod.Type.CASH_ON_DELIVERY),
					criteriaBuilder.notEqual(root.get("status"), Order.Status.COMPLETED), criteriaBuilder.notEqual(root.get("status"), Order.Status.FAILED), criteriaBuilder.notEqual(root.get("status"), Order.Status.CANCELED), criteriaBuilder.notEqual(root.get("status"), Order.Status.DENIED),
					criteriaBuilder.lessThan(root.<BigDecimal>get("amountPaid"), root.<BigDecimal>get("amount")));
			if (isPendingReceive) {
				restrictions = criteriaBuilder.and(restrictions, predicate);
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.not(predicate));
			}
		}
		if (isPendingRefunds != null) {
			Predicate predicate = criteriaBuilder.or(
					criteriaBuilder.and(criteriaBuilder.or(criteriaBuilder.and(root.get("expire").isNotNull(), criteriaBuilder.lessThanOrEqualTo(root.<Date>get("expire"), new Date())), criteriaBuilder.equal(root.get("status"), Order.Status.FAILED),
							criteriaBuilder.equal(root.get("status"), Order.Status.CANCELED), criteriaBuilder.equal(root.get("status"), Order.Status.DENIED)), criteriaBuilder.greaterThan(root.<BigDecimal>get("amountPaid"), BigDecimal.ZERO)),
					criteriaBuilder.and(criteriaBuilder.equal(root.get("status"), Order.Status.COMPLETED), criteriaBuilder.greaterThan(root.<BigDecimal>get("amountPaid"), root.<BigDecimal>get("amount"))));
			if (isPendingRefunds) {
				restrictions = criteriaBuilder.and(restrictions, predicate);
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.not(predicate));
			}
		}
		if (isUseCouponCode != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isUseCouponCode"), isUseCouponCode));
		}
		if (isExchangePoint != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isExchangePoint"), isExchangePoint));
		}
		if (isAllocatedStock != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("isAllocatedStock"), isAllocatedStock));
		}
		if (hasExpired != null) {
			if (hasExpired) {
				restrictions = criteriaBuilder.and(restrictions, root.get("expire").isNotNull(), criteriaBuilder.lessThanOrEqualTo(root.<Date>get("expire"), new Date()));
			} else {
				restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.or(root.get("expire").isNull(), criteriaBuilder.greaterThan(root.<Date>get("expire"), new Date())));
			}
		}
		criteriaQuery.where(restrictions);
		return super.count(criteriaQuery, null);
	}

	@Override
	public Long createOrderCount(Store store, Date beginDate, Date endDate,Order.Status... statuses) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(root);
		Predicate restrictions = criteriaBuilder.conjunction();
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (beginDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdDate"), beginDate));
		}
		if (endDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdDate"), endDate));
		}
		if (statuses != null && statuses.length > 0) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.in(root.get("status")).value(Arrays.asList(statuses)));
		}
		criteriaQuery.where(restrictions);
		return super.count(criteriaQuery, null);
	}

	@Override
	public Long completeOrderCount(Store store, Date beginDate, Date endDate) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Order> criteriaQuery = criteriaBuilder.createQuery(Order.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(root);
		Predicate restrictions = criteriaBuilder.conjunction();
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (beginDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("completeDate"), beginDate));
		}
		if (endDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.lessThanOrEqualTo(root.<Date>get("completeDate"), endDate));
		}
		criteriaQuery.where(restrictions);
		return super.count(criteriaQuery, null);
	}

	@Override
	public BigDecimal createOrderAmount(Store store, Date beginDate, Date endDate,Order.Status... statuses) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> criteriaQuery = criteriaBuilder.createQuery(BigDecimal.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(criteriaBuilder.sum(root.<BigDecimal>get("amount")));
		Predicate restrictions = criteriaBuilder.conjunction();
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (beginDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("createdDate"), beginDate));
		}
		if (endDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.lessThanOrEqualTo(root.<Date>get("createdDate"), endDate));
		}
		if (statuses != null && statuses.length > 0) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.in(root.get("status")).value(Arrays.asList(statuses)));
		}
		criteriaQuery.where(restrictions);
		BigDecimal result = entityManager.createQuery(criteriaQuery).getSingleResult();
		return result != null ? result : BigDecimal.ZERO;
	}

	@Override
	public BigDecimal completeOrderAmount(Store store, Date beginDate, Date endDate) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> criteriaQuery = criteriaBuilder.createQuery(BigDecimal.class);
		Root<Order> root = criteriaQuery.from(Order.class);
		criteriaQuery.select(criteriaBuilder.sum(root.<BigDecimal>get("amount")));
		Predicate restrictions = criteriaBuilder.conjunction();
		if (store != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
		}
		if (beginDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.greaterThanOrEqualTo(root.<Date>get("completeDate"), beginDate));
		}
		if (endDate != null) {
			restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.lessThanOrEqualTo(root.<Date>get("completeDate"), endDate));
		}
		criteriaQuery.where(restrictions);
		BigDecimal result = entityManager.createQuery(criteriaQuery).getSingleResult();
		return result != null ? result : BigDecimal.ZERO;
	}

	@Override
	public BigDecimal grantedCommissionTotalAmount(Store store, CommissionType commissionType, Date beginDate, Date endDate, Status... statuses) {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<BigDecimal> criteriaQuery = criteriaBuilder.createQuery(BigDecimal.class);
		Root<OrderItem> root = criteriaQuery.from(OrderItem.class);
		switch (commissionType) {
			case PLATFORM:
				criteriaQuery.select(criteriaBuilder.sum(root.<BigDecimal>get("platformCommissionTotals")));
				break;
			case DISTRIBUTION:
				criteriaQuery.select(criteriaBuilder.sum(root.<BigDecimal>get("distributionCommissionTotals")));
				break;
		}
		Predicate restrictions = criteriaBuilder.conjunction();

		Subquery<Order> orderSubquery = criteriaQuery.subquery(Order.class);
		Root<Order> orderSubqueryRoot = orderSubquery.from(Order.class);
		orderSubquery.select(orderSubqueryRoot);
		Predicate orderSubqueryRestrictions = criteriaBuilder.conjunction();
		if (store != null) {
			orderSubqueryRestrictions = criteriaBuilder.and(orderSubqueryRestrictions, criteriaBuilder.equal(orderSubqueryRoot.get("store"), store));
		}
		if (beginDate != null) {
			orderSubqueryRestrictions = criteriaBuilder.and(orderSubqueryRestrictions, criteriaBuilder.greaterThanOrEqualTo(orderSubqueryRoot.<Date>get("completeDate"), beginDate));
		}
		if (endDate != null) {
			orderSubqueryRestrictions = criteriaBuilder.and(orderSubqueryRestrictions, criteriaBuilder.lessThanOrEqualTo(orderSubqueryRoot.<Date>get("completeDate"), endDate));
		}
		if (statuses != null) {
			orderSubqueryRestrictions = criteriaBuilder.and(orderSubqueryRestrictions, criteriaBuilder.in(orderSubqueryRoot.get("status")).value(Arrays.asList(statuses)));
		}
		orderSubquery.where(orderSubqueryRestrictions);

		restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.in(root.get("order")).value(orderSubquery));
		criteriaQuery.where(restrictions);
		BigDecimal result = entityManager.createQuery(criteriaQuery).getSingleResult();
		return result != null ? result : BigDecimal.ZERO;
	}

	@Override
	public List<Order> findOverTimes(Status status) {
		TypedQuery<Order> query;
		String jpql="select order from Order order where order.status=:status and order.expire<=now() ";
		query = entityManager.createQuery(jpql, Order.class);
		query.setParameter("status", status);
		return query.getResultList();
	}


	@Override
	public List<OrderDetailsByStore> findAllByDate(String begin, String end, Store store) {
		String sql="SELECT   " +
				"s.id storeId,  " +
				"o.member_id userId,  " +
				"u.attributeValue1 userName,  " +
				"u.mobile mobile,  " +
				"oi.name productName,  " +
				"oi.price productPrice,  " +
				"SUM(oi.quantity) productCount,  " +
				"oi.specifications productSpec,  " +
				"s.name storeName,  " +
				"CASE o.status  " +
				"     WHEN 2 THEN '已付款' " +
				"     WHEN 3 THEN '已发货' " +
				"     WHEN 5 THEN '已完成' " +
				" ELSE '' " +
				" END AS status,  " +
				" pc.name typeName "+
				"FROM orderitem oi  " +
				"LEFT JOIN orders o ON oi.orders=o.id  " +
				"LEFT JOIN sku sk ON oi.sku_id=sk.id " +
				"LEFT JOIN product p ON sk.product_id=p.id " +
				"LEFT JOIN productcategory pc ON p.productCategory_id=pc.id "+
				"LEFT JOIN store s ON o.store_id=s.id  " +
				"LEFT JOIN users u ON o.member_id=u.id  " +
				"WHERE o.status IN (2,3,5)  and o.payTime >='" +
				begin+"' and o.payTime <='"+end+"'";
		if(store != null){
			sql=sql+" and o.store_id="+store.getId();
		}
		sql=sql+" GROUP BY o.store_id,o.member_id,oi.sku_id,oi.price ";


		Query query=entityManager.createNativeQuery(sql);
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.aliasToBean(OrderDetailsByStore.class));
		return query.getResultList();
	}

	@Override
	public List<Order> findPayNotSend(String limit) {
		TypedQuery<Order> query;
		String jpql="select order from Order order where order.status=2 and order.payTime<=CONCAT(CURRENT_DATE,' ','"+limit+"')  ";
		query = entityManager.createQuery(jpql, Order.class);
		return query.getResultList();
	}

	@Override
	public Order findByCodeAndStatus(String code, Status status,Store store) {
		TypedQuery<Order> query;
		String jpql="";
		if(null != status){
			jpql="select order from Order order where order.outOrderCode=:code and order.status=:status and order.store=:store ";
			query = entityManager.createQuery(jpql, Order.class);
			query.setParameter("code",code).setParameter("status",status).setParameter("store",store);
		}else{
			jpql="select order from Order order where order.outOrderCode=:code and order.store=:store ";
			query = entityManager.createQuery(jpql, Order.class);
			query.setParameter("code",code).setParameter("store",store);
		}
		try{
			return query.getSingleResult();
		}catch (NoResultException e){
			return null;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Integer findStoreMonthCount(Store store) {
		TypedQuery<Order> query;
		String jpql="select order from Order order where order.store=:store and order.status in (2,3,5) " +
				"AND order.payTime>=CONCAT(DATE_FORMAT(CURDATE() ,'%Y-%m'),'-01 ','00:00:00') " +
				"AND order.payTime<=CONCAT(LAST_DAY(CURDATE()),' ','23:59:59') ";
		query = entityManager.createQuery(jpql, Order.class);
		query.setParameter("store",store);
		return query.getResultList().size();
	}

	@Override
	public Integer findStoreDayNoTakeCount(Store store) {
		TypedQuery<Order> query;
		String jpql="SELECT order FROM Order order WHERE order.store=:store AND order.status IN (3) " ;
		query = entityManager.createQuery(jpql, Order.class);
		query.setParameter("store",store);
		return query.getResultList().size();
	}

	@Override
	public Integer findStoreDayTakeCount(Store store) {
		TypedQuery<Order> query;
		String jpql="SELECT order FROM Order order WHERE order.store=:store AND order.status IN (5) " +
				"AND order.completeDate>=CONCAT(CURDATE(),' ','00:00:00') " +
				"AND order.completeDate<=CONCAT(CURDATE(),' ','23:59:59') ";
		query = entityManager.createQuery(jpql, Order.class);
		query.setParameter("store",store);
		return query.getResultList().size();
	}

	@Override
	public int updateRefundAmount(String orderId, double refundMoney, int refund) {
		String sql = "UPDATE orders " +
					    "SET refundAmount = (refundAmount + ?), " +
					    "status = ? " +
						" WHERE " +
						" orders.id = ? " ;
		Session session = entityManager.unwrap(Session.class);
		int i = session.createSQLQuery(sql)
				.setParameter(0, refundMoney)
				.setParameter(1, refund)
				.setParameter(2, Long.valueOf(orderId))
				.executeUpdate();

		return i;
	}

	@Override
	public List<Order> findByStatus(Status status, Member member) {
		TypedQuery<Order> query;
		String jpql="select order from Order order where order.status=:status and order.member=:member ";
		query = entityManager.createQuery(jpql, Order.class);
		query.setParameter("status",status).setParameter("member",member);
		return query.getResultList();
	}

	@Override
	public void insertRefundLog( String orderId, String sn, Double refundMoney,String refundNum, String cause, int online) {
		//System.err.println("refundMoney:"+refundMoney);
		//插入一条退款记录
		String sql = "INSERT INTO orderrefunds (id,createdDate,lastModifiedDate,version,amount,memo,method,sn,orders) VALUES (?,?,?,1,?,?,?,?,?)" ;
		Session session = entityManager.unwrap(Session.class);
		int i = session.createSQLQuery(sql)
				.setParameter(0, StringUtils.getId())
				.setParameter(1, DateUtils.myFormatDate(new Date().toString()))
				.setParameter(2, DateUtils.myFormatDate(new Date().toString()))
				.setParameter(3, refundMoney)
				.setParameter(4, cause)
				.setParameter(5, online)
				.setParameter(6, sn)
				.setParameter(7, orderId).executeUpdate();
		//System.err.println("i:"+i);


	}
	@Override
	public Integer findSkuBuyCount(Member member, Sku sku) {
		String sql="SELECT IF(SUM(oi.quantity) IS NULL,0,SUM(oi.quantity)) count FROM orderitem oi   " +
				"LEFT JOIN orders o ON oi.orders=o.id   " +
				"WHERE o.status IN (2,3,5)   " +
				"AND oi.sku_id="+sku.getId()+" AND o.member_id="+member.getId();
		Query query = entityManager.createNativeQuery(sql.toString());
		query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		List<Map<String,Object>> map= query.getResultList();
		return Integer.parseInt(map.get(0).get("count").toString());
	}

	@Override
	public List<Order> findNoTakeOrderByStore(Store store) {
		TypedQuery<Order> query;
		String jpql="select order from Order order where order.status in (3) and order.store=:store and order.isSendSuccess=false ";
		query = entityManager.createQuery(jpql, Order.class);
		query.setParameter("store",store);
		return query.getResultList();
	}

	@Override
	public List queryByMemberId(Long memberId, String hours,int page,int length) {
		String sql= " SELECT " +
						" orders.id AS orderId," +
						" orders.member_id AS memberId," +
						" users.name AS userName," +
						" users.phone AS phone," +
						" orders.sn AS sn " +
					" FROM " +
						" orders " +
						" LEFT JOIN  users ON users.id = orders.member_id " +
					" WHERE " +
						" orders.member_id = ? " +
						" AND orders. STATUS in(5,11) " +
						" AND orders.createdDate >= DATE_ADD(NOW(),INTERVAL-? HOUR) " +
						" ORDER BY orders.createdDate DESC ";

		Session session = entityManager.unwrap(Session.class);
		//起始下标
		int startIndex = (page - 1) * length;
		List<Map<String,Object>> listOrder = session.createSQLQuery(sql)
				.setParameter(0, memberId).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(startIndex).setMaxResults(length)
				.setParameter(1, hours)
				.list();
		return listOrder ;
	}

	@Override
	public void savePayLog(int type, String param, BigDecimal amount, int status, String orderSn, Long memberId,String transaction_id) {
		String sql = "INSERT INTO pay_log(createdDate,pay_type,pay_param,amount,pay_status,orderSn,user_id,transaction_id) VALUES(NOW(),?,?,?,?,?,?,?)" ;
		Session session = entityManager.unwrap(Session.class);
		int i = session.createSQLQuery(sql)
				.setParameter(0, type)
				.setParameter(1, param)
				.setParameter(2, amount)
				.setParameter(3, status)
				.setParameter(4, orderSn)
				.setParameter(5, memberId)
                .setParameter(6, transaction_id)
                .executeUpdate();
	}
}