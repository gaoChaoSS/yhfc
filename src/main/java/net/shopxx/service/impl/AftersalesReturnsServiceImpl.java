/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: Pgaxu6GKQ4tIM5kJO+6B9TazfCj3uw2V
 */
package net.shopxx.service.impl;

import net.shopxx.dao.AftersalesReturnsDao;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import net.shopxx.entity.AftersalesReturns;
import net.shopxx.service.AftersalesReturnsService;

import javax.inject.Inject;

/**
 * Service - 退货
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Service
public class AftersalesReturnsServiceImpl extends BaseServiceImpl<AftersalesReturns, Long> implements AftersalesReturnsService {

    @Inject
    private AftersalesReturnsDao aftersalesReturnsDao;

    @Override
    public void persist(AftersalesReturns aftersalesReturns) {
        aftersalesReturnsDao.persist(aftersalesReturns);
    }
}