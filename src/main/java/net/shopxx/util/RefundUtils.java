package net.shopxx.util;


import com.github.wxpay.sdk.WXPayUtil;
import net.sf.json.JSONObject;
import net.shopxx.dao.OrderDao;
import net.shopxx.entity.Order;
import net.shopxx.entity.PluginConfig;
import net.shopxx.plugin.WeixinPublicPaymentPlugin;
import net.shopxx.service.OrderRefundsService;
import net.shopxx.service.OrderService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 退款工具类
 */
@Repository
public class RefundUtils {

    private final static Logger logger = LoggerFactory.getLogger(RefundUtils.class);


    @Autowired
    private OrderRefundsService orderRefundsService;

    @Autowired
    private OrderService orderService;

    @Inject
    private WeixinPublicPaymentPlugin weixinPublicPaymentPlugin;

    @Autowired
    private OrderDao orderDao;


    /**
     * @param request
     * @param orderId   订单id
     * @param orderNum  订单号
     * @param refundNum  退款流水号
     * @param orderMoney 订单金额
     * @param refundMoney  退款金额
     * @param refundCase   退款原因
     * @return
     */
    public JSONObject refundByOrderId(HttpServletRequest request, String orderId, String orderNum, String refundNum, BigDecimal orderMoney, BigDecimal refundMoney, String refundCase) {
        JSONObject jsonObject = new JSONObject();
        try {

            //创建退款数据报包
            SortedMap<Object, Object> packageParams = new TreeMap<Object, Object>();

            //获取商户号的一些参数信息
            PluginConfig pluginConfig = weixinPublicPaymentPlugin.getPluginConfig();

            //获取订单
            Order order=orderService.find(Long.parseLong(orderId));
            //封装所有参数
            //appid==公众账号ID==同下单
            packageParams.put("appid", pluginConfig.getAttribute("appId"));

            //mch_id==商户号==同下单
            packageParams.put("mch_id", pluginConfig.getAttribute("mchId"));


            //nonce_str==32位随机字符串==生成方法同下单
            packageParams.put("nonce_str", WXPayUtil.generateNonceStr()); // 随机字符串（32位以内） 这里使用时间戳

            //sign_type==签名类型==同下单
            packageParams.put("sign_type", "MD5");//paySign加密

            //out_trade_no== 商户订单号 ==  用户下单单号
            //退款订单号
            packageParams.put("out_trade_no", orderNum);

            //out_refund_no==商户退款单号== 退款流水号 (时间戳后8位+5位随机数)
            packageParams.put("out_refund_no", refundNum);

            //total_fee==订单金额==该退款订单的总价格  （官方规定 单位：分,且为整数）
            packageParams.put("total_fee", orderMoney.multiply(new BigDecimal("100")).intValue());

            //refund_fee==退款金额==需要退款的金额    （官方规定 单位：分,且为整数）
            packageParams.put("refund_fee", refundMoney.multiply(new BigDecimal("100")).intValue());

            //退款原因
            packageParams.put("refund_desc", refundCase);

            //获取sign
            String sign = PayCommonUtil.createSign("UTF-8", packageParams, pluginConfig.getAttribute("apiKey"));//最后这个是自己在微信商户设置的32位密钥

            //sign==签名==签名方法同同下单
            packageParams.put("sign", sign);

            //退款回调接口  ( 非必要 )
            //packageParams.put("notify_url",  notifyUrl+"/apiRefund/backRefund" );

            //所有参数赋值并签名后转化为xml封装好请求
            String requestXML = PayCommonUtil.getRequestXml(packageParams);
            //System.err.println("requestXML:" + requestXML);
            logger.info("退款人ID:"+order.getMember().getId());
            logger.info("退款请求参数："+requestXML);
            StringEntity stringEntity = new StringEntity(requestXML, "UTF-8");

            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/secapi/pay/refund");
            // 得指明使用UTF-8编码，否则到API服务器XML的中文不能被成功识别
            httpPost.addHeader("Content-Type", "text/xml");
            httpPost.setEntity(stringEntity);

            // 初始化证书,得到一个 HttpClient （加载含有证书的http请求）
            HttpResponse response = initCert(request).execute(httpPost);
            HttpEntity entity = response.getEntity();

            //得到返回结果的XML
            String resXml = EntityUtils.toString(entity, "UTF-8");
            logger.info("退款微信返回参数："+resXml);

            //将XML转换成 Map 集合
            Map map = XMLUtil.doXMLParse(resXml);
            //回调结果
            String returnCode = (String) map.get("return_code");
            String returnMsg = (String) map.get("return_msg");//返回信息

            String resultCode = (String) map.get("result_code");//返回结果码
            String errCodeDes = (String) map.get("err_code_des");//返回结果码
            String transaction_id = (String) map.get("transaction_id");//微信订单号

            // 判断是订单退款还是售后退款
            int type = 1;
            if (orderMoney.compareTo(refundMoney) > 0) {
                type=2;
            }
            //返回结果成功
            if ("SUCCESS".equals(returnCode) && "OK".equals(returnMsg)) {
                if ("FAIL".equals(resultCode)) {
                    jsonObject.put("status", HttpStatus.BAD_REQUEST);
                    jsonObject.put("Msg", "errCodeDes:" + errCodeDes);
                    orderService.savePayLog(type,requestXML,refundMoney,0,orderNum,order.getMember().getId(),transaction_id);
                    return jsonObject;
                } else {
                    jsonObject.put("status", "OK");
                    orderService.savePayLog(type,requestXML,refundMoney,1,orderNum,order.getMember().getId(),transaction_id);
                    //生成一条退款记录
                    //   订单Id 订单号  退款金额  退款备注  退款方式(3 线上退款)
                    orderRefundsService.insertRefundLog(orderId, orderNum, refundMoney.doubleValue(), refundNum, refundCase, 0);

                    // 更改该订单已退款的金额  更改订单状态为退款状态  Order.Status.REFUND
                    orderService.updateRefundAmount(orderId, refundMoney.doubleValue(), 11);

                    jsonObject.put("Msg", "退款成功");
                    return jsonObject;
                }

            } else {
                //返回结果失败
                jsonObject.put("status", HttpStatus.BAD_REQUEST);
                jsonObject.put("Msg", "退款失败");
                orderService.savePayLog(type,requestXML,refundMoney,0,orderNum,order.getMember().getId(),transaction_id);
                return jsonObject;
            }

        } catch (Exception e) {
            //返回结果失败
            jsonObject.put("status", HttpStatus.BAD_REQUEST);
            jsonObject.put("Msg", e.getMessage());
            return jsonObject;
        }
    }


    //商户Id
    @Value("${payConfig.mchId}")
    private String key;

    //商户证书路径
    @Value("${payConfig.payConfigPath}")
    private String payConfigPath;

    /**
     * 加载证书
     */
    private CloseableHttpClient initCert(HttpServletRequest request) throws Exception {
        //  System.err.println("key:"+key);
        //  System.err.println("payConfigPath:"+payConfigPath);

        // 商户证书的路径
        String path = request.getServletContext().getRealPath(payConfigPath);// 证书文件路径
        // System.err.println("path:"+path);

        // 指定读取证书格式为PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        // 读取本机存放的PKCS12证书文件
        FileInputStream instream = new FileInputStream(new File(path));
        try {
            // 指定PKCS12的密码(商户ID)
            keyStore.load(instream, key.toCharArray());
        } finally {
            instream.close();
        }

        SSLContext sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, key.toCharArray()).build();

        // 指定TLS版本
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
        // 设置httpclient的SSLSocketFactory
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }


}
