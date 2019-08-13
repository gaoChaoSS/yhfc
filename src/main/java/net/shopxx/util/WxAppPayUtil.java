package net.shopxx.util;

import net.shopxx.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class WxAppPayUtil {

    private final static Logger logger = LoggerFactory.getLogger(WxAppPayUtil.class);


    public Map<String, Object> payment(String orderNo, String totalFee, String desc, String ip, String openId,
                                       String notifyUrl, String WX_APPID, String WX_MCH_ID, String WX_ORDER_TIME_EXPIRE,
                                       String HOME_URL, String WX_PAY_KEY, Long memberId) throws Exception {
        // ----------------- 获取系统配置 -----------------
//        List<SystemConfig> systemConfigList = this.systemConfigRepository.findBySystemKeyIn(
//                Arrays.asList(
//                        "WX_APPID",                 // 微信分配的小程序ID
//                        "WX_MCH_ID",                // 微信支付分配的商户号
//                        "WX_ORDER_TIME_EXPIRE",     // 微信订单失效时间/分钟数
//                        "HOME_URL",                 // 主域名
//                        "WX_PAY_KEY"                // 微信商户后台设置的API密钥
//                )
//        );
        // ----------------- 发送报文模板 -----------------
        String xml = "<xml>" +
                "<appid>APPID</appid>" +                        // 公众号ID
                "<mch_id>MERCHANT</mch_id>" +                   // 微信支付分配的商户号
                "<nonce_str>NONCE_STR</nonce_str>" +            // 随机字符串
                "<body>BODY</body>" +                           // 商品描述
                "<out_trade_no>PAY_NO</out_trade_no>" +         // 商户订单号
                "<total_fee>TOTAL</total_fee>" +                // 订单总金额，单位为分
                "<spbill_create_ip>IP</spbill_create_ip>" +     // 用户终端IP
                "<time_start>START</time_start>" +              // 订单开始时间
                "<time_expire>STOP</time_expire>" +             // 订单结束时间
                "<notify_url><![CDATA[URL_TO]]></notify_url>" + // 异步通知地址
                "<trade_type>TYPE</trade_type>" +               // 交易类型，小程序取值如下：JSAPI
                "<openid>OPENID</openid>" +                     // 用户标识OpenId
                "<sign>SIGN</sign>" +                           // 签名
                "</xml>";
        // ----------------- 报文模板赋值 -----------------
        // 生成订单起始时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar nowTime = Calendar.getInstance();
        String start_time = sdf.format(nowTime.getTime());
        nowTime.add(Calendar.MINUTE, Integer.parseInt(WX_ORDER_TIME_EXPIRE));
        String stop_time = sdf.format(nowTime.getTime());

        // ----------------- xml数据封装
        // 公众号ID
        xml = xml.replace("APPID", WX_APPID);
        // 微信支付分配的商户号
        xml = xml.replace("MERCHANT", WX_MCH_ID);
        // 随机字符串
        String nonce_str = WxPayUtil.generateNonceStr();
        xml = xml.replace("NONCE_STR", nonce_str);
        // 商品描述
        xml = xml.replace("BODY", desc);
        // 商户订单号
        xml = xml.replace("PAY_NO", orderNo);
        // 订单总金额，单位为分
        xml = xml.replace("TOTAL", totalFee);
        // 用户终端IP
        xml = xml.replace("IP", ip);
        // 订单开始时间
        xml = xml.replace("START", start_time);
        // 订单结束时间
        xml = xml.replace("STOP", stop_time);
        // 异步通知地址
        xml = xml.replace("URL_TO", HOME_URL + notifyUrl);
        // 交易类型，小程序取值如下：JSAPI
        xml = xml.replace("TYPE", "JSAPI");
        // 用户标识OpenId
        xml = xml.replace("OPENID", openId);
        // ----------------- 生成签名
        Map<String, String> signMap = new HashMap<>();
        signMap.put("appid", WX_APPID);
        signMap.put("mch_id", WX_MCH_ID);
        signMap.put("nonce_str", nonce_str);
        signMap.put("body", desc);
        signMap.put("out_trade_no", orderNo);
        signMap.put("total_fee", totalFee);
        signMap.put("spbill_create_ip", ip);
        signMap.put("time_start", start_time);
        signMap.put("time_expire", stop_time);
        signMap.put("notify_url", HOME_URL + notifyUrl);
        signMap.put("trade_type", "JSAPI");
        signMap.put("openid", openId);
        String sign = WxPayUtil.generateSignature(signMap, WX_PAY_KEY);
        xml = xml.replace("SIGN", sign);

        boolean b = WxPayUtil.isSignatureValid(xml, WX_PAY_KEY);

        logger.info("支付下单请求参数：xml,{},orderNo,{},openId,{}", xml, orderNo, openId);
        // ---- 发送下单请求
        String httpResult = HttpUtils.httpsRequest("https://" + WXPayConstants.WX_PAY_API + WXPayConstants.UNIFIEDORDER_URL_SUFFIX,
                "POST", xml);
        logger.info("订单支付结果：{}", httpResult);

        //System.out.println("支付结果："+httpResult);

        // 将返回数据XML转换成Map
        Map<String, String> httpResultMap = WxPayUtil.xmlToMap(httpResult);

        // ---- 组装返回数据
        Map<String, Object> resultMap = new HashMap<>();
        // -------------------------- 支付返回值 -----------------------------
        // ----------- 最外层
        // 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断 [SUCCESS/FAIL]
        String return_code = httpResultMap.get("return_code");
        if (return_code.equals("FAIL")) {
            // 若 return_code = FAIL，将为失败，直接返回
            resultMap.put("return_code", return_code);
            resultMap.put("return_msg", httpResultMap.get("return_msg"));
            resultMap.put("param", xml);

            return resultMap;
        }

        // ----------- return_code 为 SUCCESS 的时候返回
        // 业务结果 [SUCCESS/FAIL]
        String result_code = httpResultMap.get("result_code");
        if (result_code.equals("FAIL")) {
            // 下单失败 返回
            resultMap.put("return_code", result_code);
            resultMap.put("return_msg", WXPayErrorCode.getErrorInfo(WXPayErrorCode.WxPayApiType.UNIFIEDORDER, httpResultMap.get("err_code")));
            resultMap.put("param", xml);
            return resultMap;
        }

        // ----------- return_code 和result_code都为SUCCESS的时候返回
        /*
         * 预支付交易会话标识
         * 微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时
         */
        String prepay_id = httpResultMap.get("prepay_id");
        resultMap.put("return_code", result_code);
        resultMap.put("return_msg", httpResultMap.get("httpResultMap"));
        resultMap.put("param", xml);
        // ----------------- 生成签名
        Map<String, String> wxSignMap = new HashMap<>();
        wxSignMap.put("appId", WX_APPID);
        Long timestamp = WxPayUtil.getCurrentTimestamp();
        wxSignMap.put("timeStamp", timestamp + "");
        String wx_nonce_str = WxPayUtil.generateNonceStr();
        wxSignMap.put("nonceStr", wx_nonce_str);
        wxSignMap.put("package", "prepay_id=" + prepay_id);
        wxSignMap.put("signType", WXPayConstants.MD5);
        String wxSign = WxPayUtil.generateSignature(wxSignMap, WX_PAY_KEY);
        // ----------------- 返回数据组装
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("timeStamp", timestamp.toString());
        dataMap.put("nonceStr", wx_nonce_str);
        dataMap.put("package", "prepay_id=" + prepay_id);
        dataMap.put("signType", WXPayConstants.MD5);
        dataMap.put("paySign", wxSign);
        resultMap.put("data", dataMap);
        return resultMap;
    }

    public static Map<String, Object> orderquery(String orderNo, String WX_APPID, String WX_MCH_ID, String WX_PAY_KEY) throws Exception {
        // ----------------- 获取系统配置 -----------------
//        List<SystemConfig> systemConfigList = this.systemConfigRepository.findBySystemKeyIn(
//                Arrays.asList(
//                        "WX_APPID",                 // 微信分配的小程序ID
//                        "WX_MCH_ID",                // 微信支付分配的商户号
//                        "WX_PAY_KEY"                // 微信商户后台设置的API密钥
//                )
//        );
        // ----------------- 发送报文模板 -----------------
        String xml = "<xml>" +
                "<appid>APPID</appid>" +                        // 公众号ID
                "<mch_id>MERCHANT</mch_id>" +                   // 微信支付分配的商户号
                "<out_trade_no>PAY_NO</out_trade_no>" +         // 商户订单号
                "<nonce_str>NONCE_STR</nonce_str>" +            // 随机字符串
                "<sign>SIGN</sign>" +                           // 签名
                "</xml>";
        // ----------------- 报文模板赋值 -----------------
        // 公众号ID
        xml = xml.replace("APPID", WX_APPID);
        // 微信支付分配的商户号
        xml = xml.replace("MERCHANT", WX_MCH_ID);
        // 商户订单号
        xml = xml.replace("PAY_NO", orderNo);
        // 随机字符串
        String nonce_str = WxPayUtil.generateNonceStr();
        xml = xml.replace("NONCE_STR", nonce_str);

        // ----------------- 生成签名
        Map<String, String> signMap = new HashMap<>();
        signMap.put("appid", WX_APPID);
        signMap.put("mch_id", WX_MCH_ID);
        signMap.put("out_trade_no", orderNo);
        signMap.put("nonce_str", nonce_str);
        String sign = WxPayUtil.generateSignature(signMap, WX_PAY_KEY);
        xml = xml.replace("SIGN", sign);

        // ---- 发送查询请求
        String httpResult = HttpUtils.httpsRequest("https://" + WXPayConstants.WX_PAY_API + WXPayConstants.ORDERQUERY_URL_SUFFIX,
                "POST", xml);


        // 将返回数据XML转换成Map
        Map<String, String> httpResultMap = WxPayUtil.xmlToMap(httpResult);

        // ---- 组装返回数据
        Map<String, Object> resultMap = new HashMap<>();
        // -------------------------- 支付返回值 -----------------------------
        // ----------- 最外层
        // 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断 [SUCCESS/FAIL]
        String return_code = httpResultMap.get("return_code");
        // 返回信息，如非空，为错误原因
        String return_msg = httpResultMap.get("return_msg");
        if (return_code.equals("FAIL")) {
            // 若 return_code = FAIL，将为失败，直接返回
            resultMap.put("return_code", return_code);
            resultMap.put("return_msg", return_msg);
            return resultMap;
        }

        // ----------- return_code 为 SUCCESS 的时候返回
        // 业务结果 [SUCCESS/FAIL]
        String result_code = httpResultMap.get("result_code");
        if (result_code.equals("FAIL")) {
            // 下单失败 返回
            resultMap.put("return_code", result_code);
            resultMap.put("return_msg", WXPayErrorCode.getErrorInfo(WXPayErrorCode.WxPayApiType.ORDERQUERY, httpResultMap.get("err_code")));
            return resultMap;
        }

        // ----------- return_code 和result_code都为SUCCESS的时候返回
        resultMap.put("return_code", result_code);
        resultMap.put("return_msg", "查询成功!");
        resultMap.put("orderInfo", httpResultMap);
        return resultMap;
    }

}
