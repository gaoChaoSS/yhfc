package net.shopxx.plugin.sendPlugin;

import net.shopxx.entity.KeyValue;
import net.shopxx.plugin.sendPlugin.channel.SendBaishitongPlugin;
import net.shopxx.plugin.sendPlugin.channel.SendBaisongPlugin;
import net.shopxx.plugin.sendPlugin.channel.SendQiniuPlugin;
import net.shopxx.service.KeyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description  短信发送插件工厂类
 *              工厂内部会自动获取当前系统默认的短信发送插件
 * @auther wangli
 * @create 2019-08-09 18:07
 */
@Component("sendSmsPluginFactory")
public class SendSmsPluginFactory {

    private Map<String, ISendSmsPlugin> sendSmsPluginHashMap = new HashMap<>();

    @Inject
    private List<ISendSmsPlugin> sendSmsPluginList;

    //使用百事通发送
    public static String SENDTYPE_BAISHITONG = "baishitong";

    //使用七牛云发送
    public static String SENDTYPE_QINIU = "qiniu";

    //使用佰颂发送
    public static String SENDTYPE_BAISONG = "baisong";

    /**
     * 默认的短信发送插件
     */
    private static String DEFAULT_SENDSMSPLUGIN = "";

    @Autowired
    private KeyValueService keyValueService;

    //默认的短信发送插件key名，在key_value表中定义
    private final static String smsPluginKey = "sendSmsPlugin";

    @PostConstruct
    public void init() {
        sendSmsPluginList.forEach(sendSmsPlugin -> {
            sendSmsPluginHashMap.put(sendSmsPlugin.getType(), sendSmsPlugin);
        });
        DEFAULT_SENDSMSPLUGIN = keyValueService.getDefaultForKey(smsPluginKey);
    }


    /**
     * 获取默认的短信发送插件类型
     * @return 返回默认的短信发送插件对象
     */
    public ISendSmsPlugin getSendSmsPlugin() {
        return sendSmsPluginHashMap.get(DEFAULT_SENDSMSPLUGIN);
    }

    /**
     * 获取指定短信插件
     * @param sendType 手动指定的短信插件标识
     *                 可以直接通过SendSmsPluginFactory.SENDTYPE_xxx来选择
     * @return 返回指定的短信发送插件对象
     */
    public ISendSmsPlugin getSendSmsPlugin(String sendType) {
        return sendSmsPluginHashMap.get(sendType);
    }
}