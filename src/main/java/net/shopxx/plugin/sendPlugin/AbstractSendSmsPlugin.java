package net.shopxx.plugin.sendPlugin;

import com.alibaba.fastjson.JSON;
import com.mchange.v1.identicator.StrongIdentityIdenticator;
import net.shopxx.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @Description 短信发送插件抽象类
 * @auther wangli
 * @create 2019-08-09 14:32
 */
public abstract class AbstractSendSmsPlugin implements ISendSmsPlugin{

    private final static Logger logger = LoggerFactory.getLogger(AbstractSendSmsPlugin.class);

    protected static ExecutorService executorService = Executors.newCachedThreadPool();

    protected String excuteThread(FutureTask<String> task) {
        String res = "1";
        try {
            executorService.execute(task);
            while (true) {
                if (task.isDone()) {
                    // 获得线程执行结果
                    res = task.get();
                    break;
                }
            }
        } catch (Exception e) {

        }
        executorService.shutdown();
        return res;
    }

    /***
     * 单个发送短信的接口，一次只能给一个手机号发送短信，同时支持同一内容给不同的多个手机发送
     *  注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentOrTemplateId 短信内容或者模板ID
     * @param phoneOrPhones  接收短信的手机，多个手机号以英文逗号分割
     *               eg：“15982210555，15982210556”
     * @param parameters  非使用七牛云插件可以为null。用于七牛云，替换短信模板中的魔法变量
     * @param async 是否异步
     * @return
     *      返回1  发送失败
     */
    @Override
    public final String sendSms(String contentOrTemplateId, String phoneOrPhones, Map parameters, boolean async){
        if(StringUtils.isBlank(contentOrTemplateId) || StringUtils.isBlank(phoneOrPhones)){
            logger.error("缺失参数，无法发送短信！");
        }else{
            try{
                if(async){
                    Thread.currentThread().sleep(2000);
                    FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            return realSendMsg(contentOrTemplateId, phoneOrPhones, parameters);
                        }
                    });
                    return excuteThread(task);
                }else{
                    return realSendMsg(contentOrTemplateId, phoneOrPhones, parameters);
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.error("Method sendSms Exec Exception, Error Info:" + e.getMessage());
            }
        }
        return "1";
    }

    /**
     * 调用真实发送者进行单一发送，交由各个发送渠道实现
     * @param contentOrTemplateId
     * @param phoneOrPhones
     * @param parameters
     * @return
     */
    protected abstract String realSendMsg(String contentOrTemplateId, String phoneOrPhones, Map parameters);

    /***
     * 多个发送短信的接口，一次能给多个手机号发送同一内容的短信
     *  注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentOrTemplateId 短信内容或者模板ID
     * @param phoneList  List 接收短信的手机
     * @param parameters  非使用七牛云插件可以为null。用于七牛云，替换短信模板中的魔法变量
     * @param async 是否异步
     * @return
     *      返回1  发送失败
     */
    @Override
    public final String sendSmsToPhones(String contentOrTemplateId, List<String> phoneList, Map parameters, boolean async){
        if(StringUtils.isBlank(contentOrTemplateId) || phoneList.isEmpty()){
            logger.error("缺失参数，无法发送短信！");
        }else{
            try{
                if(async){
                    Thread.currentThread().sleep(2000);
                    FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            return sendMsgMore(contentOrTemplateId, phoneList, parameters);
                        }
                    });
                    return excuteThread(task);
                }else{
                    return sendMsgMore(contentOrTemplateId, phoneList, parameters);
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.error("Method sendSms Exec Exception, Error Info:" + e.getMessage());
            }
        }
        return "1";
    }

    /**
     * 调用真实发送者进行打包发送，交由各个发送渠道实现
     *  注意：默认由抽象类实现，直接转换参数后调用sendMsg函数即可，特色渠道可以重写改方法
     * @param contentOrTemplateId
     * @param phoneList
     * @param parameters
     * @return
     */
    protected String sendMsgMore(String contentOrTemplateId, List<String> phoneList, Map parameters){
        StringBuffer stringBuffer = new StringBuffer();
        for(int i = 0; i< phoneList.size(); i++){
            stringBuffer.append(phoneList.get(i));
            if(i < phoneList.size()-1){
                stringBuffer.append(",");
            }
        }
        return realSendMsg(contentOrTemplateId, stringBuffer.toString(), parameters);
    }

    /**
     * 给同一个手机号发送多条不同短信的内容
     * 注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentsOrTemplateIds 多条短信内容或者多个模板ID，用英文逗号分隔
     *                              eg："content1,content2,..." 或者 "templateId2,templateId2,..."
     * <p>注意：使用百事通断行发送时，content的值需要加上签名和转码 URLEncoder.encode(singnname(签名) + content, "UTF-8")</p>
     *
     *
     * @param phone                 接收短信的手机号
     * @param parameters            用于七牛云，替换短信模板中的魔法变量
     * @param async                 是否异步
     * @return 返回1   发送失败
     */
    @Override
    public final String sendDiffSmsToPhone(String contentsOrTemplateIds, String phone, Map parameters, boolean async){
        if(StringUtils.isBlank(contentsOrTemplateIds) || StringUtils.isBlank(phone)){
            logger.error("缺失参数，无法发送短信！");
        }else{
            try{
                if(async){
                    Thread.currentThread().sleep(2000);
                    FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            return warpParamsForContentOrTemplateId(contentsOrTemplateIds, phone, parameters);
                        }
                    });
                    return excuteThread(task);
                }else{
                    return warpParamsForContentOrTemplateId(contentsOrTemplateIds, phone, parameters);
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.error("Method sendSms Exec Exception, Error Info:" + e.getMessage());
            }
        }
        return "1";
    }

    /**
     * 包装短信调用的参数，同一个contentsOrTemplateIds，多个phoneOrPhones
     *
     * @param contentOrTemplateId
     *           eg："content1,content2,..." 或者 "templateId2,templateId2,..."
     * @param phoneOrPhones
     * @param parameters
     * @return  把简单的content和template包装成多对多的发送格式
     *          eg：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...] 或者
     *          [{"context" : "templateId1" , "phone" : "phone1"},{"context" : "templateId2" , "phone" : "phone2"},...]
     */
    protected String warpParamsForPhoneOrPhones(String contentOrTemplateId, String phoneOrPhones, Map parameters) {
        String[] phoneOrPhonesArray = phoneOrPhones.split(",");
        List phoneOrPhonesList = new ArrayList();
        for(String phone : phoneOrPhonesArray){
            Map contentsAndPhones = new HashMap();
            contentsAndPhones.put("context", contentOrTemplateId);
            contentsAndPhones.put("phone", phone);
            phoneOrPhonesList.add(contentsAndPhones);
        }
        return realSendDiffMsg(JSON.toJSONString(phoneOrPhonesList), parameters);
    }

    /**
     * 包装短信调用的参数，多个contentsOrTemplateIds，同一个phone
     *
     * @param contentsOrTemplateIds
     *           eg："content1,content2,..." 或者 "templateId2,templateId2,..."
     * @param phone
     * @param parameters
     * @return  把简单的content和template包装成多对多的发送格式
     *          eg：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...] 或者
     *          [{"context" : "templateId1" , "phone" : "phone1"},{"context" : "templateId2" , "phone" : "phone2"},...]
     */
    protected String warpParamsForContentOrTemplateId(String contentsOrTemplateIds, String phone, Map parameters){
        String[] contentsOrTemplateIdsArray = contentsOrTemplateIds.split(",");
        List contentsOrTemplateIdsList = new ArrayList();
        for(String contentOrTemplateId : contentsOrTemplateIdsArray){
            Map contentsAndPhones = new HashMap();
            contentsAndPhones.put("context", contentOrTemplateId);
            contentsAndPhones.put("phone", phone);
            contentsOrTemplateIdsList.add(contentsAndPhones);
        }
        return realSendDiffMsg(JSON.toJSONString(contentsOrTemplateIdsList), parameters);
    }

    /**
     * 真实调用各个渠道的打包发送
     * @param contentsOrTemplateIdsAndPhones
     *          格式：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...]
     * @param parameters
     * @return
     */
    protected abstract String realSendDiffMsg(String contentsOrTemplateIdsAndPhones, Map parameters);

    /**
     * 该方法支持给不同的手机号发送多条不同内容的短信
     *  注意：如果短信内容中包含魔法变量需要在调用该方法之前进行赋值。
     *
     * @param contentsOrTemplateIdsAndPhones 多条短信内容和对应接收短信的手机号
     *                         eg：[{"context" : "content1" , "phone" : "phone1"},{"context" : "content2" , "phone" : "phone2"},...] 或者
     *                             [{"templateId" : "templateId1" , "phone" : "phone1"},{"templateId" : "templateId2" , "phone" : "phone2"},...]
     * @param parameters   非使用七牛云插件可以为null。用于七牛云，替换短信模板中的魔法变量
     * @param async  是否异步
     * @return
     *      返回1   发送失败
     */
    @Override
    public final String sendDiffSmsToPhones(String contentsOrTemplateIdsAndPhones, Map parameters, boolean async){
        if(StringUtils.isBlank(contentsOrTemplateIdsAndPhones) || parameters.isEmpty()){
            logger.error("缺失参数，无法发送短信！");
        }else{
            try{
                if(async){
                    Thread.currentThread().sleep(2000);
                    FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
                        @Override
                        public String call() throws Exception {
                            return realSendDiffMsg(contentsOrTemplateIdsAndPhones, parameters);
                        }
                    });
                    return excuteThread(task);
                }else{
                    return realSendDiffMsg(contentsOrTemplateIdsAndPhones, parameters);
                }
            }catch (Exception e){
                e.printStackTrace();
                logger.error("Method sendSms Exec Exception, Error Info:" + e.getMessage());
            }
        }
        return "1";
    }

}
