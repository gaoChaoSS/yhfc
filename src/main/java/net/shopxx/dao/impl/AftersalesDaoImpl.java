/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: pqAg4JTBkF6hyciugysfCWYTyXC4t1Qq
 */
package net.shopxx.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import net.shopxx.util.DateUtils;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.dao.AftersalesDao;
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
@Repository
public class AftersalesDaoImpl extends BaseDaoImpl<Aftersales, Long> implements AftersalesDao {

    @Override
    public List<Aftersales> findList(List<OrderItem> orderItems) {
        String jpql = "select aftersales from Aftersales aftersales where aftersales in(select aftersalesItem.aftersales from AftersalesItem aftersalesItem where aftersalesItem.orderItem in(:orderItems))";
        TypedQuery<Aftersales> query = entityManager.createQuery(jpql, Aftersales.class);
        return query.setParameter("orderItems", orderItems).getResultList();
    }

    @Override
    public Page<Aftersales> findPage(Aftersales.Type type, Aftersales.Status status, Member member, Store store, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Aftersales> criteriaQuery = criteriaBuilder.createQuery(Aftersales.class);
        Class clz = type != null ? type.getClazz() : Aftersales.class;
        Root<? extends Aftersales> root = criteriaQuery.from(clz);
        criteriaQuery.select(root);
        Predicate restrictions = criteriaBuilder.conjunction();
        if (status != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("status"), status));
        }
        if (member != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("member"), member));
        }
        if (store != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
        }
        criteriaQuery.where(restrictions);

        return super.findPage(criteriaQuery, pageable);
    }


    @Override
    public List<Map<String, Object>> queryAfterSaleRecord(Long memberId, int page, int length) {
        Session session = entityManager.unwrap(Session.class);
        String sql = "SELECT " +
				/*"CASE  aftersales.status " +
				" WHEN 0 THEN '等待审核' " +
				" WHEN 1 THEN '审核通过' " +
				" WHEN 2 THEN '审核失败' " +
				" WHEN 3 THEN '已完成' " +
				" WHEN 4 THEN '已取消' " +
				" ELSE '异常' " +
				" END AS status, " +*/
                " aftersales.status AS status ," +
                " DATE_FORMAT(aftersalesitem.createdDate,'%Y-%m-%d %H:%i:%s') AS createdDate,aftersalesitem.quantity AS applyNum,orderitem.sn AS sn,users.name AS userName ,users.address,aftersales.reason,(orderitem.price * aftersalesitem.quantity) AS refundMoney , orderitem.id AS orderItemId,orderitem.orders AS orderId FROM  aftersalesitem " +
                " LEFT JOIN aftersales on aftersales.id = aftersalesitem.aftersales_id " +
                " LEFT JOIN orderitem ON orderitem.id = aftersalesitem.orderItem_id " +
                " LEFT JOIN users ON users.id = aftersales.member_id" +
                " WHERE aftersales.member_id = ? ";

        int startIndex = (page - 1) * length;
        List list = session.createSQLQuery(sql)
                .setParameter(0, memberId)
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(startIndex)
                .setMaxResults(length).list();

        return list;
    }

    @Override
    public List<Map<String, Object>> queryRefundCause() {

        Session session = entityManager.unwrap(Session.class);

        String sql = " SELECT * FROM key_value WHERE `key` = 'afterSaleCause' ";
        List list = session.createSQLQuery(sql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();

        return list;
    }

    @Override
    public List<Map<String, Object>> queryAfterSaleRecordByOrderId(Long orderId) {
        Session session = entityManager.unwrap(Session.class);

        String sql = " select  id,status,member_id,store_id AS storeId,description,picture from aftersales WHERE order_id = ? ";
        List list = session.createSQLQuery(sql)
                .setParameter(0, orderId)
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();


        return list;
    }

    /**
     * 保存售后
     *
     * @param afterSaleId
     * @param memberId
     * @param storeId
     * @param status
     * @param afterSaleCause
     * @param des
     * @param pic
     * @param orderId
     */
    @Override
    public void saveAfterSale(String afterSaleId, Long memberId, String storeId, int status, String afterSaleCause, String des, String pic, Long orderId) {
        Session session = entityManager.unwrap(Session.class);

        String sql = " INSERT INTO  aftersales (dtype,id,createdDate,lastModifiedDate, version,reason,status,member_id,store_id,description,picture,order_id) VALUES  (?,?,?,?,?,?,?,?,?,?,?,?) ";

        String myFormatDate = DateUtils.myFormatDate(new Date().toString());
        session.createSQLQuery(sql)
                .setParameter(0, "afterSales")
                .setParameter(1, afterSaleId)
                .setParameter(2, myFormatDate)
                .setParameter(3, myFormatDate)
                .setParameter(4, 1)
                .setParameter(5, afterSaleCause)
                .setParameter(6, 0)
                .setParameter(7, memberId)
                .setParameter(8, storeId)
                .setParameter(9, des)
                .setParameter(10, pic)
                .setParameter(11, orderId).executeUpdate();


    }

    @Override
    public void saveAfterSaleItems(String id, String orderItemId, String afterSaleId, int afterQuantity) {
        Session session = entityManager.unwrap(Session.class);

        String sql = " INSERT INTO  aftersalesitem (id,createdDate,lastModifiedDate, version,quantity,aftersales_id,orderItem_id) VALUES  (?,?,?,?,?,?,?) ";

        String myFormatDate = DateUtils.myFormatDate(new Date().toString());
        session.createSQLQuery(sql)
                .setParameter(0, id)
                .setParameter(1, myFormatDate)
                .setParameter(2, myFormatDate)
                .setParameter(3, 1)
                .setParameter(4, afterQuantity)
                .setParameter(5, afterSaleId)
                .setParameter(6, orderItemId).executeUpdate();

    }

}