package net.shopxx.interceptor;

import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Member;
import net.shopxx.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpStatus.FORBIDDEN;

/**
 *  API调用接口拦截器
 *
 *  @Auther: Demaxiya
 *  @Create: 2019-07-12 09:30
 */
public class WxApiInterceptor extends BaseController implements HandlerInterceptor {

    @Inject
    private MemberService memberService;
    /**
     *  请求前处理
     *  @Auther: Demaxiya
     *  @Create: 2019-07-12 09:31
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        //  放开登录拦截
        if (requestUri.indexOf("/api/user/login") > -1 ) {
            return true;
        }
        //  放开首页banner
        if (requestUri.indexOf("/api/wxapp/store/getBanner") > -1) {
            return true;
        }
        //  放开 支付回调
        if (requestUri.indexOf("/api/wxapp/order/payOrderNodify") > -1) {
            return true;
        }
         //  放开 支付回调
        if (requestUri.indexOf("/api/wxapp/order/") > -1) {
            return true;
        }

        //放开微信退款
        if (requestUri.indexOf("/api/store/WXRefund/") > -1) {
            return true;
        }

        //  放开店长登录
        if (requestUri.indexOf("/api/store/storer/login") > -1) {
            return true;
        }

        if(requestUri.indexOf("/api/store/") > -1){
            /*Long businessId=(Long)this.getSession(request,"businessId");
            if(null == businessId){
                this.result(response, Results.status(HttpStatus.OK,this.data(HttpStatus.FORBIDDEN,"无权限访问请重新登录！")));
                return false;
            }*/
            return true;
        }
        // 权限验证
        Member member=this.getCurrentWxInfo(request);
        if(member == null){
            this.result(response, Results.status(HttpStatus.OK,this.data(HttpStatus.FORBIDDEN,"无权限访问请重新登录！")));
            return false;
        }
        // 更新session用户信息
        member=memberService.find(member.getId());
        System.out.println("用户："+member.getId()+"更新session成功");
        this.addSession(request,"member",member);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
