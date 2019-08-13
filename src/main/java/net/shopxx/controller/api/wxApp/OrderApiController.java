package net.shopxx.controller.api.wxApp;

import com.fasterxml.jackson.annotation.JsonView;
import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.Results;
import net.shopxx.controller.admin.BaseController;
import net.shopxx.entity.*;
import net.shopxx.plugin.WeixinPublicPaymentPlugin;
import net.shopxx.security.CurrentCart;
import net.shopxx.security.CurrentUser;
import net.shopxx.service.*;
import net.shopxx.util.DateUtils;
import net.shopxx.util.WXPayOtherUtil;
import net.shopxx.util.WxAppPayUtil;
import net.shopxx.util.WxPayUtil;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信订单相关接口
 *
 * @Auther: Demaxiya
 * @Create: 2019-07-09 14:22
 */
@Controller("orderApiController")
@RequestMapping("/api/wxapp/order")
public class OrderApiController extends BaseController {

    private final static Logger logger = LoggerFactory.getLogger(OrderApiController.class);


    @Inject
    private StoreService storeService;

    @Inject
    private ProductService productService;

    @Inject
    private OrderService orderService;

    @Inject
    private SkuService skuService;

    @Inject
    private CartService cartService;

    @Inject
    private CouponCodeService couponCodeService;

    @Inject
    private ReceiverService receiverService;

    @Inject
    private PaymentMethodService paymentMethodService;
    @Inject
    private ShippingMethodService shippingMethodService;

    @Inject
    private WeixinPublicPaymentPlugin weixinPublicPaymentPlugin;

    @Inject
    private MemberService memberService;

    @Inject
    private CartItemService cartItemService;

    @Value("${payment.homeUrl}")
    private String homeUrl;


    /**
     * 商品加入购物车
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-09 16:57
     * @skuId 商品规格对应sku码
     * @quantity 购买数量
     * @currentCart 当前购物车
     */
    @GetMapping(path = "/addToCart", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object addToCart(String skuId, Integer quantity, HttpServletRequest request) {
        String memberId = "";
        try {
            // 获取当前登录用户
            Member member = this.getCurrentWxInfo(request);
            memberId = member.getId() + "";
            // 判断当前门店是否已停用
            Store store = member.getBuyStore();
            if (!store.getIsEnabled()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前店铺已停用，请重新选择！"));
            }
            if (quantity == null || quantity < 1) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购买数量不合法！"));
            }
            // 查询sku信息
            Sku sku = skuService.find(Long.parseLong(skuId));
            if (sku == null) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "添加的商品SKU为空"));
            }
            if (!Product.Type.GENERAL.equals(sku.getType())) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前列表商品与门店商品类型不一致！"));
            }
            if (!sku.getIsActive()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前商品SKU码无效！"));
            }
            if (!sku.getIsMarketable()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前商品未上架！"));
            }
