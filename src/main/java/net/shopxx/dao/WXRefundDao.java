package net.shopxx.dao;


import java.math.BigDecimal;

public interface WXRefundDao {
    BigDecimal queryAllRefundMoneyByOrderId(String orderId);

    /**
     * 给退货表添加退款流水号
     * @param sn
     * @param refundNum
     */
    void updateRefundNum(String sn, String refundNum);
}
