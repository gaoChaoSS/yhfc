package net.shopxx.controller.api.store;


import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Order;
import net.shopxx.entity.OrderNoticeSms;
import net.shopxx.entity.Store;
import net.shopxx.plugin.sendPlugin.SendSmsPluginFactory;
import net.shopxx.plugin.sendPlugin.channel.SendBaishitongPlugin;
import net.shopxx.service.KeyValueService;
import net.shopxx.service.OrderNoticeSmsService;
import net.shopxx.service.OrderService;
import net.shopxx.service.StoreService;
import net.shopxx.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 取货通知
 *
 * @Auther: Demaxiya
 * @Create: 2019-08-01 11:41
 */
@Controller("sendOrderSms")
@RequestMapping("/api/store/sendSms")
public class SendOrderSmsController extends BaseController {

    @Inject
    private OrderService orderService;

    @Inject
    private StoreService storeService;


    @Inject
    private SendSmsPluginFactory sendSmsPluginFactory;

    @Inject
    private OrderNoticeSmsService orderNoticeSmsService;


    /**
     * 店长发送取货通知
     *
     * @Auther: Demaxiya
     * @Create: 2019-08-01 12:40
     */
    @GetMapping(path = "/sendSms", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object sendSms(HttpServletRequest request, Long storeId) {
        try {
            Store store = storeService.find(storeId);
            Map<String, Object> smsData = orderService.findNoTakeOrderByStore(store);
            if (null == smsData || smsData.size() < 1) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "请勿重复通知！"));
            }
            List<Map<String, Object>> msg = (List<Map<String, Object>>) smsData.get("msg");
            List<Order> orders = (List<Order>) smsData.get("orders");
            List<OrderNoticeSms> record = (List<OrderNoticeSms>) smsData.get("smsRecord");

            String msgs = JsonUtils.toJson(msg);
            String result = sendSmsPluginFactory.getSendSmsPlugin().sendDiffSmsToPhones(msgs, null,true);
            System.out.println("发送短信结果："+result);
            if ("0".equals(result)) {
                for (Order order : orders) {
                    order.setSendSuccess(true);
                    orderService.update(order);
                }

                for (OrderNoticeSms orderNoticeSms : record) {
                    orderNoticeSms.setSuccessDate(new Date());
                    orderNoticeSms.setSucess(true);
                    orderNoticeSmsService.update(orderNoticeSms);
                }
            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "通知成功！"));
        } catch (Exception e) {
            e.printStackTrace();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
