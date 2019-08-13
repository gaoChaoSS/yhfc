/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: 9OCV7ZFhKAHSB3GfZbIHGTBLKxDwUSG0
 */
package net.shopxx.controller.admin;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.entity.NavigationGroup;
import net.shopxx.service.NavigationGroupService;

/**
 * Controller - 导航组
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminNavigationGroupController")
@RequestMapping("/admin/navigation_group")
public class NavigationGroupController extends BaseController {

	@Inject
	private NavigationGroupService navigationGroupService;

	/**
	 * 添加
	 */
	@GetMapping("/add")
	public String navigationGroupd(ModelMap model) {
		return "admin/navigation_group/add";
	}

	/**
	 * 保存
	 */
	@PostMapping("/save")
	public ResponseEntity<?> save(NavigationGroup navigationGroup) {
		if (!isValid(navigationGroup)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		navigationGroup.setNavigations(null);
		navigationGroupService.save(navigationGroup);
		return Results.OK;
	}

	/**
	 * 编辑
	 */
	@GetMapping("/edit")
	public String edit(Long id, ModelMap model) {
		model.addAttribute("navigationGroup", navigationGroupService.find(id));
		return "admin/navigation_group/edit";
	}

	/**
	 * 更新
	 */
	@PostMapping("/update")
	public ResponseEntity<?> update(NavigationGroup navigationGroup, Long navigationGroupId) {
		if (!isValid(navigationGroup)) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		navigationGroupService.update(navigationGroup, "navigations");
		return Results.OK;
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(Pageable pageable, ModelMap model) {
		model.addAttribute("page", navigationGroupService.findPage(pageable));
		return "admin/navigation_group/list";
	}

	/**
	 * 删除
	 */
	@PostMapping("/delete")
	public ResponseEntity<?> delete(Long[] ids) {
		navigationGroupService.delete(ids);
		return Results.OK;
	}

}