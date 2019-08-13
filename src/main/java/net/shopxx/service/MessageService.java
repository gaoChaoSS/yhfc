/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: bkRSMSK+K4vHnVJlRWdLFxCZi13n57gE
 */
package net.shopxx.service;

import java.util.List;

import net.shopxx.entity.Message;
import net.shopxx.entity.MessageGroup;
import net.shopxx.entity.User;

/**
 * Service - 消息
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
public interface MessageService extends BaseService<Message, Long> {

	/**
	 * 查找
	 * 
	 * @param messageGroup
	 *            消息组
	 * @param user
	 *            用户
	 * @return 消息
	 */
	List<Message> findList(MessageGroup messageGroup, User user);

	/**
	 * 未读消息数量
	 * 
	 * @param messageGroup
	 *            消息组
	 * @param user
	 *            用户
	 * @return 未读消息数量
	 */
	Long unreadMessageCount(MessageGroup messageGroup, User user);

	/**
	 * 查阅
	 * 
	 * @param messageGroup
	 *            消息组
	 * @param currentUser
	 *            当前用户
	 */
	void consult(MessageGroup messageGroup, User currentUser);

	/**
	 * 发送
	 * 
	 * @param type
	 *            用户类型
	 * @param fromUser
	 *            发送人
	 * @param toUser
	 *            接收人
	 * @param content
	 *            内容
	 * @param ip
	 *            ip
	 */
	void send(User.Type type, User fromUser, User toUser, String content, String ip);

	/**
	 * 回复
	 * 
	 * @param messageGroup
	 *            消息组
	 * @param fromUser
	 *            发送人
	 * @param content
	 *            内容
	 * @param ip
	 *            ip
	 */
	void reply(MessageGroup messageGroup, User fromUser, String content, String ip);

}