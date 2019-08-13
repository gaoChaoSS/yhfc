/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: 22YIxwI+eUhJbwDWcIavVBs0O7OkiDj9
 */
package net.shopxx.service;

import net.shopxx.entity.ProductCheck;

/**
 * Service - 店铺盘点
 * 
 */
public interface ProductCheckService extends BaseService<ProductCheck, Long> {

    //通过产品sn 查询产品图片
    String queryImgBySn(String sn);
}