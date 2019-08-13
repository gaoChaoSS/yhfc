/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: ZrWQ84uQjiTYfd7vvdvRZ51ncP9Yky3m
 */
package net.shopxx.controller.admin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.shopxx.plugin.sendPlugin.SendSmsPluginFactory;
import net.shopxx.service.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.sf.json.JSONObject;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.Setting;
import net.shopxx.entity.Aftersales;
import net.shopxx.entity.AftersalesItem;
import net.shopxx.entity.Order;
import net.shopxx.util.RefundUtils;
import net.shopxx.util.SystemUtils;

/**
 * Controller - 售后
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Controller("adminAftersalesController")
@RequestMapping("admin/aftersales")
public class AftersalesController extends BaseController {

	private final static Logger logger = LoggerFactory.getLogger(AftersalesController.class);

	@Inject
	private AftersalesService aftersalesService;
	@Inject
	private OrderShippingService orderShippingService;
	@Inject
	private OrderService orderService;
	@Inject
	private SendSmsPluginFactory sendSmsPluginFactory;
	@Autowired
    private RefundUtils refundUtils;
	

	/**
	 * 添加属性
	 */
	@ModelAttribute
	public void populateModel(Long aftersalesId, ModelMap model) {
		Aftersales aftersales = aftersalesService.find(aftersalesId);
		model.addAttribute("aftersales", aftersales);
	}

	/**
	 * 物流动态
	 */
	@GetMapping("/transit_step")
	public ResponseEntity<?> transitStep(@ModelAttribute(binding = false) Aftersales aftersales) {
		Map<String, Object> data = new HashMap<>();

		Setting setting = SystemUtils.getSetting();
		if (StringUtils.isEmpty(setting.getKuaidi100Customer()) || StringUtils.isEmpty(setting.getKuaidi100Key()) || StringUtils.isEmpty(aftersales.getDeliveryCorpCode()) || StringUtils.isEmpty(aftersales.getTrackingNo())) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		data.put("transitSteps", orderShippingService.getTransitSteps(aftersales.getDeliveryCorpCode(), aftersales.getTrackingNo()));
		return ResponseEntity.ok(data);
	}

	/**
	 * 列表
	 */
	@GetMapping("/list")
	public String list(Aftersales.Type type, Aftersales.Status status, Pageable pageable, ModelMap model) {
		model.addAttribute("types", Aftersales.Type.values());
		model.addAttribute("statuses", Aftersales.Status.values());
		model.addAttribute("type", type);
		model.addAttribute("status", status);
		model.addAttribute("page", aftersalesService.findPage(type, status, null, null, pageable));
		return "admin/aftersales/list";
	}

	/**
	 * 查看
	 */
	@GetMapping("/view")
	public String view(@ModelAttribute(binding = false) Aftersales aftersales, ModelMap model) {
		if (aftersales == null) {
			return UNPROCESSABLE_ENTITY_VIEW;
		}
		if (CollectionUtils.isEmpty(aftersales.getOrderItems())) {
			return UNPROCESSABLE_ENTITY_VIEW;
		}

		Setting setting = SystemUtils.getSetting();
		model.addAttribute("isKuaidi100Enabled", StringUtils.isNotEmpty(setting.getKuaidi100Customer()) && StringUtils.isNotEmpty(setting.getKuaidi100Key()));
		model.addAttribute("aftersales", aftersales);
		return "admin/aftersales/view";
	}

	/**
	 * 审核
	 */
	@PostMapping("/review")
	public ResponseEntity<?> review(@ModelAttribute(binding = false) Aftersales aftersales, String reviewRs,boolean storeConfirm,String checkReason,HttpServletRequest request) {
		if (aftersales == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (!Aftersales.Status.PENDING.equals(aftersales.getStatus())) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		aftersales.setCheckReason(checkReason);
//		aftersalesService.review(aftersales, passed,storeConfirm);
		Order order = orderService.find(aftersales.getOrderId());
		if("APPROVED".equals(reviewRs)) {//直接退款(审核通过，无需退货)
			//退款表存数据
			List<AftersalesItem> afterItem = aftersales.getAftersalesItems();
			BigDecimal totalPrice = new BigDecimal(0);
			for(AftersalesItem item:afterItem)
			{
				BigDecimal orderItem = item.getOrderItem().getPrice().multiply(new BigDecimal(item.getQuantity()));
				totalPrice = totalPrice.add(orderItem);
				totalPrice = totalPrice.setScale(2,BigDecimal.ROUND_HALF_DOWN);
			}
			
			String refundNum = net.shopxx.util.StringUtils.getId();
			//调用退款接口
			 JSONObject data = refundUtils.refundByOrderId(request,order.getId().toString(),order.getSn(), refundNum,  order.getAmountPaid(), totalPrice, "售后退款-已审核通过");
			 if (data.get("status").equals("OK")) {
				 aftersales.setStatus(Aftersales.Status.MONEY_APPROVED);
				 aftersalesService.update(aftersales);
				 //发送通知短信 
				
				 String mobile = aftersales.getMember().getMobile();
				 if(!StringUtils.isEmpty(mobile))
				 {
					 String msm = getAfterMoneyMessage(order,totalPrice);
                     sendSmsPluginFactory.getSendSmsPlugin().sendSms(msm, mobile, null,true);
					 logger.info(msm);
				 }
			 }
			 else
			 {
				 logger.info("退款失败");
				 aftersales.setStatus(Aftersales.Status.BACK_MONEY_FAILED);
				 aftersalesService.update(aftersales);
			 }
		}
		if("STORERCONFIRM".equals(reviewRs)){//退货(退货接口会操作退货表)+退款(店长端确认取货的时候会操作退货表)
			//给用户发送提醒短信
			String mobile = aftersales.getMember().getMobile();
			if(!StringUtils.isEmpty(mobile))
                sendSmsPluginFactory.getSendSmsPlugin().sendSms(getAfterProductMessage(order,3), mobile, null,true);
			aftersales.setStatus(Aftersales.Status.STORERCONFIRM);
			aftersalesService.update(aftersales);
		}
		
		if("FAILED".equals(reviewRs))
		{//审核失败
			 aftersales.setStatus(Aftersales.Status.FAILED);
			 aftersalesService.update(aftersales);
		}
		
		return Results.OK;
	}
	
	private String getAfterProductMessage(Order order,Integer limitDay)
	{
		String address = order.getStore().getAddress();
		String sn = order.getSn();
		sn = sn.substring(sn.length()-5, sn.length());
		String afterProduct = "退货通知：亲爱的用户您好，您尾号"+sn+"的订单售后申请已通过，请于"+limitDay+"日内退回货物，以避免超时售后通道关闭无法退款。退货地址："+address;
		return afterProduct;
	}
	
	private String getAfterMoneyMessage(Order order,BigDecimal returnMoney)
	{
		String sn = order.getSn();
		sn = sn.substring(sn.length()-5, sn.length());
		String afterMoney = "退货到账通知：亲爱的用户您好，您申请的尾号"+sn+"的订单售后退款成功，"+returnMoney+"元退款已经原路返还至你的支付账户";
		return afterMoney;
	}

	/**
	 * 完成
	 */
	@PostMapping("/complete")
	public ResponseEntity<?> complete(@ModelAttribute(binding = false) Aftersales aftersales) {
		if (aftersales == null) {
			return Results.UNPROCESSABLE_ENTITY;
		}
		if (!Aftersales.Status.APPROVED.equals(aftersales.getStatus())) {
			return Results.UNPROCESSABLE_ENTITY;
		}

		aftersalesService.complete(aftersales);
		return Results.OK;
	}

}