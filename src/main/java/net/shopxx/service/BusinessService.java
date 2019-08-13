/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: wCQx9y61p9M8uicW9+Vl70VvAN+Drg5p
 */
package net.shopxx.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import net.shopxx.entity.Business;
import net.shopxx.entity.BusinessDepositLog;
import net.shopxx.entity.User;
import net.shopxx.security.AuthenticationProvider;

/**
 * Service - 商家
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface BusinessService extends BaseService<Business, Long>, AuthenticationProvider {

	/**
	 * 判断用户名是否存在
	 * 
	 * @param username
	 *            用户名(忽略大小写)
	 * @return 用户名是否存在
	 */
	boolean usernameExists(String username);

	/**
	 * 根据用户名查找商家
	 * 
	 * @param username
	 *            用户名(忽略大小写)
	 * @return 商家，若不存在则返回null
	 */
	Business findByUsername(String username);

	/**
	 * 判断E-mail是否存在
	 * 
	 * @param email
	 *            E-mail(忽略大小写)
	 * @return E-mail是否存在
	 */
	boolean emailExists(String email);

	/**
	 * 判断E-mail是否唯一
	 * 
	 * @param id
	 *            ID
	 * @param email
	 *            E-mail(忽略大小写)
	 * @return E-mail是否唯一
	 */
	boolean emailUnique(Long id, String email);

	/**
	 * 根据E-mail查找商家
	 * 
	 * @param email
	 *            E-mail(忽略大小写)
	 * @return 商家，若不存在则返回null
	 */
	Business findByEmail(String email);

	/**
	 * 判断手机是否存在
	 * 
	 * @param mobile
	 *            手机(忽略大小写)
	 * @return 手机是否存在
	 */
	boolean mobileExists(String mobile);

	/**
	 * 判断手机是否唯一
	 * 
	 * @param id
	 *            ID
	 * @param mobile
	 *            手机(忽略大小写)
	 * @return 手机是否唯一
	 */
	boolean mobileUnique(Long id, String mobile);

	/**
	 * 通过名称查找商家
	 * 
	 * @param keyword
	 *            关键词
	 * @param count
	 *            数量
	 * @return 商家
	 */
	List<Business> search(String keyword, Integer count);

	/**
	 * 根据手机查找商家
	 * 
	 * @param mobile
	 *            手机(忽略大小写)
	 * @return 商家，若不存在则返回null
	 */
	Business findByMobile(String mobile);

	/**
	 * 增加余额
	 * 
	 * @param business
	 *            商家
	 * @param amount
	 *            值
	 * @param type
	 *            类型
	 * @param memo
	 *            备注
	 */
	void addBalance(Business business, BigDecimal amount, BusinessDepositLog.Type type, String memo);

	/**
	 * 增加冻结金额
	 * 
	 * @param business
	 *            商家
	 * @param amount
	 *            值
	 */
	void addFrozenAmount(Business business, BigDecimal amount);

	/**
	 * 查询总余额
	 * 
	 * @return 总余额
	 */
	BigDecimal totalBalance();

	/**
	 * 查询冻结总额
	 * 
	 * @return 冻结总额
	 */
	BigDecimal frozenTotalAmount();

	/**
	 * 查询商家数量
	 * 
	 * @param beginDate
	 *            开始日期
	 * @param endDate
	 *            结束日期
	 * @return 商家数量
	 */
	long count(Date beginDate, Date endDate);

	/**
	 *	通过账号密码查询商家信息
	 *
	 * 	@param account
	 *
	 * 	@param pwd
	 *
	 *  @Auther: Demaxiya
	 *  @Create: 2019-07-23 18:16
	 */
	User findByAccountAndPwd(String account, String pwd);

}