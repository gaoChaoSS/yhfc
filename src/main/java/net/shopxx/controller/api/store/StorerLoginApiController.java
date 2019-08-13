package net.shopxx.controller.api.store;


import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Business;
import net.shopxx.entity.Store;
import net.shopxx.service.BusinessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 *  店长端登录
 *  @Auther: Demaxiya
 *  @Create: 2019-07-23 09:36
 */

@Controller("storerLoginController")
@RequestMapping("/api/store/storer")
public class StorerLoginApiController extends BaseController {

    @Inject
    private BusinessService businessService;

    /**
     *  店长登录
     *  @Auther: Demaxiya
     *  @Create: 2019-07-23 18:13
     */
    @GetMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object login(HttpServletRequest request,String account,String pwd){
        try{
            Business business=(Business)businessService.findByAccountAndPwd(account,pwd);
            if(null == business){
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前账号或者密码错误！"));
            }
            Store store=business.getStore();
            if(null == store){
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前商家未绑定门店！"));
            }

            if(!business.getIsEnabled()){
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前店铺被禁用！"));
            }
            this.addSession(request,"businessId",business.getId());
            Map<String,Object> data=new HashMap<>();
            data.put("businessId",business.getId());
            data.put("storeId",store.getId());
            data.put("storeName",store.getName());
            data.put("sessionId",request.getSession().getId());
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "登录成功！",data));
        }catch (Exception e){
            e.printStackTrace();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
