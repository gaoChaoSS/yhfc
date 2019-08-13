package net.shopxx.service.impl;

import com.github.wxpay.sdk.WXPayUtil;
import net.sf.json.JSONObject;
import net.shopxx.dao.OrderDao;
import net.shopxx.dao.WXRefundDao;
import net.shopxx.entity.Order;
import net.shopxx.entity.OrderRefunds;
import net.shopxx.entity.OrderReturns;
import net.shopxx.entity.PluginConfig;
import net.shopxx.plugin.WeixinPublicPaymentPlugin;
import net.shopxx.service.OrderRefundsService;
import net.shopxx.service.WXRefundService;
import net.shopxx.util.PayCommonUtil;
import net.shopxx.util.RefundUtils;
import net.shopxx.util.StringUtils;
import net.shopxx.util.XMLUtil;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class WXRefundServiceImpl implements WXRefundService {

    @Autowired
    private WXRefundDao wxRefundDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private RefundUtils refundUtils;


    @Override
    public JSONObject productCheckRefundMoney(HttpServletRequest request, String orderId)  {
        Order order = orderDao.find(Long.valueOf(orderId));

        //根据订单Id   查询（退货项）总退款金额
        BigDecimal refundMoney = wxRefundDao.queryAllRefundMoneyByOrderId(orderId);
        //退款流水号 (时间戳后8位+5位随机数)
        String refundNum = StringUtils.getId();
        //将退款流水号 与 退货绑定（orderReturns表）
        wxRefundDao.updateRefundNum(order.getSn(),refundNum);

        return refundUtils.refundByOrderId(request,orderId,order.getSn(),refundNum,order.getAmountPaid(),refundMoney,"商品缺货");

    }


}
