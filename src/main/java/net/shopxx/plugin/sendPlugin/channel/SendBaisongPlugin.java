package net.shopxx.plugin.sendPlugin.channel;

import com.alibaba.fastjson.JSONObject;
import net.shopxx.plugin.sendPlugin.AbstractSendSmsPlugin;
import net.shopxx.plugin.sendPlugin.SendSmsPluginFactory;
import net.shopxx.util.HttpPoolUtil;
import net.shopxx.util.MD5;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description 调用佰颂短信发送插件
 * 您好，已为您开通行业短信息账号：wvwsmpjd，
 * 网页密码：jd123，
 * 接口密码：970525，
 * 加密密钥：07163649-3c4c-4ff0-b48c-6ef8f619de28，
 * 接口地址：http://sms.haliaeetus.cn:8082/sms/sendMsg，
 * 短信平台地址：http://msg.haliaeetus.cn/#/login
 * @auther wangli
 * @create 2019-08-09 10:35
 */
@Component("sendBaisongPlugin")
public class SendBaisongPlugin extends AbstractSendSmsPlugin {

    private final static Logger logger = LoggerFactory.getLogger(SendBaisongPlugin.class);

    //平台用户名
    @Value("wvwsmpjd")
    private String userName = "wvwsmpjd";

    //平台接口密码  非网页密码
    @Value("970525")
    private String passWord = "970525";

    @Value("【聚点社区】")
    private String comSign = "【聚点社区】";

    //短信发送接口地址
    @Value("http://sms.haliaeetus.cn:8082/")
    private String postAddr = "http://sms.haliaeetus.cn:8082/";

    //单一发送地址
    private static final String SEND_MSG = "sms/sendMsg";
    //批量打包发送地址
    private static final String SEND_MSG_BATCH = "sms/sendBatchMsg";
    //内容加密密钥
    private static final String key = "07163649-3c4c-4ff0-b48c-6ef8f619de28";
    private static CloseableHttpClient client = HttpPoolUtil.getHttpClient();

    /**
     * 实例所支持的渠道类型
     *
     * @return
     */
    @Override
    public String getType() {
        return SendSmsPluginFactory.SENDTYPE_BAISONG;
    }

    /**
     * 调用真实发送者，交由各个发送渠道实现
     *
     * @param content
     * @param phoneOrPhones
     * @param parameters
     * @return
     */
    @Override
    protected String realSendMsg(String content, String phoneOrPhones, Map parameters) {
        String[] phoneArray = phoneOrPhones.split(",");
        //单一发送
        if(phoneArray.length == 1){
            try {
                Map map = new HashMap();
                map.put("phoneNumber", phoneOrPhones);
                map.put("comSign", comSign);
                map.put("content", content);
                map.put("userName", userName);
                //短信内容+加密密钥进行加密
                map.put("passWord", MD5.getMDtLower32(passWord));
                map.put("md5Content", MD5.MD5Encode(content.concat(key), "UTF-8"));
                HttpPost post = new HttpPost(postAddr + SEND_MSG);
                post.addHeader("Content-Type", "application/json;");
                post.setEntity(new StringEntity(JSONObject.toJSONString(map), "UTF-8"));
                logger.info("佰颂短信发送内容：" + JSONObject.toJSONString(map) + ",接收手机号：" + phoneOrPhones);
                CloseableHttpResponse response = client.execute(post);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity, "UTF-8");
                // 关闭
                response.close();
                Map resultMap = JSONObject.parseObject(result, Map.class);
                if (!resultMap.get("code").toString().equals("0")) {
                    logger.error("调用佰颂短信推送错误，错误码：" + resultMap.get("code") + ",提示信息：" + resultMap.get("msg"));
                    return "1";
                }
                return "0";
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("SendBaisongPlugin.SendSms Exec Exception, Error Info：" + e.getMessage());
            }
        }else{//打包发送
            super.warpParamsForPhoneOrPhones(content, phoneOrPhones, parameters);
        }
        return "1";
    }

    /**
     * 真实调用各个渠道的打包发送
     *
     * @param contentsOrTemplateIdsAndPhones 格式：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...]
     * @param parameters
     * @return
     */
    @Override
    protected String realSendDiffMsg(String contentsOrTemplateIdsAndPhones, Map parameters) {
        List contentAndPhoneList = rebuildParam(contentsOrTemplateIdsAndPhones);
        try {
            Map map = new HashMap();
            map.put("contentArr", contentAndPhoneList);
            map.put("userName", userName);
            //短信内容+加密密钥进行加密
            map.put("md5password", MD5.getMDtLower32(passWord));
//            map.put("md5Content", MD5.MD5Encode(contentsOrTemplateIdsAndPhones.concat(key), "UTF-8"));
            HttpPost post = new HttpPost(postAddr + SEND_MSG_BATCH);
            post.addHeader("Content-Type", "application/json;");
            post.setEntity(new StringEntity(JSONObject.toJSONString(map), "UTF-8"));
            CloseableHttpResponse response = client.execute(post);
            logger.info("[佰颂]发送地址为：" + post + ",内容为：" + map);
            HttpEntity entity = response.getEntity();
            String str = EntityUtils.toString(entity, "UTF-8");
            logger.info("[佰颂]调用短信发送返回的信息：" + str);
            // 关闭
            response.close();
            return "0";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("SendBaisongPlugin.sendDiffSms Exec Exception, Error Info：" + e.getMessage());
        }
        return "1";
    }

    /**
     * 佰颂批量发送参数名重新封装
     * @param contentsOrTemplateIdsAndPhones
     * @return
     */
    private List rebuildParam(String contentsOrTemplateIdsAndPhones) {
        List<Map> list = JSONObject.parseArray(contentsOrTemplateIdsAndPhones, Map.class);
        List newList = new ArrayList();
        for(Map contentAndPhone : list){
            Map map = new HashMap();
            map.put("content",  comSign + contentAndPhone.get("context"));
            map.put("mobile",  contentAndPhone.get("phone"));
            newList.add(map);
        }
        return newList;
    }

}
