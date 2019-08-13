package net.shopxx.controller.api.store;

import net.sf.json.JSONObject;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.controller.api.wxApp.OrderApiController;
import net.shopxx.entity.*;
import net.shopxx.service.*;
import net.shopxx.util.RefundUtils;
import net.shopxx.util.StringUtils;
import org.aspectj.lang.annotation.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.*;

/**
 * 店长售后
 *
 * @Auther: Demaxiya
 * @Create: 2019-08-06 15:36
 */
@Controller("storerAfterSales")
@RequestMapping("/api/store/afterSales")
public class StorerAfterSalesController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(StorerAfterSalesController.class);


    @Autowired
    private RefundUtils refundUtils;

    @Autowired
    private OrderService orderService;

    @Inject
    private OrderItemService orderItemService;

    @Inject
    private AftersalesService aftersalesService;

    @Inject
    private StoreService storeService;

    @Inject
    private MemberService memberService;

    @Inject
    private AftersalesItemService aftersalesItemService;

    @Inject
    private OrderReturnsService orderReturnsService;


    /**
     * 店长确定售后退款
     *
     * @Auther: Demaxiya
     * @Create: 2019-08-06 14:36
     */
    @GetMapping(path = "/confirmAfterSales", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object confirmAfterSales(HttpServletRequest request, Long orderId, Long orderItemId, int refundCount, Long afterSalesId, Long afterSalesItemId) {
        try {
            // 查询对应订单
            Order order = orderService.find(orderId);
            // 查询对应订单明细
            OrderItem orderItem = orderItemService.find(orderItemId);
            // 查询售后单
            Aftersales aftersales = aftersalesService.find(afterSalesId);
            // 查询售后单明细
            AftersalesItem aftersalesItem = aftersalesItemService.find(afterSalesItemId);
            // 计算应退款金额
            BigDecimal buyPrice = orderItem.getPrice();
            BigDecimal total = buyPrice.multiply(new BigDecimal("" + refundCount));
            logger.info("购买价格：" + buyPrice + "退货数量：" + refundCount + "退款金额：" + total);
            logger.info("退款金额：" + total.doubleValue() + "_______订单金额：" + Double.valueOf(order.getPrice().toString()) + "订单编号：" + order.getSn());
            JSONObject data = refundUtils.refundByOrderId(request, orderId + "", order.getSn(), StringUtils.getId(), order.getAmountPaid(), total, "售后商品退款");
            if (data.get("status").equals("OK")) {

                // 更新实际退货数量
                aftersalesItem.setActualQuantity(refundCount + aftersalesItem.getActualQuantity());
                // 退款成功 售后单已完成
                aftersales.setStatus(Aftersales.Status.STORERCONFIRM_MONEY_COMPLETED);

                List<AftersalesItem> items = new ArrayList<>();
                items.add(aftersalesItem);
                aftersales.setAftersalesItems(items);
                aftersalesService.update(aftersales);
                //aftersalesItemService.update(aftersalesItem);


                // 生成订单退货
                OrderReturns orderReturns = new OrderReturns();
                // 订单退货明细项
                List<OrderReturnsItem> orderReturnsItems = orderReturns.getOrderReturnsItems();
                orderReturns.setCreatedDate(new Date());
                orderReturns.setLastModifiedDate(new Date());
                orderReturns.setVersion(1L);
                orderReturns.setSn(order.getSn());
                orderReturns.setOrder(order);
                orderReturns.setArea(order.getArea());
                for (AftersalesItem aftersalesItem1 : aftersales.getAftersalesItems()) {
                    OrderReturnsItem orderReturnsItem = new OrderReturnsItem();
                    orderReturnsItem.setName(orderItem.getName());
                    orderReturnsItem.setOrderReturns(orderReturns);
                    orderReturnsItem.setQuantity(aftersalesItem1.getActualQuantity());
                    orderReturnsItem.setSn(aftersalesItem1.getOrderItem().getOrder().getSn());
                    orderReturnsItem.setSpecifications(orderItem.getSpecifications());
                    orderReturnsItems.add(orderReturnsItem);
                }
                orderReturnsService.save(orderReturns);
                return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, data.get("Msg").toString()));
            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, data.get("Msg").toString()));
        } catch (Exception e) {
            e.printStackTrace();
            //logger.error();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 满足店长处理的售后单
     *
     * @Auther: Demaxiya
     * @Create: 2019-08-06 15:38
     */
    @GetMapping(path = "/afterSalesList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object afterSalesList(HttpServletRequest request, Pageable pageable, Long storeId, String phone) {
        try {
            // 查询店铺
            Store store = storeService.find(storeId);
            if (null == store) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "店铺不存在，请联系运营人员！"));
            }
            if (!store.getIsEnabled()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前店铺已下架，请联系运营人员！"));
            }

            Page<Aftersales> page = null;
            // 根据搜索字段查询用户
            if (!"".equals(phone) && null != phone) {
                Member member = memberService.findByMobile(phone);
                if (null == member) {
                    return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "暂无更多数据！", new ArrayList<Map<String, Object>>()));
                }
                page = aftersalesService.findPage(Aftersales.Type.AFTERSALES_RETURNS, Aftersales.Status.STORERCONFIRM, member, store, pageable);
            } else {
                page = aftersalesService.findPage(Aftersales.Type.AFTERSALES_RETURNS, Aftersales.Status.STORERCONFIRM, null, store, pageable);
            }
            List<Aftersales> data = page.getContent();
            if (data.size() < 0) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "暂无更多数据！", new ArrayList<Map<String, Object>>()));
            }

            // 返回数据组装
            List<Map<String, Object>> returnData = new ArrayList<>();
            for (Aftersales aftersales : data) {
                Map<String, Object> afterSalesInfo = new HashMap<>();
                Member member = aftersales.getMember();
                if (null != member) {
                    afterSalesInfo.put("afterSalesId", aftersales.getId());
                    afterSalesInfo.put("applyUser", member.getAttributeValue1()); // 申请人
                    afterSalesInfo.put("mobile", member.getMobile());             // 电话
                }
                String orderSn = "";
                Long orderId = null;
                // 售后明细项
                List<Map<String, Object>> items = new ArrayList<>();
                if (aftersales.getAftersalesItems().size() > 0) {
                    for (AftersalesItem aftersalesItem : aftersales.getAftersalesItems()) {
                        Map<String, Object> item = new HashMap<>();
                        Order order = aftersalesItem.getOrderItem().getOrder();
                        orderSn = order.getSn();
                        orderId = order.getId();
                        item.put("afterSalesItemId", aftersalesItem.getId());                   // 售后明细ID
                        item.put("refundCount", aftersalesItem.getQuantity() - aftersalesItem.getActualQuantity());                  // 应退数量
                        item.put("productImage", aftersalesItem.getOrderItem().getThumbnail()); // 商品图片
                        item.put("productName", aftersalesItem.getOrderItem().getName());       // 商品名称
                        item.put("orderItemId", aftersalesItem.getOrderItem().getId());          // 订单明细ID
                        items.add(item);
                    }
                }
                afterSalesInfo.put("orderSn", orderSn); // 订单号
                afterSalesInfo.put("orderId", orderId); // 订单ID
                afterSalesInfo.put("items", items);
                returnData.add(afterSalesInfo);

            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", returnData));
        } catch (Exception e) {
            e.printStackTrace();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
