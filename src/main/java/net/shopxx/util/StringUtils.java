package net.shopxx.util;

import java.util.Random;

/**
 * @Description:
 * @Date: 2019/1/10
 * @Author: yl
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils{

    /**
     * StringUtils工具类方法
     * 获取一定长度的随机字符串，范围0-9，a-z
     * @param length：指定字符串长度
     * @return 一定长度的随机字符串
     */
    public static String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        String base1 = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base1.length());
            sb.append(base1.charAt(number));
        }
        return sb.toString();
    }


    /**
     * 根据当前时间获取字符串
     * @param length
     * @return
     */
    public static String getLongTypeString(int length){
        Long i=System.currentTimeMillis();
        String s = i.toString();
        int slength = s.length();
        if(slength>=length){
            s=s.substring(slength-length,slength);
        }else {
            s=s.substring(0,slength);
        }
        return  s;
    }


    /**
     * 生成id  5随机数+8时间戳   13位
     */
    public static String getId(){
        Long l = System.currentTimeMillis();
        String s = l.toString();
        //System.err.println("s"+s);
        s = s.substring(5, s.length());
        int i = new Random().nextInt(99999);
        if (i < 10000) {
            i += 10000;
        }
       // System.out.println("i"+i);
        return i+s ;
    }


}
