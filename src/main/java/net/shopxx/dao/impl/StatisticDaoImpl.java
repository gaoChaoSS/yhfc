/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: esUuIIXTQ2fPkpv5MbGJe8bWiE5CBMqW
 */
package net.shopxx.dao.impl;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.dao.StatisticDao;
import net.shopxx.entity.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.*;


/**
 * Dao - 统计
 *
 * @author SHOP++ Team
 * @version 6.1
 */
@Repository
public class StatisticDaoImpl extends BaseDaoImpl<Statistic, Long> implements StatisticDao {

    @Override
    public boolean exists(Statistic.Type type, Store store, int year, int month, int day) {
        Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");

        if (store != null) {
            String jpql = "select count(*) from Statistic statistic where statistic.type = :type and statistic.year = :year and statistic.month = :month and statistic.day = :day and statistic.store = :store";
            return entityManager.createQuery(jpql, Long.class).setParameter("type", type).setParameter("year", year).setParameter("month", month).setParameter("day", day).setParameter("store", store).getSingleResult() > 0;
        } else {
            String jpql = "select count(*) from Statistic statistic where statistic.type = :type and statistic.year = :year and statistic.month = :month and statistic.day = :day and statistic.store is null";
            return entityManager.createQuery(jpql, Long.class).setParameter("type", type).setParameter("year", year).setParameter("month", month).setParameter("day", day).getSingleResult() > 0;
        }
    }

