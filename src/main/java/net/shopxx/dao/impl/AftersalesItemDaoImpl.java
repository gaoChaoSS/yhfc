/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: d+bDuq2CL60sE5Z3B/hJBuVHXmwPKJ4S
 */
package net.shopxx.dao.impl;

import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import net.shopxx.dao.AftersalesItemDao;
import net.shopxx.entity.AftersalesItem;

import java.util.List;
import java.util.Map;

/**
 * Dao - 售后项
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Repository
public class AftersalesItemDaoImpl extends BaseDaoImpl<AftersalesItem, Long> implements AftersalesItemDao {

    @Override
    public int queryQuantity(Long orderItemId) {
        String sql= " SELECT IFNULL(SUM(aftersalesitem.quantity),0) AS quantity FROM aftersalesitem WHERE orderItem_id = ? " ;

        Session session = entityManager.unwrap(Session.class);

        List<Map<String,Object>> list = session.createSQLQuery(sql)
                .setParameter(0, orderItemId).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list() ;
        return Integer.valueOf(list.get(0).get("quantity").toString()) ;

    }
}