/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: 2qhlYFjA0oN+ouJ12SQosSeoM3rXoItD
 */
package net.shopxx.template.directive;

import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import net.shopxx.entity.CategoryApplication;
import net.shopxx.service.CategoryApplicationService;
import net.shopxx.util.FreeMarkerUtils;

/**
 * 模板指令 - 经营分类申请数量
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Component
public class CategoryApplicationCountDirective extends BaseDirective {

	/**
	 * "状态"参数名称
	 */
	private static final String STATUS_PARAMETER_NAME = "status";

	/**
	 * "店铺ID"参数名称
	 */
	private static final String STORE_ID_PARAMETER_NAME = "storeId";

	/**
	 * "商品分类ID"参数名称
	 */
	private static final String PRODUCT_CATEGORY_PARAMETER_NAME = "productCategoryId";

	/**
	 * 变量名称
	 */
	private static final String VARIABLE_NAME = "count";

	@Inject
	private CategoryApplicationService categoryApplicationService;

	/**
	 * 执行
	 * 
	 * @param env
	 *            环境变量
	 * @param params
	 *            参数
	 * @param loopVars
	 *            循环变量
	 * @param body
	 *            模板内容
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		CategoryApplication.Status status = FreeMarkerUtils.getParameter(STATUS_PARAMETER_NAME, CategoryApplication.Status.class, params);
		Long storeId = FreeMarkerUtils.getParameter(STORE_ID_PARAMETER_NAME, Long.class, params);
		Long productCategoryId = FreeMarkerUtils.getParameter(PRODUCT_CATEGORY_PARAMETER_NAME, Long.class, params);

		Long count = categoryApplicationService.count(status, storeId, productCategoryId);
		setLocalVariable(VARIABLE_NAME, count, env, body);
	}

}