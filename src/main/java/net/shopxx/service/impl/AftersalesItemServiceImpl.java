/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: SF7ZwNetnt76h+Y3vQmlzn+00ijff/nW
 */
package net.shopxx.service.impl;

import net.shopxx.dao.AftersalesItemDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.shopxx.entity.AftersalesItem;
import net.shopxx.service.AftersalesItemService;

/**
 * Service - 售后项
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Service
public class AftersalesItemServiceImpl extends BaseServiceImpl<AftersalesItem, Long> implements AftersalesItemService {

    @Autowired
    private AftersalesItemDao aftersalesItemDao ;

    @Override
    public int queryQuantity(Long orderItemId) {
        return  aftersalesItemDao.queryQuantity(orderItemId);
    }
}