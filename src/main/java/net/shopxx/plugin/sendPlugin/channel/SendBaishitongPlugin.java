package net.shopxx.plugin.sendPlugin.channel;

import net.shopxx.plugin.sendPlugin.AbstractSendSmsPlugin;
import net.shopxx.plugin.sendPlugin.SendSmsPluginFactory;
import net.shopxx.util.MD5;
import net.shopxx.util.WebUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


/****
 * 百事通短信发送插件
 */
@Component("sendBaishitongPlugin")
public class SendBaishitongPlugin extends AbstractSendSmsPlugin {

    private final static Logger logger = LoggerFactory.getLogger(SendBaishitongPlugin.class);
    @Value("${jd.sms.url}")
    private String smsUrl = "http://bst.8315.cn:9892/cmppweb/";
    @Value("${jd.sms.accName}")
    private String accName = "147597";
    @Value("${jd.sms.password}")
    private String password = "000000";
    @Value("${jd.sms.singnname}")
    private String singnname = "【聚点社区】";

    /**
     * 单一发送
     *
     * @param contentOrTemplateId 短信内容
     * @param phoneOrPhones 接收手机号
     * @param parameters 可为空
     * @return
     */
    @Override
    protected String realSendMsg(String contentOrTemplateId, String phoneOrPhones, Map parameters) {
        Map<String, Object> params = new HashMap<String, Object>();
        try {
            String specialSmsUrl = smsUrl + "sendsms";
            params.put("uid", accName);
            params.put("pwd", MD5.MD5Encode(password, ""));
            String sphone = phoneOrPhones;
            if (StringUtils.isNotBlank(phoneOrPhones) && phoneOrPhones.indexOf("，") > -1) {
                sphone = phoneOrPhones.replaceAll("，", ",");
            }
            int plen = sphone.split(",").length;
            if (plen > 0 && plen < 3000) {
                params.put("srcphone", "10690300");
                params.put("mobile", sphone);
                params.put("msg", URLEncoder.encode(singnname + contentOrTemplateId, "UTF-8"));
                logger.info("[百事通]发送短信的参数:" + params + "地址：" + specialSmsUrl);
                String result = WebUtils.httpPost(specialSmsUrl, params);
                logger.info("[百事通]发送短信返回结果(0,开始表示成功)===" + result);
                if (StringUtils.isNotBlank(result)) {
                    String[] res = result.split(",");
                    return res[0];
                }
            } else {
                logger.error("[百事通]发送短信失败!,手机号码个数在1-3000，当前手机号码数量=" + plen);
            }
        } catch (Exception e) {
            logger.error("发送短信失败!", e);
        }
        return "1";
    }

    /**
     * 打包发送
     * @param contentsOrTemplateIdsAndPhones
     *          实际入参格式：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...]
     * @param parameters 可为空
     * @return
     */
    @Override
    protected String realSendDiffMsg(String contentsOrTemplateIdsAndPhones, Map parameters) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (StringUtils.isBlank(contentsOrTemplateIdsAndPhones)) {
            return "1";
        }
        try {
            String specialSmsUrl = smsUrl + "sendsmspkg";
            params.put("uid", accName);
            params.put("pwd", MD5.MD5Encode(password, ""));
            params.put("srcphone", "10690300");
            params.put("msg", contentsOrTemplateIdsAndPhones);
            logger.info("[百事通]发送短信的参数:" + params + "地址：" + specialSmsUrl);
            String result = WebUtils.httpPost(specialSmsUrl, params);
            logger.info("[百事通]发送短信返回结果(0,开始表示成功)===" + result);
            if (StringUtils.isNotBlank(result)) {
                String[] res = result.split(",");
                return res[0];
            }
        } catch (Exception e) {
            logger.error("[百事通]发送短信失败!", e);
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
        return SendSmsPluginFactory.SENDTYPE_BAISHITONG;
    }

}
