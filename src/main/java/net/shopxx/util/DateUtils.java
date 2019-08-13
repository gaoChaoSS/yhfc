package net.shopxx.util;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * 时间工具类
 * 
 * @author yangli
 *
 */
public class DateUtils extends org.apache.commons.lang.time.DateUtils {

	public static String FORMAT_HMS = "yyyy-MM-dd HH:mm:ss";

	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

	public static SimpleDateFormat format_hms = new SimpleDateFormat(FORMAT_HMS);


	public static String formatDateTime(Date date, String partern) {

		return DateFormatUtils.format(date, StringUtils.isBlank(partern) ? FORMAT_HMS : partern);

	}

	/**
	 * 获取订单限制时间
	 * 
	 * @param day       1 or -1
	 * @param limitTime 23:00:00
	 * @return
	 */
	public static Date getOrderLimitDate(int day, String limitTime) {

		Calendar c = null;
		try {
			c = Calendar.getInstance();
			c.setTime(new Date()); // 设置时间
			c.add(Calendar.DATE, day);// 日期分钟加1,Calendar.DATE(天),Calendar.HOUR(小时)
			
			if (StringUtils.isEmpty(limitTime)) {
				  c.set(Calendar.HOUR_OF_DAY, 22);
			      c.set(Calendar.MINUTE, 0);
			      c.set(Calendar.SECOND, 0);
			} else {
				c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(limitTime.split(":")[0]));
				c.set(Calendar.MINUTE, Integer.parseInt(limitTime.split(":")[1]));
				c.set(Calendar.SECOND, Integer.parseInt(limitTime.split(":")[2]));
			}
		} catch (Exception e) {
			c.set(Calendar.HOUR_OF_DAY, 22);
			c.set(Calendar.MINUTE, 00);
			c.set(Calendar.SECOND, 00);
		}
		Date date = c.getTime();
		return date;
	}
	
	/**
	 * 获取当月的第一天
	 * @return
	 */
	public static String getMonthFirstDay()
	{
		Calendar cale = Calendar.getInstance();  
        cale.add(Calendar.MONTH, 0);  
        cale.set(Calendar.DAY_OF_MONTH, 1);  
        Date date = cale.getTime();
        return  format.format(date.getTime());
	}
	
	/**
	 * 获取当月的最后一天
	 * @return
	 */
	public static String getMonthLastDay()
	{
		Calendar cale = Calendar.getInstance();  
		cale.add(Calendar.MONTH, 1);  
	    cale.set(Calendar.DAY_OF_MONTH, 0); 
	    Date date = cale.getTime();
	    return  format.format(date.getTime());
	}
	
	/**
	 * 获取当天yyyy-MM-dd
	 * @return
	 */
	public static String getToday()
	{
		return  format.format(new Date());
	}

	/**
	 * 获取今日零时零分
	 *
	 * @return
	 */
	public static Long getNowToday(){
		Long  time = System.currentTimeMillis();  //当前时间的时间戳
		//今天零点零分零秒
		long nowToday = time/(1000*3600*24)*(1000*3600*24) - TimeZone.getDefault().getRawOffset();
		return  nowToday ;
	}
	
	public static String getDateFormat(int i,Date date)
	{
		
		switch (i) {
		case 1:
			return format.format(date);
		case 2:
			return format_hms.format(date);
		default:
			break;
		}
		return "";
	}

	/**
	 * 判断当天是否为本月第一天
	 *
	 * @return
	 */
	public static boolean isFirstDayOfMonth() {
		boolean flag = false;
		Calendar calendar = Calendar.getInstance();
		int today = calendar.get(Calendar.DAY_OF_MONTH);
		if (1 == today) {
			flag = true;
		}
		return flag;
	}


	/**
	 * 获取任意时间的下一个月
	 * 描述:<描述函数实现的功能>.
	 * @param repeatDate
	 * @return
	 */
	public static String getPreMonth(String repeatDate) {
		String lastMonth = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dft = new SimpleDateFormat("yyyyMM");
		int year = Integer.parseInt(repeatDate.substring(0, 4));
		String monthsString = repeatDate.substring(4, 6);
		int month;
		if ("0".equals(monthsString.substring(0, 1))) {
			month = Integer.parseInt(monthsString.substring(1, 2));
		} else {
			month = Integer.parseInt(monthsString.substring(0, 2));
		}
		cal.set(year,month,Calendar.DATE);
		lastMonth = dft.format(cal.getTime());
		return lastMonth;
	}

	/**
	 * 获取任意时间的上一个月
	 * 描述:<描述函数实现的功能>.
	 * @param repeatDate
	 * @return
	 */
	public static String getLastMonth(String repeatDate) {
		String lastMonth = "";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat dft = new SimpleDateFormat("yyyyMM");
		int year = Integer.parseInt(repeatDate.substring(0, 4));
		String monthsString = repeatDate.substring(4, 6);
		int month;
		if ("0".equals(monthsString.substring(0, 1))) {
			month = Integer.parseInt(monthsString.substring(1, 2));
		} else {
			month = Integer.parseInt(monthsString.substring(0, 2));
		}
		cal.set(year,month-2,Calendar.DATE);
		lastMonth = dft.format(cal.getTime());
		return lastMonth;
	}


	/**
	 * Fri Feb 02 18:18:49 CST 2018 转换为 2018-01-30 22:22:24
	 * @param dateStr
	 * @return
	 */
	public  static String myFormatDate(String dateStr){
		if(dateStr!=null && dateStr != ""){
			String[] strings = dateStr.split(" ");
			if (strings[1].equals("Jan")){
				strings[1] = "01" ;
			}
			if (strings[1].equals("Feb")){
				strings[1] = "02" ;
			}
			if (strings[1].equals("Mar")){
				strings[1] = "03" ;
			}
			if (strings[1].equals("Apr")){
				strings[1] = "04" ;
			}
			if (strings[1].equals("May")){
				strings[1] = "05" ;
			}
			if (strings[1].equals("Jun")){
				strings[1] = "06" ;
			}
			if (strings[1].equals("Jul")){
				strings[1] = "07" ;
			}
			if (strings[1].equals("Aug")){
				strings[1] = "08" ;
			}
			if (strings[1].equals("Sep")){
				strings[1] = "09" ;
			}
			if (strings[1].equals("Oct")){
				strings[1] = "10" ;
			}
			if (strings[1].equals("Nov")){
				strings[1] = "11" ;
			}
			if (strings[1].equals("Dec")){
				strings[1] = "12" ;
			}
			return strings[5]+"-"+strings[1]+"-"+strings[2]+" "+strings[3] ;
		}else {
			return dateStr ;
		}
	}


	/**
	 * 当前年月日  2019-08-10 转 20190810
	 */
	public static String formatNowTime(){
		Calendar calendar = Calendar.getInstance();
		long createTime = calendar.getTimeInMillis();

		//System.out.println(createTime);   //1559613367549
		//SimpleDateFormat form1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat form1 = new SimpleDateFormat("yyyy-MM-dd");
		String nowTime = form1.format(createTime);
		String[] split = nowTime.split("-");
		return split[0]+split[1]+split[2] ;
	}


	/**
	 * 获取当前时间的 时分秒  HH:mm:ss
	 */
	public static String getHMS(){
		Calendar calendar = Calendar.getInstance();
		long createTime = calendar.getTimeInMillis();
		SimpleDateFormat form1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String nowTime = form1.format(createTime);
		String[] split = nowTime.split(" ");
		return split[1] ;

	}

	/**
	 * 获取时分秒 HHmmss
	 */
	public static String gethms(String hms){
		String[] split = hms.split(":");
		return split[0]+split[1]+split[2];
	}




	/**
	 * 当前时间的7天后的时间
	 * @return nowTime  当前时间       to7Days  当前时间7天后的时间
	 */
	public static Map<String, String> to7Days(){
		HashMap<String, String> map = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		long createTime = calendar.getTimeInMillis();

		//System.out.println(createTime);   //1559613367549
		//SimpleDateFormat form1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat form1 = new SimpleDateFormat("yyyy-MM-dd");
		String nowTime = form1.format(createTime);
		//System.out.println(nowTime);2019-05-28
		map.put("nowTime",nowTime);

		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 7);
		//System.out.println(calendar.getTimeInMillis());   //1541840067116

		Date today = calendar.getTime();
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String to7Days = format.format(today);
		//System.out.println(to7Days);2019-06-04
		map.put("to7Days",to7Days);
		return map ;
	}

	/**
	 * 在目标时间后加7天
	 * @param dateStr  2019-05-28 09:17:59
	 * @return
	 */
	public static String add7Days(String dateStr)throws Exception{
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = simpleDateFormat.parse(dateStr);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 7);
		System.out.println(calendar.getTimeInMillis());   //1541840067116

		Date today = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String result = format.format(today);
		System.out.println(result);
		return result ;
	}

	/**
	 * 比较时间
	 * @param date
	 * @return
	 */
	public static boolean compareTime(Date date) {

		if (date != null) {

			Date nowdate = new Date();

			if (nowdate.before(date)) {
				return true;
			}

		}

		return false;
	}





	/***
	 * 当前日期 之前或之后的某月
	 * @param date
	 * @param num
	 * @return
	 */
	public static Date getSpecDate(Date date, int num) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.DAY_OF_MONTH, num);
		return calendar.getTime();
	}

	/***
	 * 设置时间
	 *
	 * @param date
	 * @param type
	 *            0-一天起始的时间 1-一天结束时间
	 * @return
	 */
	public static Date setOneDateTime(Date date, int type) {
		Calendar lCal = Calendar.getInstance();
		lCal.setTime(date);
		if (type == 0) {
			lCal.set(Calendar.HOUR_OF_DAY, 0);
			lCal.set(Calendar.MINUTE, 0);
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
		} else if (type == 1) {
			lCal.set(Calendar.HOUR_OF_DAY, 23);
			lCal.set(Calendar.MINUTE, 59);
			lCal.set(Calendar.SECOND, 59);
			lCal.set(Calendar.MILLISECOND, 0);
		}
		return lCal.getTime();
	}


	public static String setOneDateStrTime(Date date, int type) {
		Calendar lCal = Calendar.getInstance();
		lCal.setTime(date);
		if (type == 0) {
			lCal.set(Calendar.HOUR_OF_DAY, 0);
			lCal.set(Calendar.MINUTE, 0);
			lCal.set(Calendar.SECOND, 0);
			lCal.set(Calendar.MILLISECOND, 0);
		} else if (type == 1) {
			lCal.set(Calendar.HOUR_OF_DAY, 23);
			lCal.set(Calendar.MINUTE, 59);
			lCal.set(Calendar.SECOND, 59);
			lCal.set(Calendar.MILLISECOND, 0);
		}
		return formatDateTime(lCal.getTime(),FORMAT_HMS);
	}




	public static void main(String[] args) {
		System.out.println(DateUtils.getMonthFirstDay());
		System.out.println(DateUtils.getMonthLastDay());
		System.out.println(DateUtils.getOrderLimitDate(-1, null));
		
		System.out.println(DateUtils.getDateFormat(2, new Date()));
		//随便测试修啊11111
		BigDecimal o = new BigDecimal(0);
		BigDecimal d = new BigDecimal(0.010000).multiply(new BigDecimal(1));
		System.out.println(o.add(d));
	}

	
}
