package net.shopxx.plugin.sendPlugin.channel;

import com.alibaba.fastjson.JSON;
import com.qiniu.http.Response;
import com.qiniu.sms.SmsManager;
import com.qiniu.util.Auth;
import net.shopxx.plugin.sendPlugin.AbstractSendSmsPlugin;
import net.shopxx.plugin.sendPlugin.SendSmsPluginFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * @Description 调用七牛云短信发送插件
 * <pre>
 *      七牛云控制台：https://portal.qiniu.com/sms/template
 *      xuxiaoming@jdsq360.com/Yhfc2016!
 *  </pre>
 * @auther wangli
 * @create 2019-08-09 10:35
 */
@Component("sendQiniuPlugin")
public class SendQiniuPlugin extends AbstractSendSmsPlugin {

    private final static Logger logger = LoggerFactory.getLogger(SendQiniuPlugin.class);

    //七牛云正式的ACCESS_KEY
    private String ACCESS_KEY = "9gyyPFJ6CYPLibVa6ayjlvGLL8fk-kRvYsLTVpzC";

    //七牛云正式的SECRET_KEY
    private String SECRET_KEY = "uRKs4IcT1ZM4J27pl3FlqHIDKkvCwPGoXkttnLNr";

    private SmsManager smsManager = null;

    @PostConstruct
    public void init() {
        Auth auth = Auth.create(ACCESS_KEY, SECRET_KEY);
        // 实例化一个SmsManager对象
        smsManager = new SmsManager(auth);
    }

    /**
     * 调用真实发送者
     *
     * @param contentOrTemplateId
     * @param phoneOrPhones
     * @param parameters
     * @return
     */
    @Override
    protected String realSendMsg(String contentOrTemplateId, String phoneOrPhones, Map parameters) {
        if (StringUtils.isNotBlank(phoneOrPhones) && phoneOrPhones.indexOf("，") > -1) {
            phoneOrPhones = phoneOrPhones.replaceAll("，", ",");
        }
        String[] phoneArray = phoneOrPhones.split(",");
        try {
            Response resp = smsManager.sendMessage(contentOrTemplateId, phoneArray, parameters);
            logger.info("发送七牛云短信的参数:" + JSON.toJSONString(parameters) + ",模板ID：" + contentOrTemplateId + ",接收手机号：" + phoneOrPhones);
            String result = resp.bodyString();
            if (result.contains("error")) {
                logger.error("七牛云短信推送调用失败：" + result);
                return "1";
            }
            logger.debug(resp.bodyString());
            return "0";
        } catch (Exception e) {
            logger.error("七牛云短信调用失败：" + e.getMessage());
        }
        return "1";
    }

    /**
     * @param contentsOrTemplateIdsAndPhones 格式：[{"context" : "tempateId1" , "phone" : "phone1"},{"context" : "tempateId2" , "phone" : "phone2"},...]
     * @param parameters
     * @return
     */
    @Override
    protected String realSendDiffMsg(String contentsOrTemplateIdsAndPhones, Map parameters) {
        try{
            List<Map> contentsOrtemplateIdsList = JSON.parseArray(contentsOrTemplateIdsAndPhones, Map.class);
            int smsCount = contentsOrtemplateIdsList.size();
            if (smsCount > 0 && smsCount < 3000) {
                for (Map context : contentsOrtemplateIdsList) {
                    String templateId = context.get("context").toString();
                    String phone = context.get("context").toString();
                    //后期成功和失败的计数可以在此处添加
                    Response resp = smsManager.sendMessage(templateId, new String[]{phone}, parameters);
                }
                return "0";
            } else {
                logger.error("发送短信失败!,手机号码个数在1-3000，当前手机号码数量=" + smsCount);
            }
        } catch (Exception e){
            e.printStackTrace();
            logger.error("调用七牛云异常：" + e.getMessage());
        }
        return "1";
    }

    /**
     * 实例所支持的渠道类型
     *
     * @return
     */
    @Override
    public String getType() {
        return SendSmsPluginFactory.SENDTYPE_QINIU;
    }
}
