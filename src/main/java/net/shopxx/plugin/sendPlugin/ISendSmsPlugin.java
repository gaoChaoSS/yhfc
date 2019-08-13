package net.shopxx.plugin.sendPlugin;

import java.util.List;
import java.util.Map;

/**
 * @Description  短信发送插件接口类
 * @auther wangli
 * @create 2019-08-09 23:35
 */
public interface ISendSmsPlugin {

    /**
     * 实例所支持的渠道类型
     * @return
     */
    String getType();

    /***
     * 单个发送短信的接口，一次只能给一个手机号发送短信，同时支持同一内容给不同的多个手机发送
     *  注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentOrTemplateId 短信内容或者模板ID, 如果使用百事通插件
     *                            如果使用的是百事通和佰颂插件，则直接传入短信内容
     *                            如果使用的七牛插件，则传入七牛云控制台对应短信模板的ID
     * @param phoneOrPhones  接收短信的手机，多个手机号以英文逗号分割
     *               eg：“15982210555，15982210556”
     * @param parameters  非使用七牛云插件可以为null。用于七牛云，替换短信模板中的魔法变量
     * @param async 是否异步
     * @return
     *      返回1  发送失败
     */
    String sendSms(final String contentOrTemplateId, final String phoneOrPhones, final Map parameters, final boolean async);

    /***
     * 多个发送短信的接口，一次能给多个手机号发送同一内容的短信
     *  注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentOrTemplateId 短信内容或者模板ID
     *                            如果使用的是百事通和佰颂插件，则直接传入短信内容
     *                            如果使用的七牛插件，则传入七牛云控制台对应短信模板的ID
     * @param phoneList  List 接收短信的手机
     * @param parameters  非使用七牛云插件可以为null。用于七牛云，替换短信模板中的魔法变量
     * @param async 是否异步
     * @return
     *      返回1  发送失败
     */
    String sendSmsToPhones(final String contentOrTemplateId, final List<String> phoneList, final Map parameters, final boolean async);

    /**
     * 给同一个手机号发送多条不同短信的内容
     * 注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentsOrTemplateIds 多条短信内容或者多个模板ID，用英文逗号分隔
     *                              如果使用的是百事通和佰颂插件，则直接传入短信内容
     *                              如果使用的七牛插件，则传入七牛云控制台对应短信模板的ID
     *                              eg："content1,content2,..." 或者 "templateId2,templateId2,..."
     * <p>注意：使用百事通断行发送时，content的值需要加上签名和转码 URLEncoder.encode(singnname(签名) + content, "UTF-8")</p>
     *
     *
     * @param phone                 接收短信的手机号
     * @param parameters            用于七牛云，替换短信模板中的魔法变量
     * @param async                 是否异步
     * @return 返回1   发送失败
     */
    String sendDiffSmsToPhone(final String contentsOrTemplateIds, final String phone, final Map parameters, final boolean async);

    /**
     * 该方法支持给不同的手机号发送多条不同内容的短信
     *  注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentsOrTemplateIdsAndPhones 多条短信内容和对应接收短信的手机号
     *                                       如果使用的是百事通和佰颂插件，则直接传入短信内容
     *                                       如果使用的七牛插件，则传入七牛云控制台对应短信模板的ID
     *                         eg：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...] 或者
     *                             [{"templateId" : "templateId1" , "phone" : "phone1"},{"templateId" : "templateId2" , "phone" : "phone2"},...]
     * @param paramters   非使用七牛云插件可以为null。用于七牛云，替换短信模板中的魔法变量
     * @param async  是否异步
     * @return
     *      返回1   发送失败
     */
    String sendDiffSmsToPhones(final String contentsOrTemplateIdsAndPhones, final Map paramters, final boolean async);

}
