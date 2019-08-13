/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: c3w7uWCnZJtLBrahptnAfrc9+xYawSMc
 */
package net.shopxx.service;

import net.shopxx.entity.AftersalesReturns;
import org.apache.poi.ss.formula.functions.T;

/**
 * Service - 退货
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface AftersalesReturnsService extends BaseService<AftersalesReturns, Long> {

    /**
     * 持久化实体对象
     *            实体对象
     */
    void persist(AftersalesReturns aftersalesReturns);

}