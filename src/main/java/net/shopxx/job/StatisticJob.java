/*
 * Copyright 2008-2018 shopxx.net. All rights reserved.
 * Support: http://www.shopxx.net
 * License: http://www.shopxx.net/license
 * FileId: KHriKg7cc2Q8d5+C2u1P/72uDglTAkqE
 */
package net.shopxx.job;

import java.util.Calendar;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import net.shopxx.service.StatisticService;

/**
 * Job - 统计
 * 
 * @author SHOP++ Team
 * @version 6.1
 */
@Lazy(false)
@Component
public class StatisticJob {

	@Inject
	private StatisticService statisticService;
	
	@Value("${order_limit_time}")
	protected String order_limit_time;

	/**
	 * 收集
	 */
	@Scheduled(cron = "${job.statistic_collect.cron}")
	public void collect() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);

		statisticService.collect(year, month, day,order_limit_time);
	}
}