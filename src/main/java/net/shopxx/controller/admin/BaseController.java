/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: a9iY+NSWQm+xSi61iz90x/KwOxs9FHPv
 */
package net.shopxx.controller.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import net.shopxx.entity.Member;
import net.shopxx.service.MemberService;
import net.shopxx.util.GsonUtil;
import net.shopxx.util.JsonUtils;
import net.shopxx.util.MySessionContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Controller - 基类
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public class BaseController {

	/**
	 * "验证结果"属性名称
	 */
	private static final String CONSTRAINT_VIOLATIONS_ATTRIBUTE_NAME = "constraintViolations";
	
	/**
	 * 请求无法处理视图
	 */
	protected static final String UNPROCESSABLE_ENTITY_VIEW = "common/error/unprocessable_entity";
	
	@Value("${order_limit_time}")
	protected String order_limit_time;

	@Inject
	private Validator validator;
	
	@Inject
	private MemberService memberService;

	/**
	 * 数据验证
	 * 
	 * @param target
	 *            验证对象
	 * @param groups
	 *            验证组
	 * @return 验证结果
	 */
	protected boolean isValid(Object target, Class<?>... groups) {
		Assert.notNull(target, "[Assertion failed] - target is required; it must not be null");

		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(target, groups);
		if (constraintViolations.isEmpty()) {
			return true;
		}
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		requestAttributes.setAttribute(CONSTRAINT_VIOLATIONS_ATTRIBUTE_NAME, constraintViolations, RequestAttributes.SCOPE_REQUEST);
		return false;
	}

	/**
	 * 数据验证
	 * 
	 * @param targets
	 *            验证对象
	 * @param groups
	 *            验证组
	 * @return 验证结果
	 */
	protected boolean isValid(Collection<Object> targets, Class<?>... groups) {
		Assert.notEmpty(targets, "[Assertion failed] - targets must not be empty: it must contain at least 1 element");

		for (Object target : targets) {
			if (!isValid(target, groups)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 数据验证
	 * 
	 * @param type
	 *            类型
	 * @param property
	 *            属性
	 * @param value
	 *            值
	 * @param groups
	 *            验证组
	 * @return 验证结果
	 */
	protected boolean isValid(Class<?> type, String property, Object value, Class<?>... groups) {
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");
		Assert.hasText(property, "[Assertion failed] - property must have text; it must not be null, empty, or blank");

		Set<?> constraintViolations = validator.validateValue(type, property, value, groups);
		if (constraintViolations.isEmpty()) {
			return true;
		}
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		requestAttributes.setAttribute(CONSTRAINT_VIOLATIONS_ATTRIBUTE_NAME, constraintViolations, RequestAttributes.SCOPE_REQUEST);
		return false;
	}

	/**
	 * 数据验证
	 * 
	 * @param type
	 *            类型
	 * @param properties
	 *            属性
	 * @param groups
	 *            验证组
	 * @return 验证结果
	 */
	protected boolean isValid(Class<?> type, Map<String, Object> properties, Class<?>... groups) {
		Assert.notNull(type, "[Assertion failed] - type is required; it must not be null");
		Assert.notEmpty(properties, "[Assertion failed] - properties must not be empty: it must contain at least 1 element");

		for (Map.Entry<String, Object> entry : properties.entrySet()) {
			if (!isValid(type, entry.getKey(), entry.getValue(), groups)) {
				return false;
			}
		}
		return true;
	}

	/**
	 *	返回数据组装
	 *	有数据
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-08 18:27
	 */
	protected Map<String,Object> data(HttpStatus status, String message, Object data){
		Map<String,Object> dataSet=new HashMap<>();
		dataSet.put("status",status);
		dataSet.put("message",message);
		dataSet.put("data",data);
		return dataSet;
	}

	/**
	 *	返回数据组装
	 *	无数据
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-08 18:27
	 */
	protected Map<String,Object> data(HttpStatus status,String message){
		Map<String,Object> dataSet=new HashMap<>();
		dataSet.put("status",status);
		dataSet.put("message",message);
		return dataSet;
	}

	/**
	 * 获取Body参数
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getBodyParameter(HttpServletRequest request) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
		String str = "";
		String wholeStr = "";
		while((str = reader.readLine()) != null){//一行一行的读取body体里面的内容；
			wholeStr += str;
		}
		Map<String, Object> parm = GsonUtil.jsonToMap(wholeStr);
		return parm;
	}
	
	
	/**
	 * 保存session
	 * @return sessionId
	 */
	protected String setWxLoginUser(HttpServletRequest request,Member member)
	{
		MySessionContext myc = MySessionContext.getInstance();  
		HttpSession session = request.getSession();
		session.setAttribute("wx_open_id", member);
		session.setMaxInactiveInterval(60*60*24);
		myc.addSession(session);
		return session.getId();
	}
	
	/**
	 * 获取用户对象
	 * @param sessionId
	 * @return
	 */
	protected Member getWxLoginUser(String  sessionId)
	{
		MySessionContext myc= MySessionContext.getInstance();  
	    HttpSession session = myc.getSession(sessionId);  
		Member member = (Member)session.getAttribute("wx_open_id");
		return member;
	}

	/**
	 * 获取当前登陆的用户信息
	 *
	 * @param request
	 * @return
	 */
	protected Member getCurrentWxInfo(HttpServletRequest request) {
		if (request.getSession().getAttribute("member") == null) {
			return null;
		}
		Member member = (Member) request.getSession().getAttribute("member");
		member = memberService.find(member.getId());
		return member;
	}

	/**
	 * 添加Session
	 *
	 * @param request
	 * @return
	 */
	protected void addSession(HttpServletRequest request, String sessionId, Object object) {
		// 60分钟 * 24小时 * 7天 后Session过期
		request.getSession().setMaxInactiveInterval(60 * 60 * 24 * 7);
		request.getSession().setAttribute(sessionId, object);
	}

	/**
	 * 获取Session
	 *
	 * @param request
	 * @param sessionId
	 * @return
	 */
	protected Object getSession(HttpServletRequest request, String sessionId) {
		return request.getSession().getAttribute(sessionId);
	}

	/**
	 *	请求输出
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-12 11:22
	 */
	public void result(HttpServletResponse response, ResponseEntity<?> resultResponse) throws Exception{
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.print(JsonUtils.toJson(resultResponse));
		out.flush();
	}

	public static List<Map<String,Object>> stringToMap(String regix){
		List<Map<String,Object>> data=new ArrayList<>();
		if(regix.indexOf("},{")>-1){
			for(int i=0;i<regix.split("},").length;i++){
				String aa="";
				if(regix.split("},").length>2) {
					if(i==regix.split("},").length-1){
						aa=regix.split("},")[i] ;
					}else{
						aa = regix.split("},")[i] + "}";
					}
				}else{
					if (i == 0 ) {
						aa = regix.split("},")[i] + "}";
					} else {
						aa = regix.split("},")[i];
					}
				}
				Map<String,Object> item=GsonUtil.jsonToMap(aa);
				data.add(item);
			}
		}else{
			Map<String,Object> item=GsonUtil.jsonToMap(regix);
			data.add(item);
		}
		return data;
	}

}