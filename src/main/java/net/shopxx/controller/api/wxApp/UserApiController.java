package net.shopxx.controller.api.wxApp;

import com.fasterxml.jackson.annotation.JsonView;
import com.vdurmont.emoji.EmojiParser;
import net.sf.json.JSONObject;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.*;
import net.shopxx.plugin.WeixinPublicLoginPlugin;
import net.shopxx.service.MemberRankService;
import net.shopxx.service.MemberService;
import net.shopxx.service.SocialUserService;
import net.shopxx.service.UserService;
import net.shopxx.util.HttpUtils;
import net.shopxx.util.JsonUtils;
import net.shopxx.util.WxPhoneUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 微信用户相关接口
 *
 * @author yangli
 */
@Controller("userApiController")
@RequestMapping("/api/user")
public class UserApiController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(UserApiController.class);

    @Inject
    private UserService userService;

    @Inject
    private MemberService memberService;

    @Inject
    private SocialUserService socialUserService;

    @Inject
    private MemberRankService memberRankService;

    @Inject
    private WeixinPublicLoginPlugin weixinPublicLoginPlugin;


    /**
     * 微信用户信息完善接口
     *
     * @param code          微信每次登录code，每次登录会变化  Member 对象 attributeValue0 字段
     * @param nickName      微信昵称    Member 对象 attributeValue1 字段
     * @param avatarUrl     微信头像地址  Member 对象 attributeValue2 字段
     * @param city          城市   Member 对象 area 对象
     * @param encryptedData 手机号-密文  Member 对象 mobile 字段
     * @return
     */
    @GetMapping(path = "/updateUserInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public ResponseEntity<?> updateUserInfo(String code, String nickName, String avatarUrl, String city, String encryptedData, String iv) {
        try {
            //查找到用户(目前只有小程序)
            //通过code 调用官方api拿到用户的唯一ID
            String uniqueId = this.getWxOpenId(code);
            SocialUser socialUser = socialUserService.findByUniqueId(uniqueId);
            if (null == socialUser) {
                return ResponseEntity.ok(this.data(HttpStatus.OK, "找不到用户", null));
            }
            User user = socialUser.getUser();
            Member member = memberService.find(user.getId());
            if (!StringUtils.isEmpty(nickName)) {
                nickName = EmojiParser.parseToHtmlDecimal(nickName);
                member.setAttributeValue1(nickName);
            }
            if (!StringUtils.isEmpty(avatarUrl)) {
                member.setAttributeValue2(avatarUrl);
            }
            if (!StringUtils.isEmpty(encryptedData) && !StringUtils.isEmpty(iv)) {
                member.setMobile(this.getWxMobile(code, encryptedData, iv));
            }
            //city 需要转换成area对象后续处理  TODO
            memberService.update(member);
            // 数据返回
            Map<String, Object> data = new HashMap<>();
            data.put("mobile", member.getMobile());

            logger.info("update member  success,current member,{}", member.getId() + "-" + member.getMobile());

            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", data));
        } catch (Exception e) {
            logger.error("update member  fail,current member=" + nickName, e);
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }

    }

    /**
     * 授权更新手机号
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-12 10:21
     */
    @GetMapping(path = "/wxPhone", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object wxPhone(HttpServletRequest request, String nickName, String avatarUrl, String encryptedData, String iv, String code) {
        try {

            PluginConfig pluginConfig = weixinPublicLoginPlugin.getPluginConfig();
            String appid = pluginConfig.getAttribute("appId");
            String secret = pluginConfig.getAttribute("appSecret");
            String resultSequence = HttpUtils.getInstance()
                    .sendHttpGet("https://api.weixin.qq.com/sns/jscode2session" + "?appid=" + appid + "&secret="
                            + secret + "&grant_type=authorization_code" + "&js_code=" + code);
            String sessionKey = JSONObject.fromObject(resultSequence).getString("session_key");
            // ----------------- 解密获取微信手机号 -----------------
            String resultMsg = WxPhoneUtil.getPhoneNumber(encryptedData, sessionKey, iv);
            Map<String, Object> resultMap = new HashedMap();

            // 获取手机号
            String wxPhone = JSONObject.fromObject(resultMsg).getString("phoneNumber");
            Member member = memberService.findByMobile(wxPhone);
            if ("".equals(wxPhone) || null == wxPhone) {
                resultMap.put("phone", "18523249812");
                member.setMobile("18523249812");
                return ResponseEntity.ok(this.data(HttpStatus.OK, "授权成功！", resultMap));
            }
            Member m = this.getCurrentWxInfo(request);
            if (member == null) {
                if (!StringUtils.isEmpty(nickName)) {
                    nickName = EmojiParser.parseToHtmlDecimal(nickName); // 带有图标的名称转义
                    m.setAttributeValue1(nickName);
                }
                if (!StringUtils.isEmpty(avatarUrl)) {
                    m.setAttributeValue9(avatarUrl);
                }
                m.setMobile(wxPhone);
                memberService.update(m);

            } else if (member != null && member.getMobile().equals(wxPhone)) {
                return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, "当前手机号已被注册！", null));
            }
            resultMap.put("phone", wxPhone);
            logger.info("update member  success,current member nickName,{}", nickName + "-" + wxPhone);
            return ResponseEntity.ok(this.data(HttpStatus.OK, "授权成功！", resultMap));
        } catch (Exception e) {
            logger.error("update member  fail,current member=" + nickName, e);
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }

    /**
     * 登录(更新code)
     *
     * @param code 微信小程序每次登录token
     * @return
     */
    @GetMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public ResponseEntity<?> login(String code, HttpServletRequest request) {
        try {
            //通过code 调用官方api拿到用户的唯一ID
            String uniqueId = this.getWxOpenId(code);
            //查找是否绑定
            //uniqueId = id; 开发模式调接口
            SocialUser socialUser = socialUserService.findByUniqueId(uniqueId);
            Member member = null;
            if (null == socialUser) {//绑定

                member = new Member();
                member.setUsername("wx_xcx" + System.currentTimeMillis());
                member.setPassword("jdhh");
                member.setEmail("wx_xcx" + System.currentTimeMillis() + "@jdhh.com");
                member.setMobile("");
                member.setPoint(0L);
                member.setBalance(BigDecimal.ZERO);
                member.setFrozenAmount(BigDecimal.ZERO);
                member.setAmount(BigDecimal.ZERO);
                member.setIsEnabled(true);
                member.setIsLocked(false);
                member.setLockDate(null);
                member.setLastLoginIp(request.getRemoteAddr());
                member.setLastLoginDate(new Date());
                member.setSafeKey(null);
                member.setMemberRank(memberRankService.findDefault());
                member.setDistributor(null);
                member.setCart(null);
                member.setOrders(null);
                member.setPaymentTransactions(null);
                member.setMemberDepositLogs(null);
                member.setCouponCodes(null);
                member.setReceivers(null);
                member.setReviews(null);
                member.setConsultations(null);
                member.setProductFavorites(null);
                member.setProductNotifies(null);
                member.setSocialUsers(null);
                member.setPointLogs(null);
                member.setWeixinOpenId(uniqueId);
                userService.register(member);


                socialUser = new SocialUser();
                socialUser.setLoginPluginId(code);
                socialUser.setUniqueId(uniqueId);
                socialUser.setUser(member);
                socialUserService.save(socialUser);

                socialUserService.bindUser(member, socialUser, uniqueId);


            } else {//已绑定，更新code
                socialUser.setLoginPluginId(code);
                socialUserService.update(socialUser);
                User user = socialUser.getUser();
                member = memberService.find(user.getId());
            }
            //存放session
            this.addSession(request, "member", member);
            Store store = member.getBuyStore();
            // 数据返回
            Map<String, Object> data = new HashMap<>();
            data.put("wxOpenId", member.getWeixinOpenId());
            data.put("mobile", member.getMobile());
            data.put("sessionId", request.getSession().getId());
            if (store == null) {
                data.put("isStore", 0);
            } else {
                data.put("isStore", 1);
            }
            if (member.getBuyStore() != null) {
                data.put("storeId", store.getId());
                data.put("storeName", member.getBuyStore().getName());
                data.put("storeAddress", member.getBuyStore().getAddress());
            }

            logger.info("用户登录成功,当前用户,{}", member.getId() + "-" + member.getAttributeValue1());
            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", data));

        } catch (Exception e) {

            logger.error(" member  login fail,code=" + code, e);

            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }


    /**
     *  推广码扫描接口
     *  @Auther: Demaxiya
     *  @Create: 2019-08-12 14:01
     */
    @GetMapping(path = "/promotion", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object promotion(HttpServletRequest request,String rcode,String type){
        logger.info("二维码扫码进入！type,{},promotionCode,{}",type,rcode);

        try {
            // 验证参数
            if("".equals(rcode) || null == rcode){
                return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, "邀请码不能为空！"));
            }
            // 查询推广码是否存在
            Boolean exist=memberService.promotionCodeExist(rcode);
            if(!exist){
                return ResponseEntity.ok(this.data(HttpStatus.OK, "非有效推广码！"));
            }
            // 判断当前用户是否满足条件
            Member member=this.getCurrentWxInfo(request);

            if(null == member){
                return ResponseEntity.ok(this.data(HttpStatus.OK, "当前用户未登录！"));
            }

            if(member.getIsNew() == 1 && StringUtils.isNotEmpty(member.getMobile()) && StringUtils.isEmpty(member.getPromotionCode())){
                member.setIsNew(0);
                member.setPromotionCode(rcode);
                memberService.update(member);

                logger.info("二维码扫码绑定成功！memberId,type,{},promotionCode,{}",member.getId(),type,rcode);
                return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！"));
            }
            return ResponseEntity.ok(this.data(HttpStatus.OK, "当前用户不属于新用户！"));
        }catch (Exception e){
            logger.error("二维码扫描失败！type,{},promotionCode,{}",type,rcode,e);
            return ResponseEntity.ok(this.data(HttpStatus.OK, e.getMessage()));
        }
    }

    /**
     * 獲取微信手機號
     *
     * @param jsCode        登錄code
     * @param encryptedData 密文
     * @param iv            加密偏移
     * @return
     */
    private String getWxMobile(String jsCode, String encryptedData, String iv) {
        if (!weixinPublicLoginPlugin.getIsInstalled()) {
            return null;
        }
        try {
            PluginConfig pluginConfig = weixinPublicLoginPlugin.getPluginConfig();
            String appid = pluginConfig.getAttribute("appId");
            String secret = pluginConfig.getAttribute("appSecret");
            String resultSequence = HttpUtils.getInstance()
                    .sendHttpGet("https://api.weixin.qq.com/sns/jscode2session" + "?appid=" + appid + "&secret="
                            + secret + "&grant_type=authorization_code" + "&js_code=" + jsCode);
            String sessionKey = JSONObject.fromObject(resultSequence).getString("session_key");
            // ----------------- 解密获取微信手机号 -----------------
            String resultMsg = WxPhoneUtil.getPhoneNumber(encryptedData, sessionKey, iv);
            return JSONObject.fromObject(resultMsg).getString("phoneNumber");
        } catch (Exception e) {
            logger.error("获取微信手机号失败,code=" + jsCode, e);
        }

        return null;
    }

    /**
     * 获取微信用户的唯一id
     *
     * @param jsCode
     * @return
     */
    private String getWxOpenId(String jsCode) {
        String openId = null;

        if (!weixinPublicLoginPlugin.getIsInstalled()) {
            return openId;
        }

        try {
            PluginConfig pluginConfig = weixinPublicLoginPlugin.getPluginConfig();
            String appid = pluginConfig.getAttribute("appId");
            String secret = pluginConfig.getAttribute("appSecret");
            String resultSequence = HttpUtils.getInstance().sendHttpGet(
                    "https://api.weixin.qq.com/sns/jscode2session" +
                            "?appid=" + appid +
                            "&secret=" + secret +
                            "&grant_type=authorization_code" +
                            "&js_code=" + jsCode);

            openId = JSONObject.fromObject(resultSequence).getString("openid");

        } catch (Exception e) {

            logger.error("获取微信openid失败,code" + jsCode, e);
        }

        return openId;
    }




}
