package net.shopxx.service;

import net.sf.json.JSONObject;
import net.shopxx.entity.OrderReturns;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface WXRefundService {

    /**
     * 店铺盘点退款（微信平台）
     * @param request
     * @param orderId
     * @return
     * @throws Exception
     */
    JSONObject productCheckRefundMoney(HttpServletRequest request, String orderId) throws Exception;



}