//            if (sku.getProduct().getStore().hasExpired()) {
//                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前店铺已过期！"));
//            }

            // 查询当前用户是否有购物车
            Cart currentCart = member.getCart();
            if (currentCart == null) {
                currentCart = cartService.createWx(member);
            }
            // 判断购物车是否存在该Sku
            int cartItemSize = 1;
            int skuQuantity = quantity;
            if (currentCart != null) {
                if (currentCart.contains(sku)) {
                    CartItem cartItem = currentCart.getCartItem(sku);
                    cartItemSize = currentCart.size();
                    skuQuantity = cartItem.getQuantity() + quantity;
                } else {
                    cartItemSize = currentCart.size() + 1;
                    skuQuantity = quantity;
                }
            }
            if (Cart.MAX_CART_ITEM_SIZE != null && cartItemSize > Cart.MAX_CART_ITEM_SIZE) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车超过最大数量！" + Cart.MAX_CART_ITEM_SIZE));
            }
            if (CartItem.MAX_QUANTITY != null && skuQuantity > CartItem.MAX_QUANTITY) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "商品超过最大数量！" + CartItem.MAX_QUANTITY));
            }
            if (skuQuantity > sku.getAvailableStock()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前商品超过库存数量！"));
            }
            // 购物车添加SKU
            if (currentCart.getCartItems().contains(sku)) {
                for (CartItem cartItem : currentCart.getCartItems()) {
                    // 判断购物车是否存在该商品 存在就加1
                    if (cartItem.getId().compareTo(sku.getId()) == 0) {
                        cartItem.setQuantity(cartItem.getQuantity() + quantity);
                    }
                }
            } else {
                cartService.add(currentCart, sku, quantity);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("cartId", currentCart.getId());
            result.put("cartTag", currentCart.getTag());
            result.put("cartKey", currentCart.getKey());
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功加入购物车！", result));
        } catch (Exception e) {
            logger.error("商品加入购物车失败, skuid,{},memberId,{}", skuId, memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 移除购物车
     * 单商品移除
     *
     * @skuId
     * @currentCart
     * @Auther: Demaxiya
     * @Create: 2019-07-09 17:38
     */
    @GetMapping(path = "/removeToCart", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object removeToCart(Long skuId, Integer quantity, HttpServletRequest request) {

        String memberId = "";

        try {
            Map<String, Object> data = new HashMap<>();
            Sku sku = skuService.find(skuId);
            // 获取当前用户的购物车
            Member member = this.getCurrentWxInfo(request);
            memberId = member.getId() + "";
            Cart currentCart = member.getCart();
            if (sku == null) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前sku不存在！"));
            }
            if (currentCart == null || currentCart.isEmpty() || currentCart.getCartItems().size() < 0) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前购物车不存在！"));
            }
            if (!currentCart.contains(sku)) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前sku在购物车不存在！"));
            }
            //cartService.remove(currentCart, sku);
            for (CartItem item : currentCart.getCartItems()) {
                if (item.getSku().getId().compareTo(skuId) == 0) {
                    if (item.getQuantity() - quantity == 0) {
                        return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "数量不允许为0！"));

                    } else {
                        item.setQuantity(item.getQuantity() - quantity);
                        cartItemService.update(item);
                    }

                }
            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "移除成功！"));
        } catch (Exception e) {
            logger.error("单商品移除失败, skuid,{},memberId,{}", skuId, memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 删除购物车商品
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-09 17:54
     */
    @GetMapping(path = "/clearSkuCart", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object clearSkuCart(String skuId, HttpServletRequest request) {
        String memberId = "";
        try {
            // 获取当前购物车
            Member member = this.getCurrentWxInfo(request);
            memberId = member.getId() + "";
            Cart cart = member.getCart();
            Sku sku = skuService.find(Long.parseLong(skuId));
            // 删除购物车商品
            cartService.remove(cart, sku);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "删除成功！"));
        } catch (Exception e) {
            logger.error("删除购物车商品失败, skuid,{},memberId,{}", skuId, memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


    /**
     * 生成订单
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-09 14:33
     */
    @GetMapping(path = "/createOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object createOrder(Long skuId, Integer quantity, String cartTag, String storeId,
                              BigDecimal balance, HttpServletRequest request) {
        String memberId = "";
        try {

            // 获取当前用户
            Member currentUser = this.getCurrentWxInfo(request);
            memberId = currentUser.getId() + "";
            Cart cart = currentUser.getCart();
            Store store = storeService.find(Long.parseLong(storeId));
            // 判断店铺是否启用
            if (!store.getIsEnabled()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前店铺已停用，请重新选择！"));
            }
            Order.Type orderType;
            if (skuId != null) {
                Sku sku = skuService.find(skuId);
                if (sku == null) {
                    return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "sku为空！"));
                }
                if (Product.Type.GIFT.equals(sku.getType())) {
                    return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "商品类型不一致！"));
                }
                if (quantity == null || quantity < 1) {
                    return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购买数量不合法！"));
                }

                cart = generateCart(currentUser, sku, quantity);

                switch (sku.getType()) {
                    case GENERAL:
                        orderType = Order.Type.GENERAL;
                        break;
                    case EXCHANGE:
                        orderType = Order.Type.EXCHANGE;
                        break;
                    default:
                        orderType = null;
                        break;
                }
            } else {
                orderType = Order.Type.GENERAL;
            }
            if (cart == null || cart.isEmpty()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车为空！"));
            }
            if (cartTag != null && !StringUtils.equals(cart.getTag(), cartTag)) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车标签错误！"));
            }
            if (cart.hasNotActive()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车存在无效商品！"));
            }
            if (cart.hasNotMarketable()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车存在已下架商品！"));
            }
            if (cart.hasLowStock()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车存在库存不足商品！"));
            }
            if (cart.hasExpiredProduct()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "购物车存在过期店铺商品！"));
            }
            if (orderType == null) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "订单类型为空！"));
            }
            if (balance != null && balance.compareTo(BigDecimal.ZERO) < 0) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "余额为负数！"));
            }
            if (balance != null && balance.compareTo(currentUser.getAvailableBalance()) > 0) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "余额不足！"));
            }
            // 默认支付方式和物流配送方式
            PaymentMethod paymentMethod = paymentMethodService.find(Long.parseLong("1"));
            ShippingMethod shippingMethod = shippingMethodService.find(Long.parseLong("1"));
            // 判断购物商品数量为0的情况
            for (CartItem cartItem : cart.getCartItems()) {
                if (cartItem.getQuantity() <= 0) {
                    return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, cartItem.getSku().getProduct().getName() + "数量不能为0"));
                }
            }
            //
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = cartItem.getSku().getProduct();
                Set<Product> products = store.getProducts();
                if (!products.contains(product)) {
                    return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, cartItem.getSku().getProduct().getName() + "在当前店铺不存在！"));
                }
            }
            // 判断商品是否限购
            for (CartItem cartItem : cart.getCartItems()) {
                Product product = cartItem.getSku().getProduct();
                Sku sku = cartItem.getSku();
                Integer buyCount = cartItem.getQuantity();// 当前购物车的购买数量
                Integer maxBuyCount = product.getMaxBuy();// 商品限购数量
                if (maxBuyCount > 0) {
                    Integer skuBuyCount = orderService.findSkuBuyCount(currentUser, sku);
                    if (buyCount + skuBuyCount > maxBuyCount) {
                        return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, product.getName() + "已经超过限购数," + "份/人!"));
                    }
                }
            }
            // 生成订单
            List<Order> orders = orderService.create(orderType, store, cart, null, paymentMethod, shippingMethod, null, null, balance, "聚点好货订单");

            // 数据返回
            Order createOrder = orders.get(0);
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", createOrder.getId());    // 订单ID
//            data.put("orderSn",createOrder.getSn());    // 订单编号
//            List<Map<String,Object>> pros=new ArrayList<>();
//            for(OrderItem item:createOrder.getOrderItems()){
//                Map<String,Object> pro=new HashMap<>();
//                pro.put("productPrice",item.getSku().getPrice());                           // 商品价格
//                pro.put("productName",item.getSku().getProduct().getName());                // 商品名称
//                pro.put("productSpecs",item.getSpecifications());                           // 规格
//                pro.put("productImages",item.getSku().getProduct().getProductImages());     // 图片
//                pro.put("productCount",item.getQuantity());                                 // 购买数量
//                pros.add(pro);
//            }
//            data.put("orderItems",pros);
//            data.put("orderPrice",createOrder.getAmount()); // 订单额
//            data.put("storeName",createOrder.getStore().getName());         // 门店名称
//            data.put("storeAddress",createOrder.getStore().getAddress());   // 门店地址
//            data.put("mobile",createOrder.getMember().getMobile());         // 收件人电话
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "订单创建成功！", data));
        } catch (Exception e) {
            logger.error("生成订单失败, skuid,{},memberId,{}，storeId,{}", skuId, memberId, storeId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }


    /**
     * 生成购物车
     *
     * @param member   会员
     * @param sku      SKU
     * @param quantity 数量
     * @return 购物车
     */
    public Cart generateCart(Member member, Sku sku, Integer quantity) {
        Assert.notNull(member, "[Assertion failed] - member is required; it must not be null");
        Assert.notNull(sku, "[Assertion failed] - sku is required; it must not be null");
        Assert.state(!Product.Type.GIFT.equals(sku.getType()), "[Assertion failed] - sku type can't be GIFT");
        Assert.notNull(quantity, "[Assertion failed] - quantity is required; it must not be null");
        Assert.state(quantity > 0, "[Assertion failed] - quantity must be greater than 0");

        Cart cart = new Cart();
        Set<CartItem> cartItems = new HashSet<>();
        CartItem cartItem = new CartItem();
        cartItem.setSku(sku);
        cartItem.setQuantity(quantity);
        cartItems.add(cartItem);
        cartItem.setCart(cart);
        cart.setMember(member);
        cart.setCartItems(cartItems);
        return cart;
    }

    /**
     * 订单支付
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-09 19:51
     */
    @GetMapping(path = "/payOrder", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object payOrder(String orderId, HttpServletRequest request) {
        String memberId = "";
        try {
            PluginConfig pluginConfig = weixinPublicPaymentPlugin.getPluginConfig();
            Member member = this.getCurrentWxInfo(request);

            memberId = member.getId() + "";

            // 判断店铺状态
            Store store = member.getBuyStore();
            if (!store.getIsEnabled()) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "当前店铺已停用，请重新选择！"));
            }
            // 获取IP
            String remoteAddr = request.getRemoteAddr();
            String realIp = request.getHeader("X-Real-IP");
            String ip;
            if (realIp == null || realIp.length() < 1) {
                ip = remoteAddr;
            } else {
                ip = realIp;
            }
            Order order = orderService.find(Long.parseLong(orderId));
            if (!order.getStatus().equals(Order.Status.PENDING_PAYMENT)) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, "只能支付等待付款的订单！"));
            }
            String totalFee = order.getAmountPaid().multiply(new BigDecimal("100")).intValue() + "";
            String desc = order.getSn();

            // 判断商品是否限购
            for (OrderItem orderItem : order.getOrderItems()) {
                Sku sku = orderItem.getSku();
                Product product = sku.getProduct();
                Integer buyCount = orderItem.getQuantity();// 当前订单的购买数量
                Integer maxBuyCount = product.getMaxBuy();// 商品限购数量
                if (maxBuyCount > 0) {
                    Integer skuBuyCount = orderService.findSkuBuyCount(member, sku);
                    if (buyCount + skuBuyCount > maxBuyCount) {
                        return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, product.getName() + "已经超过限购数," + "份/人!"));
                    }
                }
            }

            //String homeUrl = "https://jdsq360.goho.co";//本地
            //String homeUrl="https://jdsq360.goho.co";//本地
            //String homeUrl="https://jdsq360.imdo.co";//测试机
            //String homeUrl="https://haohuo.jdsq360.com";//正式环境
            Map<String, Object> data = new WxAppPayUtil().payment(order.getSn(), totalFee, desc, ip, member.getWeixinOpenId(), "/api/wxapp/order/payOrderNodify",
                    pluginConfig.getAttribute("appId"), pluginConfig.getAttribute("mchId"), "2", homeUrl, pluginConfig.getAttribute("apiKey"), member.getId());

            String param = data.get("param").toString();
            // 保存支付记录
            orderService.savePayLog(0, param, new BigDecimal(totalFee), 0, order.getSn(), order.getMember().getId(), "");
            if (data.get("return_code").toString().equals("FAIL")) {
                return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, data.get("return_msg").toString()));
            }
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "支付成功！", data));
        } catch (Exception e) {
            logger.error("订单支付失败, orderId,{},memberId,{}", orderId, memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 支付回调
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-09 20:37
     */
    @PostMapping("/payOrderNodify")
    //@PostMapping(value = "/payOrderNodify")
    public void payOrderNodify(HttpServletRequest request, HttpServletResponse response) {

        String transaction_id = "";
        String orderNo = "";

        try {
            Map<String, String> resultMap = new HashMap<>();
            PluginConfig pluginConfig = weixinPublicPaymentPlugin.getPluginConfig();
            // -------------------- 获取参数 ------------------
            Map<String, String> paraMap = WXPayOtherUtil.getWeixinResult(request);
            String return_code = paraMap.get("return_code");                            // 通信标识，非交易标识
            String return_msg = paraMap.get("return_msg");                              // 返回信息

            // 通信标识 - 失败
            if (return_code.equals("FAIL")) {
                // 状态或者业务状态失败
                // 返回XML
                resultMap.put("return_code", return_code);
                resultMap.put("return_msg", return_msg);
                //return WxPayUtil.mapToXml(resultMap);
            }

            logger.info("支付回调：{}", return_msg);

            String result_code = paraMap.get("result_code");                            // 回调业务结果状态
            orderNo = paraMap.get("out_trade_no");                               // 支付订单号
            String time_end = paraMap.get("time_end");                                  // 支付完成时间
            transaction_id = paraMap.get("transaction_id");                      // 微信支付订单号
            // 查询对应订单
            Order order = orderService.findBySn(orderNo);

            if (result_code.equals("FAIL")) {
                // 支付失败
                orderService.savePayLog(0, paraMap.toString(), new BigDecimal("0.00"), 0, orderNo, order.getMember().getId(), transaction_id);
                this.orderService.fail(order);
            } else if (result_code.equals("SUCCESS")) {
                // -------------- 查询对应订单支付情况 --------------
                Map<String, Object> orderQueryMap = WxAppPayUtil.orderquery(orderNo, pluginConfig.getAttribute("appId"), pluginConfig.getAttribute("mchId"), pluginConfig.getAttribute("apiKey"));
                if (orderQueryMap.get("orderInfo") != null) {
                    Map<String, String> orderInfoMap = (Map<String, String>) orderQueryMap.get("orderInfo");
                    if (orderInfoMap.get("return_code").equals("SUCCESS") &&
                            orderInfoMap.get("result_code").equals("SUCCESS")) {

                        if (orderInfoMap.get("trade_state").equals("SUCCESS") ||
                                orderInfoMap.get("trade_state").equals("REFUND")) {
                            // 支付成功 订单状态改为待发货
                            order.setStatus(Order.Status.PENDING_SHIPMENT);
                            order.setOutOrderCode(Order.getRandomCode());   // 生成取货码
                            //order.setLastModifiedDate(new Date());
                            order.setPayTime(new Date());
                            //order.setAmountPaid(order.getAmount());
                            this.orderService.update(order);
                            orderService.savePayLog(0, paraMap.toString(), new BigDecimal("0.00"), 1, orderNo, order.getMember().getId(), transaction_id);
                        } else if (orderInfoMap.get("trade_state").equals("NOTPAY")) {
                            // 未支付
                            this.orderService.fail(order);

                        } else if (orderInfoMap.get("trade_state").equals("CLOSED")) {
                            // 已关闭
                            this.orderService.fail(order);

                        } else if (orderInfoMap.get("trade_state").equals("PAYERROR")) {
                            // 支付失败
                            this.orderService.fail(order);
                        }
                    }
                }
            }
            resultMap.put("return_code", return_code);
            resultMap.put("return_msg", return_msg);
            PrintWriter out = response.getWriter();
            out.println("SUCCESS");
            out.flush();
            out.close();
        } catch (Exception e) {
            logger.error("订单支付回调失败, orderId,{},transaction_id,{}", orderNo, transaction_id, e);
        }
    }

    /**
     * 查询订单详情
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-10 11:13
     */
    @GetMapping(path = "/getOrderDetails", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object getOrderDetails(HttpServletRequest request, String orderId) {
        try {
            Order order = orderService.find(Long.parseLong(orderId));
            // 判断订单时间是否超过店铺截单时间
            Store store = order.getStore();
            Date sendDate = new Date();

            Boolean isCanRefund = false;
            // System.out.println(order.getStatus());

            if (order.getStatus().compareTo(Order.Status.PENDING_SHIPMENT) == 0) {
                //System.out.println("进来了");
                //判断是否可以退款
                if (order.getPayTime() != null) {
                    //System.out.println("进来了2");
                    if (order.getPayTime().getTime() < DateUtils.getOrderLimitDate(0, this.order_limit_time).getTime()) {
                        //System.out.println("进来了3");
                        isCanRefund = true;
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
                    long timeTF = calendar.getTime().getTime();
                    if (order.getPayTime().getTime() >= DateUtils.getOrderLimitDate(0, this.order_limit_time).getTime()
                            && order.getPayTime().getTime() <= timeTF) {

                        isCanRefund = true;
                    }


                }
            }


            if (store != null) {
                String limit = (new SimpleDateFormat("HH:mm:ss").format(store.getLimitTime())).replace(":", "");
                if (order.getPayTime() != null) {
                    String orderDate = (new SimpleDateFormat("HH:mm:ss").format(order.getPayTime())).replace(":", "");
                    if (Integer.parseInt(orderDate) >= Integer.parseInt(limit)) {
                        sendDate = addHours(order.getCreatedDate(), 48);
                    } else {
                        sendDate = addHours(order.getCreatedDate(), 24);
                    }
                }
            }
            Map<String, Object> data = new HashMap<>();
            data.put("sendDate", new SimpleDateFormat("yyyy-MM-dd").format(sendDate));   // 订单送货时间
            data.put("isCanRefund", isCanRefund);   // 订单是否可以退款
            data.put("orderId", order.getId());    // 订单ID
            data.put("orderSn", order.getSn());    // 订单编号
            List<Map<String, Object>> pros = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                Map<String, Object> pro = new HashMap<>();
                pro.put("productPrice", item.getPrice());                                    // 商品价格
                pro.put("productName", item.getName());                                      // 商品名称
                pro.put("productSpecs", item.getSpecifications());                           // 规格
                pro.put("productImages", item.getThumbnail());                               // 图片
                pro.put("productCount", item.getQuantity());                                 // 购买数量
                pros.add(pro);
            }

            data.put("orderItems", pros);
            data.put("orderPrice", order.getAmount()); // 订单额
            data.put("storeName", order.getStore().getName());         // 门店名称
            data.put("storeAddress", order.getStore().getAddress());   // 门店地址
            data.put("mobile", order.getMember().getMobile());         // 收件人电话
            data.put("orderCreateTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getCreatedDate()));
            data.put("businessName", order.getStore().getBusiness().getUsername());  // 店长
            data.put("businessMobile", order.getStore().getBusiness().getMobile());  // 电话
            data.put("orderStatus", order.getStatus());                              // 订单状态
            data.put("completeDate", null == order.getCompleteDate() ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getCompleteDate()));    // 订单完成时间


            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", data));
        } catch (Exception e) {
            logger.error("查询订单详情失败, orderId,{}", orderId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 订单列表
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-10 11:36
     */
    @GetMapping(path = "/orderList", produces = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(BaseEntity.BaseView.class)
    public Object orderList(HttpServletRequest request, Pageable pageable, Order.Status status) {

        String memberId = "";
        try {
            Member member = this.getCurrentWxInfo(request);
            memberId = member.getId() + "";
            Order.Status sta = null;
            if (status.compareTo(Order.Status.SELECT_ALL) != 0) {
                sta = status;
            }
            Page<Order> page = orderService.findPage(null, sta, null, member, null, null, null, null, null, null, null, pageable);
            List<Order> orderList = page.getContent();
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "成功！", orderList));
        } catch (Exception e) {
            logger.error("查询订单列表失败,memberId,{}", memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 查询购物车列表
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-12 14:50
     */
    @GetMapping(path = "/cartList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object cartList(HttpServletRequest request) {
        String memberId = "";
        try {
            // 获取当前用户
            Member member = this.getCurrentWxInfo(request);
            // 返回数据组装
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> data = new ArrayList<>();
            if (member == null || null == member.getCart()) {
                result.put("data", data);
                return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "购物车为空！", result));
            }

            memberId = member.getId() + "";
            Cart cart = member.getCart();
            BigDecimal total = new BigDecimal("0.00");
            List<CartItem> copy = new ArrayList<>();
            for (CartItem cartItem : cart.getCartItems()) {
                copy.add(cartItem);
            }

            for (CartItem cartItem : copy) {

                System.out.println("Sku:" + cartItem.getSku());

                Map<String, Object> item = new HashMap<>();
                item.put("productName", cartItem.getSku().getProduct().getName());                           // 商品名称
                item.put("skuId", cartItem.getSku().getId());                                                // skuId
                item.put("productImages", cartItem.getSku().getProduct().getProductImages());                // 商品图片
                item.put("price", cartItem.getSku().getPrice());                                             // 销售价
                item.put("productSpec", cartItem.getSku().getSpecificationValues());                         // 商品规格
                item.put("productCount", cartItem.getQuantity());                                            // 购买数量
                item.put("createDate", cartItem.getCreatedDate());                                           // 创建时间
                item.put("marketPrice", cartItem.getSku().getMarketPrice());                    // 市场价
                item.put("maxBuyCount", cartItem.getSku().getProduct().getMaxBuy());                         // 最大购买数量
                total = total.add(cartItem.getSku().getPrice().multiply(new BigDecimal(cartItem.getQuantity() + "")));
                data.add(item);
            }
            Collections.sort(data, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Date name1 = (Date) (o1.get("createDate"));
                    Date name2 = (Date) (o2.get("createDate"));
                    return name1.compareTo(name2);
                }
            });
            result.put("data", data);
            result.put("total", total);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", result));
        } catch (Exception e) {
            logger.error("查询购物车列表失败,memberId,{}", memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    /**
     * 给指定时间加上小时数
     *
     * @param date  指定日期
     * @param hours 多少小时
     */
    public static Date addHours(Date date, int hours) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.HOUR, hours);// 24小时制
        Date newDate = cal.getTime();
        return newDate;
    }

    /**
     * 提货列表
     *
     * @Auther: Demaxiya
     * @Create: 2019-07-13 16:41
     */
    @GetMapping(path = "/bringList", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object bringList(HttpServletRequest request) {
        String memberId = "";
        try {
            // 获取当前用户
            Member member = this.getCurrentWxInfo(request);
            memberId = member.getId() + "";
            List<Map<String, Object>> result = new ArrayList<>();
            // 查询该用户的所有订单
            List<Order> orders = orderService.findByStatus(Order.Status.SHIPPED, member);
            if (orders.size() > 0) {
                for (Order order : orders) {
                    Store store = order.getStore();
                    Date sendDate = new Date();
                    if (store != null) {
                        String limit = (new SimpleDateFormat("HH:mm:ss").format(store.getLimitTime())).replace(":", "");
                        String orderDate = (new SimpleDateFormat("HH:mm:ss").format(order.getPayTime())).replace(":", "");

                        if (Integer.parseInt(orderDate) >= Integer.parseInt(limit)) {
                            sendDate = addHours(order.getCreatedDate(), 48);
                        } else {
                            sendDate = addHours(order.getCreatedDate(), 24);
                        }
                    }
                    Map<String, Object> item = new HashMap<>();
                    item.put("outOrderCode", order.getOutOrderCode());                                   // 提货码
                    item.put("sendDate", new SimpleDateFormat("yyyy-MM-dd").format(sendDate));    // 送货时间
                    item.put("storeName", store.getName());                                              // 店铺名称
                    item.put("openTime", store.getBusiness().getAttributeValue2());                      // 营业时间
                    item.put("lastModifyDate", order.getLastModifiedDate());
                    item.put("storeAddress", store.getAddress());                                        // 店铺地址
                    List<Map<String, Object>> items = new ArrayList<>();
                    for (OrderItem orderItem : order.getOrderItems()) {
                        Map<String, Object> orderItems = new HashMap<>();
                        orderItems.put("productImg", orderItem.getThumbnail());                         // 商品图
                        orderItems.put("productName", orderItem.getName());                             // 商品名称
                        orderItems.put("productSpec", orderItem.getSpecifications());                   // 商品规格
                        orderItems.put("buyCount", orderItem.getQuantity());                            // 商品购买数量
                        items.add(orderItems);
                    }
                    item.put("orderItems", items);                                                      // 订单明细
                    result.add(item);
                }
            }
            Collections.sort(result, new Comparator<Map<String, Object>>() {
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    Date name1 = (Date) (o1.get("lastModifyDate"));
                    Date name2 = (Date) (o2.get("lastModifyDate"));
                    return name2.compareTo(name1);
                }
            });
            return Results.status(HttpStatus.OK, this.data(HttpStatus.OK, "查询成功！", result));
        } catch (Exception e) {
            logger.error("查询提货列表失败,memberId,{}", memberId, e);
            return Results.status(HttpStatus.OK, this.data(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }
}
