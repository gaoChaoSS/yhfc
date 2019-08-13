/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: BuK21sBT5IURwLSFw/WnDfaE+LqgWZ9E
 */
package net.shopxx.controller.business;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import net.shopxx.Results;
import net.shopxx.entity.Business;
import net.shopxx.entity.BusinessAttribute;
import net.shopxx.security.CurrentUser;
import net.shopxx.service.BusinessAttributeService;
import net.shopxx.service.BusinessService;

/**
 * Controller - 个人资料
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("businessProfileController")
@RequestMapping("/business/profile")
public class ProfileController extends BaseController {

	@Inject
	private BusinessService businessService;
	@Inject
	private BusinessAttributeService businessAttributeService;

	/**
	 * 检查E-mail是否唯一
	 */
	@GetMapping("/check_email")
	public @ResponseBody boolean checkEmail(String email, @CurrentUser Business currentUser) {
		return StringUtils.isNotEmpty(email) && businessService.emailUnique(currentUser.getId(), email);
	}

	/**
	 * 检查手机是否唯一
	 */
	@GetMapping("/check_mobile")
	public @ResponseBody boolean checkMobile(String mobile, @CurrentUser Business currentUser) {
		return StringUtils.isNotEmpty(mobile) && businessService.mobileUnique(currentUser.getId(), mobile);
	}

	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(ModelMap model) {
		return "business/profile/edit";
	}

	/**
	 * 更新
	 */
	@PostMapping("/update")
	public ResponseEntity<?> update(String email, String mobile, @CurrentUser Business currentUser, HttpServletRequest request) {
		if (!isValid(Business.class, "email", email) || !isValid(Business.class, "mobile", mobile)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (!businessService.emailUnique(currentUser.getId(), email)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (!businessService.mobileUnique(currentUser.getId(), mobile)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		currentUser.setEmail(email);
		currentUser.setMobile(mobile);
		currentUser.removeAttributeValue();
		for (BusinessAttribute businessAttribute : businessAttributeService.findList(true, true)) {
			String[] values = request.getParameterValues("businessAttribute_" + businessAttribute.getId());
			if (!businessAttributeService.isValid(businessAttribute, values)) {
				return Results.UNPROCESSABLE_ENTITY;
			}
			Object businessAttributeValue = businessAttributeService.toBusinessAttributeValue(businessAttribute, values);
			currentUser.setAttributeValue(businessAttribute, businessAttributeValue);
		}
		businessService.update(currentUser);

		return Results.OK;
	}

}