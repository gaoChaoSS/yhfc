/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: Vf3wkxEgLgO24r/jpD+wxoxtIRqJYQfP
 */
package net.shopxx.service;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.entity.SocialUser;
import net.shopxx.entity.User;

/**
 * Service - 社会化用户
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface SocialUserService extends BaseService<SocialUser, Long> {

	/**
	 * 查找社会化用户
	 * 
	 * @param loginPluginId
	 *            登录插件ID
	 * @param uniqueId
	 *            唯一ID
	 * @return 社会化用户，若不存在则返回null
	 */
	SocialUser findByOpenId( String uniqueId);
	
	
	/**
	 * 查找社会化用户
	 * 
	 * @param loginPluginId
	 *            登录插件ID
	 * @return 社会化用户，若不存在则返回null
	 */
	SocialUser find(String loginPluginId);
	
	
	/**
	 * 查找社会化用户-用户唯一id
	 * @param uniqueId
	 * @return
	 */
	SocialUser findByUniqueId(String uniqueId);

	/**
	 * 查找社会化用户分页
	 * 
	 * @param user
	 *            用户
	 * @param pageable
	 *            分页信息
	 * @return 社会化用户分页
	 */
	Page<SocialUser> findPage(User user, Pageable pageable);

	/**
	 * 绑定用户
	 * 
	 * @param user
	 *            用户
	 * @param socialUser
	 *            社会化用户
	 * @param uniqueId
	 *            唯一ID
	 */
	void bindUser(User user, SocialUser socialUser, String uniqueId);

}