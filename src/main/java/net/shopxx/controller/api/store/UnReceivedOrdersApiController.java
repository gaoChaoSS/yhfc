package net.shopxx.controller.api.store;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;
import net.shopxx.Results;
import net.shopxx.dao.SnDao;
import net.shopxx.entity.*;
import net.shopxx.service.*;
import net.shopxx.util.GsonUtil;
import net.shopxx.util.JsonUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.annotation.JsonView;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.OrderCountByMember.orderCountByMember;

/**
 * 待取货订单
 *
 * @author yangli
 */
@Controller("unReceivedOrders")
@RequestMapping("/api/store/unReceivedOrders")
public class UnReceivedOrdersApiController extends BaseController {

    @Inject
    private OrderService orderService;

    @Inject
    private StoreService storeService;

    @Inject
    private OrderItemService orderItemService;

    @Inject
    private SnDao snDao;

    @Inject
    private AftersalesReturnsService aftersalesReturnsService;

    @Inject
    private WXRefundService wxRefundService;

    /**
     * @param pageNumber=1(第几页)
     * @param pageSize=20(每页条数，默认20)
     * @param orderProperty=createdDate(排序字段，默认createdDate)
     * @param orderDirection=ASC                            or  DESC (排序方式，默认asc)
     * @param storeId                                       店铺id
     * @return
     */
    @GetMapping(path = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(orderCountByMember.class)
    public ResponseEntity<?> list(Pageable pageable, Long storeId) {
        try {
            String orderPro = pageable.getOrderProperty();
            if (StringUtils.isEmpty(orderPro)) {
                pageable.setOrderProperty("createdDate");
                pageable.setOrderDirection(net.shopxx.Order.Direction.ASC);
            }
            Store store = storeService.find(storeId);
            Page<Order> orders = orderService.findPage(null, Order.Status.SHIPPED, store, null, null, null, null, null,
                    null, null, null, pageable);
            List<Order> orderList = orders.getContent();
            OrderCountByMember orderCount = null;
            Map<Long, OrderCountByMember> orderMap = new HashMap<Long, OrderCountByMember>();
            for (Order order : orderList) {
                Long memberId = order.getMember().getId();
                orderCount = orderMap.get(memberId);
                if (null == orderCount) {
                    orderCount = new OrderCountByMember();
                    orderCount.setMemberId(memberId);
                    orderCount.setName(order.getMember().getAttributeValue1());
                    orderCount.setOrderCount(1L);
                    orderCount.setTelphone(order.getPhone());
                    orderMap.put(memberId, orderCount);
                } else {
                    orderCount.setOrderCount(orderCount.getOrderCount() + 1);
                    orderMap.put(memberId, orderCount);
                }
            }
            Collection<OrderCountByMember> coll = orderMap.values();

            return ResponseEntity.ok(this.data(HttpStatus.OK, "成功！", new ArrayList<OrderCountByMember>(coll)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(this.data(HttpStatus.BAD_REQUEST, e.getMessage(), null));
        }
    }

    /**
     * 根据取货码查询订单信息
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-23 10:23
     */
    @GetMapping(path = "/getByCode", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(orderCountByMember.class)
    public Object getByCode(HttpServletRequest request, String code, Long storeId) {
        try {

            Store store = storeService.find(storeId);
            if (null == code || "".equals(code)) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "取货码不能为空！"));
            }
            // 查询订单
            Order order = orderService.findByCodeAndStatus(code, Order.Status.SHIPPED, store);
            if (null == order) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "取货码对应订单不存在,请检查该订单是否处于待取货！"));
            }
            Member member = order.getMember();
            // 数据返回组装
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getId());                            // 订单ID
            data.put("receiveName", member.getAttributeValue1());        // 收货人名称
            data.put("receiveMobile", member.getMobile());                // 收货人电话
            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem orderItem : order.getOrderItems()) {
                Map<String, Object> item = new HashMap<>();
                item.put("orderItemId", orderItem.getId());                // 明细ID
                item.put("orderItemSn", orderItem.getSn());                // 明细ID
                item.put("productImage", orderItem.getThumbnail());        // 商品图片
                item.put("productName", orderItem.getName());            // 商品名称
                item.put("productSpec", orderItem.getSpecifications());    // 购买规格
                item.put("buyCount", orderItem.getQuantity());            // 购买数量
                items.add(item);
            }
            data.put("items", items);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", data));
        } catch (Exception e) {
            e.printStackTrace();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 取货完成
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-23 16:04
     */
    @GetMapping(path = "/completeOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object completeOrder(HttpServletRequest request) {
        try {
            String orderId = request.getParameter("orderId").toString();
            Order order = orderService.find(Long.parseLong(orderId));
//            if (null == request.getParameterValues("itemsMap")) {
//                order.setStatus(Order.Status.COMPLETED);
//                order.setCompleteDate(new Date());
//                orderService.update(order);
//                return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "取货成功！"));
//            }
//            String[] itemsMap = (String[]) request.getParameterValues("itemsMap");
//            String regix = "";
//            for (String a : itemsMap) {
//                regix = regix + a;
//            }
//            regix = regix.replace("[", "").replace("]", "");
//            List<Map<String, Object>> itemsParam = stringToMap(regix);
//
//            JSONObject jsonObject = new JSONObject();
//            // 判断订单是否要退货
//            if (itemsParam.size() > 0) {
//                // 创建订单退货
//                OrderReturns orderReturns = new OrderReturns();
//                orderReturns.setCreatedDate(new Date());
//                orderReturns.setLastModifiedDate(new Date());
//                orderReturns.setVersion(1L);
//                orderReturns.setSn(order.getSn());
//                orderReturns.setOrder(order);
//                orderReturns.setArea(order.getArea());
//                // 创建售后单
//                AftersalesReturns aftersalesReturns = new AftersalesReturns();
//                aftersalesReturns.setCreatedDate(new Date());
//                aftersalesReturns.setLastModifiedDate(new Date());
//                aftersalesReturns.setVersion(1L);
//                aftersalesReturns.setReason("订单缺货");
//                aftersalesReturns.setStatus(AftersalesReturns.Status.PENDING);
//                aftersalesReturns.setMember(order.getMember());
//                aftersalesReturns.setStore(order.getStore());
//
//                // 订单退货明细项
//                List<OrderReturnsItem> orderReturnsItems = orderReturns.getOrderReturnsItems();
//                // 售后单明细项
//                List<AftersalesItem> aftersalesItems = aftersalesReturns.getAftersalesItems();
//                for (Map<String, Object> map : itemsParam) {
//                    // 创建订单退货明细
//                    String orderItemId = map.get("orderItemId").toString();
//                    orderItemId = orderItemId.substring(0, orderItemId.indexOf("."));
//                    String returnCount = map.get("returnCount").toString();
//                    returnCount = returnCount.substring(0, returnCount.indexOf("."));
//                    OrderItem orderItem = orderItemService.find(Long.parseLong(orderItemId));
//                    OrderReturnsItem orderReturnsItem = new OrderReturnsItem();
//                    orderReturnsItem.setName(orderItem.getName());
//                    orderReturnsItem.setOrderReturns(orderReturns);
//                    orderReturnsItem.setQuantity(Integer.parseInt(returnCount));
//                    orderReturnsItem.setSn(map.get("orderItemSn").toString());
//                    orderReturnsItem.setSpecifications(orderItem.getSpecifications());
//                    orderReturnsItems.add(orderReturnsItem);
//                    // 创建售后明细
//                    AftersalesItem aftersalesItem = new AftersalesItem();
//                    aftersalesItem.setCreatedDate(new Date());
//                    aftersalesItem.setLastModifiedDate(new Date());
//                    aftersalesItem.setVersion(1L);
//                    aftersalesItem.setQuantity(Integer.parseInt(returnCount));
//                    aftersalesItem.setAftersales(aftersalesReturns);
//                    aftersalesItem.setOrderItem(orderItem);
//                    aftersalesItems.add(aftersalesItem);
//                }
//                orderService.returns(order, orderReturns);
//                aftersalesReturnsService.persist(aftersalesReturns);
//                //店铺盘点退款
//                JSONObject jsonObject1 = wxRefundService.productCheckRefundMoney(request, orderId);
//                System.err.println(jsonObject1);
//
//
//            }
            order.setStatus(Order.Status.COMPLETED);
            order.setCompleteDate(new Date());
            orderService.update(order);

            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "取货成功！"));


        } catch (Exception e) {
            e.printStackTrace();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


}