    @Override
    public List<Statistic> analyze(Statistic.Type type, Store store, Statistic.Period period, Date beginDate, Date endDate) {
        Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");
        Assert.notNull(period, "[Assertion failed] - period is required; it must not be null");

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Statistic> criteriaQuery = criteriaBuilder.createQuery(Statistic.class);
        Root<Statistic> root = criteriaQuery.from(Statistic.class);
        Predicate restrictions = criteriaBuilder.conjunction();
        switch (type) {
            case REGISTER_MEMBER_COUNT:
            case REGISTER_BUSINESS_COUNT:
            case CREATE_ORDER_COUNT:
            case COMPLETE_ORDER_COUNT:
            case CREATE_ORDER_AMOUNT:
            case COMPLETE_ORDER_AMOUNT:
            case ADDED_MEMBER_CASH:
            case ADDED_BUSINESS_CASH:
            case ADDED_BAIL:
            case ADDED_PLATFORM_COMMISSION:
            case ADDED_DISTRIBUTION_COMMISSION: {
                switch (period) {
                    case YEAR:
                        criteriaQuery.select(criteriaBuilder.construct(Statistic.class, root.get("type"), root.get("year"), criteriaBuilder.sum(root.<BigDecimal>get("value"))));
                        criteriaQuery.groupBy(root.get("type"), root.get("year"));
                        break;
                    case MONTH:
                        criteriaQuery.select(criteriaBuilder.construct(Statistic.class, root.get("type"), root.get("year"), root.get("month"), criteriaBuilder.sum(root.<BigDecimal>get("value"))));
                        criteriaQuery.groupBy(root.get("type"), root.get("year"), root.get("month"));
                        break;
                    case DAY:
                        criteriaQuery.select(criteriaBuilder.construct(Statistic.class, root.get("type"), root.get("year"), root.get("month"), root.get("day"), root.<BigDecimal>get("value")));
                        break;
                }
                break;
            }
            case MEMBER_BALANCE:
            case MEMBER_FROZEN_AMOUNT:
            case MEMBER_CASH:
            case BUSINESS_BALANCE:
            case BUSINESS_FROZEN_AMOUNT:
            case BUSINESS_CASH:
            case BAIL:
            case PLATFORM_COMMISSION:
            case DISTRIBUTION_COMMISSION: {
                Subquery<Statistic> subquery = criteriaQuery.subquery(Statistic.class);
                Root<Statistic> subqueryRoot = subquery.from(Statistic.class);
                switch (period) {
                    case YEAR:
                        subquery.select(subqueryRoot);
                        subquery.where(criteriaBuilder.equal(subqueryRoot.get("type"), root.get("type")), criteriaBuilder.equal(subqueryRoot.get("year"), root.get("year")), criteriaBuilder.or(criteriaBuilder.greaterThan(subqueryRoot.<Integer>get("month"), root.<Integer>get("month")),
                                criteriaBuilder.and(criteriaBuilder.equal(subqueryRoot.get("month"), root.get("month")), criteriaBuilder.greaterThan(subqueryRoot.<Integer>get("day"), root.<Integer>get("day")))));
                        restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.exists(subquery).not());
                        break;
                    case MONTH:
                        subquery.select(subqueryRoot);
                        subquery.where(criteriaBuilder.equal(subqueryRoot.get("type"), root.get("type")), criteriaBuilder.equal(subqueryRoot.get("year"), root.get("year")), criteriaBuilder.equal(subqueryRoot.get("month"), root.get("month")),
                                criteriaBuilder.greaterThan(subqueryRoot.<Integer>get("day"), root.<Integer>get("day")));
                        restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.exists(subquery).not());
                        break;
                    case DAY:
                        break;
                }
                break;
            }
        }
        restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("type"), type));
        if (store != null) {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("store"), store));
        } else {
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.isNull(root.get("store")));
        }
        if (beginDate != null) {
            Calendar calendar = DateUtils.toCalendar(beginDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.or(criteriaBuilder.greaterThan(root.<Integer>get("year"), year), criteriaBuilder.and(criteriaBuilder.equal(root.<Integer>get("year"), year), criteriaBuilder.greaterThan(root.<Integer>get("month"), month)),
                    criteriaBuilder.and(criteriaBuilder.equal(root.<Integer>get("year"), year), criteriaBuilder.equal(root.<Integer>get("month"), month), criteriaBuilder.greaterThanOrEqualTo(root.<Integer>get("day"), day))));
        }
        if (endDate != null) {
            Calendar calendar = DateUtils.toCalendar(endDate);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.or(criteriaBuilder.lessThan(root.<Integer>get("year"), year), criteriaBuilder.and(criteriaBuilder.equal(root.<Integer>get("year"), year), criteriaBuilder.lessThan(root.<Integer>get("month"), month)),
                    criteriaBuilder.and(criteriaBuilder.equal(root.<Integer>get("year"), year), criteriaBuilder.equal(root.<Integer>get("month"), month), criteriaBuilder.lessThanOrEqualTo(root.<Integer>get("day"), day))));
        }
        criteriaQuery.where(restrictions);
        return entityManager.createQuery(criteriaQuery).getResultList();
    }


    @Override
    public List<OrderCountByStore> ordersCountByStore(String beginDate, String endDate, Area area, Boolean isAreaChildren, Store store) {
        StringBuffer sqlString = new StringBuffer("select store.id,store.name as storeName ,it.name as productName,it.sn,it.specifications,sum(it.quantity) as count,ord.`status` " +
                " from orders ord");
        sqlString.append(" LEFT JOIN store store on store.id = ord.store_id");
        sqlString.append(" LEFT JOIN orderitem it on ord.id = it.orders");
        sqlString.append(" where ");
        sqlString.append(" (ord.status = 2 or ord.status = 3 or ord.status = 5)"); //2：已付款，3:已发货,5:取货已完成
        sqlString.append(" and DATE_FORMAT(ord.payTime,'%Y-%m-%d %H:%i:%s') >= '").append(beginDate).append("'");
        sqlString.append(" and");
        sqlString.append(" DATE_FORMAT(ord.payTime,'%Y-%m-%d %H:%i:%s') <= '").append(endDate).append("'");
        sqlString.append(" group by store.id,sn,`status`");


        Query query = entityManager.createNativeQuery(sqlString.toString());
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.aliasToBean(OrderCountByStore.class));
        List<OrderCountByStore> list = query.getResultList();
        return list;
    }

    @Override
    public List<OrderBuy> orderBuyList(String beginDate, String endDate) {

        StringBuffer sqlString = new StringBuffer("select it.sku_id,pc.id as productCategory_id,pc.treePath,su.supplierName,pc.`name` as productCategory,p.`name` as productName_now, it.name as productName_buy,it.sn,it.specifications,sum(it.quantity) as count,ord.`status` " +
                " from orders ord ");
        sqlString.append(" LEFT JOIN orderitem it on ord.id = it.orders ");
        sqlString.append(" LEFT JOIN sku s on s.id = it.sku_id ");
        sqlString.append(" LEFT JOIN product p on p.id = s.product_id ");
        sqlString.append(" LEFT JOIN productcategory pc on p.productCategory_id = pc.id ");
        sqlString.append(" LEFT JOIN supplier su on su.id = p.supplier");
        sqlString.append(" where ");
        sqlString.append(" (ord.status = 2 or ord.status = 3 or ord.status = 5)");//2：已付款，3:已发货,5:取货已完成
        sqlString.append(" and ");
        sqlString.append(" DATE_FORMAT(ord.createdDate,'%Y-%m-%d %H:%i:%s') >= '").append(beginDate).append("'");
        ;
        sqlString.append(" and ");
        sqlString.append(" DATE_FORMAT(ord.createdDate,'%Y-%m-%d %H:%i:%s') <= '").append(endDate).append("'");
        sqlString.append(" group by sn,`status`");
        sqlString.append(" order by productCategory_id,productName_buy ");

        Query query = entityManager.createNativeQuery(sqlString.toString());
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.aliasToBean(OrderBuy.class));
        List<OrderBuy> list = query.getResultList();
        return list;
    }

    @Override
    public int countUser(String beginDate, String endDate) {

        String sql = "SELECT  count( 1 )  FROM users WHERE createdDate >=? " +
                "AND createdDate <= ?  ";


        Session session = entityManager.unwrap(Session.class);

        SQLQuery countquery = session.createSQLQuery(sql);
        countquery.setParameter(0, beginDate);
        countquery.setParameter(1, endDate);

        return Integer.parseInt(String.valueOf(countquery.uniqueResult()));
    }

    @Override
    public Page<Map<String, Object>> analyzeMemberListPage(Statistic.Type type, Statistic.Period period, String beginDate, String endDate, Pageable pageable) {

        String countsql = "SELECT count(1)" +
                " FROM statistic WHERE type = 0 " +
                " AND str_to_date(CONCAT( MONTH, '/', DAY, '/', YEAR ), '%m/%d/%Y' ) >= ? " +
                " AND str_to_date(CONCAT( MONTH, '/', DAY, '/', YEAR ), '%m/%d/%Y' ) <= ? ";


        String sql = "SELECT str_to_date(CONCAT( MONTH + 1, '/', DAY, '/', YEAR ), '%m/%d/%Y' ) cdate," +
                " VALUE num  FROM statistic WHERE type = 0 " +
                " AND str_to_date(CONCAT( MONTH, '/', DAY, '/', YEAR ), '%m/%d/%Y' ) >= ?  " +
                " AND str_to_date(CONCAT( MONTH, '/', DAY, '/', YEAR ), '%m/%d/%Y' ) <= ?  order by cdate desc limit ?,?";

        Session session = entityManager.unwrap(Session.class);

        SQLQuery countquery = session.createSQLQuery(countsql);
        countquery.setParameter(0, beginDate);
        countquery.setParameter(1, endDate);
        Integer totalcount = Integer.parseInt(String.valueOf(countquery.uniqueResult()));

        SQLQuery query = session.createSQLQuery(sql);
        query.setParameter(0, beginDate);
        query.setParameter(1, endDate);
        query.setParameter(2, pageable.getPageNumber() == 1 ? 0 : pageable.getPageNumber());
        query.setParameter(3, pageable.getPageSize());
        query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        List<Map<String, Object>> countList = query.list();

        return new Page<>(countList == null ? new ArrayList<Map<String, Object>>() : countList, Long.parseLong(totalcount + ""), pageable);

    }


    @Override
    public Map<String, Object> analyzeTotalOrderCount(String type, String beginDate, String endDate) {


        //订单数量统计
        String countOrderNumSql = "SELECT " +
                " IFNULL(count( CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN 2 END ), 0 ) totalyxnum," +
                " IFNULL(count( CASE WHEN o.`status` NOT IN ( 2, 3, 5 ) THEN 0 END ), 0 ) totalwxnum," +
                " count( 1 ) tnums  FROM  " +
                " orders o  WHERE " +
                " o.createdDate >= ? " +
                " and o.createdDate <= ?";

        Session session = entityManager.unwrap(Session.class);

        SQLQuery query = session.createSQLQuery(countOrderNumSql);
        query.setParameter(0, beginDate);
        query.setParameter(1, endDate);
        query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List list = query.list();
        return list != null ? (Map) list.get(0) : null;
    }

    @Override
    public List<Map<String, Object>> analyzeTotalPaidCount(String type, String beginDate, String endDate) {
        //店铺销售top 10
        String countStorePaidSql = "SELECT " +
//                " s.id storeId," +
                " s.`name`," +
                " IFNULL(sum(case  when o.`status` in(2,3,4,5) then  o.amountPaid end),0.00) `value` " +
                " from  orders o LEFT JOIN store s on o.store_id=s.id " +
                " where o.createdDate >=? " +
                " and o.createdDate <=? " +
                " GROUP BY o.store_id order by `value` desc limit 10 ";
        Session session = entityManager.unwrap(Session.class);

        SQLQuery query = session.createSQLQuery(countStorePaidSql);
        query.setParameter(0, beginDate);
        query.setParameter(1, endDate);
        query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        return query.list();
    }

    @Override
    public Page<Map<String, Object>> analyzeListPage(String type, Integer storeId, String beginDate, String endDate, Pageable pageable) {

        String sql = getSql(type);

        if (StringUtils.isBlank(sql)) {
            return new Page<>();
        }

        String countSql = "select count(1) from ( " + sql + " )tc ";

        Session session = entityManager.unwrap(Session.class);
        SQLQuery countquery = session.createSQLQuery(countSql);
        if (type.equals("4") && storeId != null) {
            countquery.setParameter(0, storeId);
            countquery.setParameter(1, beginDate);
            countquery.setParameter(2, endDate);
            countquery.setParameter(3, storeId);
            countquery.setParameter(4, beginDate);
            countquery.setParameter(5, endDate);
        } else {
            countquery.setParameter(0, beginDate);
            countquery.setParameter(1, endDate);
            countquery.setParameter(2, beginDate);
            countquery.setParameter(3, endDate);
        }

        Integer totalcount = Integer.parseInt(String.valueOf(countquery.uniqueResult()));

        sql = sql + "  limit ?,?";//增加分页参数

        SQLQuery query = session.createSQLQuery(sql);
        if (type.equals("4") && storeId != null) {
            query.setParameter(0, storeId);
            query.setParameter(1, beginDate);
            query.setParameter(2, endDate);
            query.setParameter(3, storeId);
            query.setParameter(4, beginDate);
            query.setParameter(5, endDate);
            query.setParameter(6, pageable.getPageNumber() == 1 ? 0 : pageable.getPageNumber());
            query.setParameter(7, pageable.getPageSize());
        } else {
            query.setParameter(0, beginDate);
            query.setParameter(1, endDate);
            query.setParameter(2, beginDate);
            query.setParameter(3, endDate);
            query.setParameter(4, pageable.getPageNumber() == 1 ? 0 : pageable.getPageNumber());
            query.setParameter(5, pageable.getPageSize());
        }

        query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        List<Map<String, Object>> countList = query.list();

        return new Page<>(countList, Long.parseLong(totalcount + ""), pageable);

    }


    private String getSql(String type) {
        String sql = "";
        if (StringUtils.isBlank(type)) {
            return sql;
        }

        if (type.equals("1")) {
            sql = " SELECT   t.cdate,sum(t.yxnum)yxnum,sum(t.wxnum)wxnum,sum(t.tnums)tnums," +
                    "FORMAT( IFNULL(sum(t.tpaids) / sum(mnums),0) , 3 ) coprice," +
                    "sum(t.mnums)mnums,sum(t.gmv)gmv,sum(t.tpaids)tpaids  " +
                    " FROM " +
                    " (SELECT   DATE_FORMAT( o.createdDate, '%Y-%m-%d' ) cdate," +
                    "0 yxnum," +
                    "IFNULL( count( CASE WHEN o.`status` NOT IN ( 2, 3, 5 ) THEN 0 END ), 0 ) wxnum," +
                    "count( 1 ) tnums," +
                    "  0 mnums," +
                    "IFNULL( sum( o.amount ), 0.00 ) gmv," +
                    "0 tpaids " +
                    " FROM " +
                    "orders o" +
                    " WHERE" +
                    " o.createdDate >=? " +
                    " and o.createdDate <=? " +
                    " GROUP BY " +
                    " cdate " +
                    " UNION  all  " +
                    " SELECT   DATE_FORMAT( o.createdDate, '%Y-%m-%d' ) cdate," +
                    "IFNULL( count( CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN 2 END ), 0 ) yxnum," +
                    "0 wxnum," +
                    "0 tnums," +
                    "count(DISTINCT(CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN o.member_id END )) mnums," +
                    "0 gmv," +
                    " IFNULL( sum( CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN o.amountPaid END ), 0.00 ) tpaids " +
                    " FROM   orders o WHERE  o.payTime >=? " +
                    " and o.payTime <=? GROUP BY cdate  ) t GROUP BY cdate  ORDER BY  cdate  desc";

        } else if (type.equals("2")) {

            sql = " SELECT   t.name," +
                    "sum(t.yxnum)yxnum," +
                    "sum(t.wxnum)wxnum," +
                    "sum(t.tnums)tnums," +
                    "FORMAT( IFNULL(sum(t.tpaids) / sum(mnums),0) , 3 ) coprice," +
                    "sum(t.mnums)mnums," +
                    "sum(t.gmv)gmv," +
                    "sum(t.tpaids)tpaids " +
                    " FROM " +
                    "(" +
                    "  SELECT  " +
                    "s.name," +
                    "o.store_id," +
                    "0 yxnum," +
                    "IFNULL( count( CASE WHEN o.`status` NOT IN ( 2, 3, 5 ) THEN 0 END ), 0 ) wxnum," +
                    "count( 1 ) tnums," +
                    "  0 mnums," +
                    "IFNULL( sum( o.amount ), 0.00 ) gmv," +
                    "0 tpaids  " +
                    " FROM  orders o  " +
                    " LEFT JOIN store s ON o.store_id = s.id" +
                    " WHERE  o.createdDate >=?  " +
                    " and o.createdDate <=?  " +
                    " GROUP BY   " +
                    " o.store_id   " +
                    " UNION  all  " +
                    " SELECT " +
                    "  s.name, o.store_id," +
                    "IFNULL( count( CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN 2 END ), 0 ) yxnum," +
                    "0 wxnum," +
                    "0 tnums," +
                    " count(DISTINCT(CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN o.member_id END )) mnums," +
                    "0 gmv," +
                    "IFNULL( sum( CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN o.amountPaid END ), 0.00 ) tpaids  " +
                    " FROM   orders o  " +
                    " LEFT JOIN store s ON o.store_id = s.id" +
                    " WHERE o.payTime >=?  " +
                    " and o.payTime <=?  " +
                    " GROUP BY  o.store_id   )t  " +
                    " GROUP BY  name  " +
                    " ORDER BY  tpaids  desc ";

        } else if (type.equals("3")) {

            sql = "SELECT t.id,t.name,sum(t.yxnum)yxnum,sum(t.wxnum)wxnum,sum(t.tnums)tnums,FORMAT(IFNULL(sum(t.tpaids)/sum(mnums),0),3) coprice,sum(t.gmv)gmv, sum(t.tpaids)tpaids  " +
                    " from  ( " +
                    "SELECT  " +
                    "s.`name`,s.id, " +
                    "0 yxnum, " +
                    "IFNULL(count(case  when o.`status` not in(2,3,5) then  0 end ),0)wxnum, " +
                    "count(1) tnums, " +
                    "0 mnums, " +
                    "IFNULL(sum(o.amount),0.00) gmv, " +
                    "0  tpaids " +
                    "from  orders o LEFT JOIN store s on o.store_id=s.id  where o.createdDate >=?  " +
                    " and o.createdDate <=?   GROUP BY o.store_id " +
                    " union all  " +
                    " SELECT     " +
                    "s.`name`,s.id, " +
                    "IFNULL(count(case  when o.`status` in(2,3,5) then  2 end),0)yxnum, " +
                    "0 wxnum, " +
                    "0  tnums, " +
                    "count(DISTINCT(CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN o.member_id END )) mnums, " +
                    "0 gmv, " +
                    "IFNULL(sum(case  when o.`status` in(2,3,5) then  o.amountPaid end),0.00) tpaids " +
                    " from  orders o LEFT JOIN store s on o.store_id=s.id  where o.payTime >=?   " +
                    " and o.payTime <=?   GROUP BY o.store_id " +
                    " ) t GROUP BY name,id order by tpaids desc";

        } else if (type.equals("4")) {

            sql = " SELECT t.cdate,sum(t.yxnum)yxnum,sum(t.wxnum)wxnum,sum(t.tnums)tnums,FORMAT(IFNULL(sum(t.tpaids)/sum(mnums),0),3) coprice,sum(t.gmv)gmv, " +
                    "sum(t.tpaids)tpaids    from  ( " +
                    " SELECT    s.`name`, " +
                    " s.id, " +
                    "DATE_FORMAT( o.createdDate, '%Y-%m-%d' ) cdate, " +
                    "0 yxnum, " +
                    "IFNULL(count(case  when o.`status` not in(2,3,5) then  0 end ),0)wxnum, " +
                    "count(1) tnums, " +
                    "0 mnums, " +
                    "IFNULL(sum(o.amount),0.00) gmv, " +
                    "0  tpaids " +
                    "from  orders o LEFT JOIN store s on o.store_id=s.id   where o.store_id=?   " +
                    "AND o.createdDate >= ?   AND o.createdDate <= ?   " +
                    " GROUP BY cdate " +
                    " union all  " +
                    " SELECT    s.`name`,  s.id, " +
                    " DATE_FORMAT( o.createdDate, '%Y-%m-%d' ) cdate, " +
                    "IFNULL(count(case  when o.`status` in(2,3,5) then  2 end),0)yxnum, " +
                    "0 wxnum, " +
                    "0 tnums, " +
                    "count(DISTINCT(CASE WHEN o.`status` IN ( 2, 3, 5 ) THEN o.member_id END )) mnums, " +
                    "0  gmv, " +
                    "IFNULL(sum(case  when o.`status` in(2,3,5) then  o.amountPaid end),0.00) tpaids " +
                    " from  orders o LEFT JOIN store s on o.store_id=s.id   where o.store_id=?    " +
                    " AND o.payTime >= ?    AND o.payTime <= ?    " +
                    " GROUP BY cdate ) t GROUP BY cdate order by cdate desc";

        }


        return sql;
    }


}