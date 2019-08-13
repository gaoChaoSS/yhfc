/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: bgt5awDludfsjBEW+J8LwWa9ldZcevdc
 */
package net.shopxx.controller.admin;

import javax.inject.Inject;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.shopxx.Results;
import net.shopxx.service.CacheService;

/**
 * Controller - 缓存
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminCacheController")
@RequestMapping("/admin/cache")
public class CacheController extends BaseController {

	@Inject
	private CacheService cacheService;

	/**
	 * 清除缓存
	 */
	@GetMapping("/clear")
	public String clear(ModelMap model) {
		Long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
		Long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
		Long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
		model.addAttribute("totalMemory", totalMemory);
		model.addAttribute("maxMemory", maxMemory);
		model.addAttribute("freeMemory", freeMemory);
		model.addAttribute("cacheSize", cacheService.getCacheSize());
		model.addAttribute("diskStorePath", cacheService.getDiskStorePath());
		return "admin/cache/clear";
	}

	/**
	 * 清除缓存
	 */
	@PostMapping("/clear")
	public ResponseEntity<?> clear() {
		cacheService.clear();
		return Results.OK;
	}

}