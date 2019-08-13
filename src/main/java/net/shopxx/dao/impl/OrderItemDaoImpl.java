/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: ew7KN5BAAsObBOsDdjXUY9GLbETP/eG1
 */
package net.shopxx.dao.impl;

import net.shopxx.entity.Order;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import net.shopxx.dao.OrderItemDao;
import net.shopxx.entity.OrderItem;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;

/**
 * Dao - 订单项
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Repository
public class OrderItemDaoImpl extends BaseDaoImpl<OrderItem, Long> implements OrderItemDao {

    @Override
    public List<OrderItem> findAllByUser(List<Order> orders) {
        TypedQuery<OrderItem> query;
        String jpql = "select orderItem from OrderItem orderItem where orderItem.orders in (:orders) ";
        query=entityManager.createQuery(jpql,OrderItem.class).setParameter("orders", orders);
        return query.getResultList();
    }

    @Override
    public List<Map<String, Object>> queryInventoryList(Long storeId,String goodsName,int startIndex ,int length) {
        StringBuffer sql = new StringBuffer("SELECT productcheck.id,productcheck.store_id as storeId,sum(productcheck.count) as count ,countCheck,productcheck.productName as productName ,productcheck.specifications as specifications,img  FROM productcheck ");
        sql.append(" WHERE productcheck.store_id = ? ");
        //昨天的盘点数据
        sql.append(" and date(createdDate) = SUBDATE(CURDATE(), 1) ");
        if (goodsName !=null){
            //商品名
            sql.append(" and productName like concat('%',?,'%')");
        }

        sql.append("GROUP BY CONCAT(productcheck.productName,productcheck.specifications)");
        sql.append(" order by countCheck,productName ");
        //将entityManager 转化成 Hibernate 的 session 执行原生SQL
        Session session = entityManager.unwrap(Session.class);
        //结果集转化成 Map 的 Query
        Query query = session.createSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(startIndex).setMaxResults(length);
        query.setParameter(0,storeId);
        if (goodsName !=null){
            query.setParameter(1,goodsName);
        }
        List<Map<String, Object>> resultList = query.list();
        return resultList;
    }

    @Override
    public void updateProductCheck(int id ,int productCheck) {
        StringBuffer sql = new StringBuffer("UPDATE productcheck SET productcheck.countCheck = ? WHERE id = ? ");
        //将entityManager 转化成 Hibernate 的 session 执行原生SQL
        Session session = entityManager.unwrap(Session.class);
        //结果集转化成 Map 的 Query
        Query query = session.createSQLQuery(sql.toString());
        query.setParameter(0,productCheck);
        query.setParameter(1,id);
        query.executeUpdate();
        //int i = query.executeUpdate();
        //System.err.println(i);
    }

    @Override
    public List findByOrderId(String orderId) {
        String sql= " SELECT orderitem.id AS id,name ,price,orderitem.quantity,specifications,thumbnail,sku_id AS skuId " +
                " from orderitem " +
                " LEFT JOIN aftersalesitem ON aftersalesitem.orderItem_id = orderitem.id " +
                "WHERE " +
                "  orders = ?"+
                " GROUP BY orderitem.id ";

        Session session = entityManager.unwrap(Session.class);

        List<Map<String,Object>> listOrderItem = session.createSQLQuery(sql)
                .setParameter(0, orderId).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .list();
        return listOrderItem ;

    }

    @Override
    public List findById(Long orderItemId) {
        //String sql= " SELECT id AS orderItemId,name ,price,quantity,specifications,thumbnail,sku_id AS skuId from orderitem WHERE id = ?  AND orderitem.id NOT IN ( SELECT  aftersalesitem.orderItem_id FROM aftersalesitem )";
        //String sql= " SELECT id AS orderItemId,name ,price,quantity,specifications,thumbnail,sku_id AS skuId from orderitem WHERE id = ?  ";
        String sql= " SELECT orderitem.id AS orderItemId,name ,price,orderitem.quantity AS allQuantity ,(orderitem.quantity - sum(aftersalesitem.quantity)) AS quantity ,specifications,thumbnail,sku_id AS skuId " +
                " from orderitem " +
                " LEFT JOIN aftersalesitem ON aftersalesitem.orderItem_id = orderitem.id " +
                " WHERE orderitem.id = ?  ";

        Session session = entityManager.unwrap(Session.class);

        List<Map<String,Object>> listOrderItem = session.createSQLQuery(sql)
                .setParameter(0, orderItemId).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .list();
        return listOrderItem ;
    }

    @Override
    public List findByMemberId(Long memberId) {
        return null;
    }

    @Override
    public List findByMemberId1(Long memberId,String orderItemId) {
        // String sql= " SELECT aftersalesitem.* from aftersalesitem LEFT JOIN aftersales ON aftersales.id = aftersalesitem.aftersales_id WHERE member_id = ? ";

        String sql = " SELECT orderitem.`name`,orderitem.price,orderitem.quantity,orderitem.thumbnail,orderitem.specifications,orderitem.sn FROM aftersalesitem  " +
                " LEFT JOIN aftersales ON aftersales.id = aftersalesitem.aftersales_id " +
                " LEFT JOIN orderitem ON orderitem.id = aftersalesitem.orderItem_id " +
                " WHERE " +
                " aftersales.member_id = ? " +
                " AND orderitem.id = ? " ;
        Session session = entityManager.unwrap(Session.class);

        List<Map<String,Object>> listOrderItem = session.createSQLQuery(sql)
                .setParameter(0, memberId)
                .setParameter(1, orderItemId).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .list();
        return listOrderItem ;
    }
}