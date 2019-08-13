package net.shopxx.controller.api.store;

import com.github.wxpay.sdk.WXPayUtil;
import net.sf.json.JSONObject;
import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.Order;
import net.shopxx.entity.OrderItem;
import net.shopxx.entity.OrderRefunds;
import net.shopxx.entity.PluginConfig;
import net.shopxx.plugin.WeixinPublicPaymentPlugin;
import net.shopxx.service.OrderItemService;
import net.shopxx.service.OrderRefundsService;
import net.shopxx.service.OrderService;
import net.shopxx.service.WXRefundService;
import net.shopxx.util.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.*;

import static org.bouncycastle.asn1.x500.style.RFC4519Style.o;

@Controller
@RequestMapping("/api/store/WXRefund")
public class WXRefundApiController extends BaseController {


    @Autowired
    private OrderService orderService;

    @Autowired
    private RefundUtils refundUtils;

    @Value("${order_limit_time}")
    private String orderCountTime;

    @Inject
    private OrderItemService orderItemService;

    /**
     * 商家根据订单号退款（微信平台）
     *
     * @param request
     * @param orderId    订单Id
     *                   refundMoney 退款金额
     * @param refundCase 退款原因
     * @return
     */
    @GetMapping(path = "/refundByOrderId", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JSONObject refundByOrderId(HttpServletRequest request, String orderId, String refundCase) {
        System.err.println("orderId:" + orderId);
        //System.err.println("refundMoney:"+refundMoney);
        System.err.println("refundCase:" + refundCase);

        String hms = DateUtils.getHMS();
        //申请退款的时间
        String now = DateUtils.gethms(hms);
        //订单统计时间
        String sendOrder = DateUtils.gethms(orderCountTime);

        Order order = orderService.find(Long.valueOf(orderId));

        //该订单已退款
        if (order.getStatus().compareTo(Order.Status.REFUND) == 0) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("status", HttpStatus.BAD_REQUEST);
            jsonObject.put("Msg", "该订单已全额退款");
            return jsonObject;

        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
        long timeTF = calendar.getTime().getTime();
        //退款流水号 (时间戳后8位+5位随机数)
        String refundNum = StringUtils.getId();

        JSONObject jsonObject = new JSONObject();

        //店铺截单前的订单 订单状态：发货状态（可退款）
        if (order.getStatus().compareTo(Order.Status.PENDING_SHIPMENT) == 0) {

            //判断是否可以退款
            if (order.getPayTime() != null) {
                //System.out.println("进来了2");

                if (order.getPayTime().getTime() < DateUtils.getOrderLimitDate(0, this.order_limit_time).getTime()) {

                    return refundUtils.refundByOrderId(request, orderId, order.getSn(), refundNum, order.getAmountPaid(), order.getAmountPaid(), refundCase);

                    //22点到24点的订单
                } else if (order.getPayTime().getTime() >= DateUtils.getOrderLimitDate(0, this.order_limit_time).getTime()
                        && order.getPayTime().getTime() <= timeTF) {

                    return refundUtils.refundByOrderId(request, orderId, order.getSn(), refundNum, order.getAmountPaid(), order.getAmountPaid(), refundCase);
                } else {

                    jsonObject.put("status", HttpStatus.BAD_REQUEST);
                    jsonObject.put("Msg", "不在规定申请退款时间内，退款失败");
                    return jsonObject;
                }
            }else {
                jsonObject.put("status", HttpStatus.BAD_REQUEST);
                jsonObject.put("Msg", "订单异常，退款失败");
                return jsonObject;
            }
        } else {
            jsonObject.put("status", HttpStatus.BAD_REQUEST);
            jsonObject.put("Msg", "不能退款");
            return jsonObject;
        }
    }


//    /**
//     * 店长确定售后退款
//     *
//     * @Auther: Demaxiya
//     * @Create: 2019-08-06 14:36
//     */
//    @GetMapping(path = "/confirmAfterSales", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Object confirmAfterSales(HttpServletRequest request, String orderId, String orderItemId, int refundCount) {
//        try {
//            // 查询对应订单
//            Order order = orderService.find(Long.parseLong(orderId));
//            // 查询对应订单明细
//            OrderItem orderItem = orderItemService.find(Long.parseLong(orderItemId));
//            // 计算应退款金额
//            BigDecimal buyPrice = orderItem.getPrice();
//            BigDecimal total = buyPrice.multiply(new BigDecimal("" + refundCount));
//            JSONObject data = refundUtils.refundByOrderId(request, orderId, order.getSn(), StringUtils.getId(), total.doubleValue(), Double.valueOf(order.getPrice().toString()), "售后商品退款");
//            if (data.get("status").equals("OK")) {
//                return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, data.get("Msg").toString()));
//            }
//            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, data.get("Msg").toString()));
//        } catch (Exception e) {
//            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
//        }
//    }
}
