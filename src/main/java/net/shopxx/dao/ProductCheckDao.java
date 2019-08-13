/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: MlAsGPQBEmN83fguBG988+AlpEZlqlUX
 */
package net.shopxx.dao;

import net.shopxx.entity.ProductCheck;

/**
 * Dao - 店铺商品盘点
 * 
 */
public interface ProductCheckDao extends BaseDao<ProductCheck, Long> {

    String queryImgBySn(String sn);
}