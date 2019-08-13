package net.shopxx.util;

/**
 * 微信支付错误码
 *
 * @Auther: Huanghao
 * @Create: 2019/2/20 13:32
 */
public class WXPayErrorCode {

    public enum WxPayApiType {
        UNIFIEDORDER,       // 统一下单
        ORDERQUERY,         // 订单查询
        CLOSEORDER,         // 关闭订单
        REFUND,             // 退款申请
        REFUNDQUERY,        // 退款查询
        TRANSFERS           // 企业付款到零钱
    }

    /**
     * 统一下单错误码
     */
    public final static String[] UNIFIEDORDER_ERROR_CODE = new String[]{
            "INVALID_REQUEST",
            "NOAUTH",
            "NOTENOUGH",
            "ORDERPAID",
            "ORDERCLOSED",
            "SYSTEMERROR",
            "APPID_NOT_EXIST",
            "MCHID_NOT_EXIST",
            "APPID_MCHID_NOT_MATCH",
            "LACK_PARAMS",
            "OUT_TRADE_NO_USED",
            "SIGNERROR",
            "XML_FORMAT_ERROR",
            "REQUIRE_POST_METHOD",
            "POST_DATA_EMPTY",
            "NOT_UTF8"
    };
    /**
     * 统一下单错误码 - 描述
     */
    public final static String[] UNIFIEDORDER_ERROR_DESC = new String[]{
            "参数错误",
            "商户无此接口权限",
            "余额不足",
            "商户订单已支付",
            "订单已关闭",
            "系统异常，请用相同参数重新调用",
            "APPID不存在",
            "MCHID不存在",
            "appid和mch_id不匹配",
            "缺少参数",
            "商户订单号重复",
            "签名错误",
            "XML格式错误",
            "请使用post方法",
            "post数据为空",
            "编码格式错误"
    };

    /**
     * 查询订单错误码
     */
    public final static String[] ORDERQUERY_ERROR_CODE = new String[]{
            "ORDERNOTEXIST",
            "SYSTEMERROR"
    };

    /**
     * 查询订单错误码 - 描述
     */
    public final static String[] ORDERQUERY_ERROR_DESC = new String[]{
            "此交易订单号不存在",
            "系统异常，请再调用发起查询"
    };

    /**
     * 订单关闭错误码
     */
    public final static String[] CLOSEORDER_ERROR_CODE = new String[]{
            "ORDERPAID",
            "SYSTEMERROR",
            "ORDERCLOSED",
            "SIGNERROR",
            "REQUIRE_POST_METHOD",
            "XML_FORMAT_ERROR"
    };

    /**
     * 订单关闭错误码 - 描述
     */
    public final static String[] CLOSEORDER_ERROR_DESC = new String[]{
            "订单已支付",
            "系统异常，请重新调用该API",
            "订单已关闭",
            "签名错误",
            "请使用post方法",
            "XML格式错误"
    };

    /**
     * 退款申请错误码
     */
    public final static String[] REFUND_ERROR_CODE = new String[]{
            "SYSTEMERROR",
            "BIZERR_NEED_RETRY",
            "TRADE_OVERDUE",
            "ERROR",
            "USER_ACCOUNT_ABNORMAL",
            "INVALID_REQ_TOO_MUCH",
            "NOTENOUGH",
            "INVALID_TRANSACTIONID",
            "PARAM_ERROR",
            "APPID_NOT_EXIST",
            "MCHID_NOT_EXIST",
            "REQUIRE_POST_METHOD",
            "SIGNERROR",
            "XML_FORMAT_ERROR",
            "FREQUENCY_LIMITED"
    };

    /**
     * 退款申请错误码 - 描述
     */
    public final static String[] REFUND_ERROR_DESC = new String[]{
            "请不要更换商户退款单号，请使用相同参数再次调用API。",
            "退款业务流程错误，需要商户触发重试来解决",
            "订单已经超过可退款的最大期限(支付后一年内可退款)",
            "申请退款业务发生错误",
            "退款申请失败，商户可自行处理退款。",
            "请检查业务是否正常，确认业务正常后请在1分钟后再来重试",
            "商户可用退款余额不足",
            "请求参数错误，检查原交易号是否存在或发起支付交易接口返回失败",
            "请求参数未按指引进行填写",
            "APPID不存在",
            "MCHID不存在",
            "请使用post方法",
            "签名错误",
            "XML格式错误",
            "2个月之前的订单申请退款有频率限制"
    };

    /**
     * 退款查询错误码
     */
    public final static String[] REFUNDQUERY_ERROR_CODE = new String[]{
            "SYSTEMERROR",
            "REFUNDNOTEXIST",
            "INVALID_TRANSACTIONID",
            "PARAM_ERROR",
            "APPID_NOT_EXIST",
            "MCHID_NOT_EXIST",
            "REQUIRE_POST_METHOD",
            "SIGNERROR",
            "XML_FORMAT_ERROR"
    };

    /**
     * 退款查询错误码 - 描述
     */
    public final static String[] REFUNDQUERY_ERROR_DESC = new String[]{
            "请尝试再次掉调用API。",
            "订单号错误或订单状态不正确",
            "检查原交易号是否存在或发起支付交易接口返回失败",
            "请求参数未按指引进行填写",
            "APPID不存在",
            "MCHID不存在",
            "请使用post方法",
            "签名错误",
            "XML格式错误"
    };

    /**
     * 企业付款到零钱错误码
     */
    public final static String[] TRANSFERS_ERROR_CODE = new String[]{
            "NO_AUTH",
            "AMOUNT_LIMIT",
            "PARAM_ERROR",
            "OPENID_ERROR",
            "SEND_FAILED",
            "NOTENOUGH",
            "SYSTEMERROR",
            "SIGN_ERROR",
            "XML_ERROR",
            "FATAL_ERROR",
            "FREQ_LIMIT",
            "MONEY_LIMIT",
            "CA_ERROR",
            "V2_ACCOUNT_SIMPLE_BAN",
            "PARAM_IS_NOT_UTF8",
            "SENDNUM_LIMIT",
            "RECV_ACCOUNT_NOT_ALLOWED",
            "PAY_CHANNEL_NOT_ALLOWED"
    };

    /**
     * 企业付款到零钱错误码 - 描述
     */
    public final static String[] TRANSFERS_ERROR_DESC = new String[]{
            "没有该接口权限",
            "最低付款金额为1元，最高10万元",
            "参数错误",
            "Openid格式错误或者不属于商家公众账号",
            "请查单确认付款结果，以查单结果为准",
            "付款账号余额不足",
            "微信内部接口调用发生错误",
            "校验签名错误",
            "XML格式错误",
            "两次请求商户单号一样，但是参数不一致",
            "接口请求频率超时接口限制",
            "已经达到今日付款总额上限/已达到付款给此用户额度上限",
            "请求没带商户API证书或者带上了错误的商户API证书",
            "用户微信支付账户未知名，无法付款",
            "请求参数中包含非utf8编码字符",
            "该用户今日付款次数超过限制,如有需要请登录微信支付商户平台更改API安全配置",
            "收款账户不在收款账户列表",
            "本商户号未配置API发起能力"
    };

    /**
     * 根据错误码获取指定描述信息
     *
     * @param wxPayApiType API接口类型
     * @param errorCode    错误码
     * @return
     */
    public static String getErrorInfo(WxPayApiType wxPayApiType, String errorCode) {
        String[] errorCodeArr = null;
        String[] errorDescArr = null;
        if (WxPayApiType.UNIFIEDORDER.equals(wxPayApiType)) {
            // 统一下单错误码
            errorCodeArr = UNIFIEDORDER_ERROR_CODE;
            errorDescArr = UNIFIEDORDER_ERROR_DESC;
        } else if (WxPayApiType.ORDERQUERY.equals(wxPayApiType)) {
            // 订单查询错误码
            errorCodeArr = ORDERQUERY_ERROR_CODE;
            errorDescArr = ORDERQUERY_ERROR_DESC;
        } else if (WxPayApiType.CLOSEORDER.equals(wxPayApiType)) {
            // 关闭订单错误码
            errorCodeArr = CLOSEORDER_ERROR_CODE;
            errorDescArr = CLOSEORDER_ERROR_DESC;
        } else if (WxPayApiType.REFUND.equals(wxPayApiType)) {
            // 退款申请
            errorCodeArr = REFUND_ERROR_CODE;
            errorDescArr = REFUND_ERROR_DESC;
        } else if (WxPayApiType.REFUNDQUERY.equals(wxPayApiType)) {
            // 退款查询
            errorCodeArr = REFUNDQUERY_ERROR_CODE;
            errorDescArr = REFUNDQUERY_ERROR_DESC;
        }else if(WxPayApiType.TRANSFERS.equals(wxPayApiType)){
            errorCodeArr = TRANSFERS_ERROR_CODE;
            errorDescArr = TRANSFERS_ERROR_DESC;
        }

        // ------------------- 获取对应错误信息 -------------------
        if (errorCodeArr != null && errorCodeArr.length > 0) {
            for (int i = 0; i < errorCodeArr.length; i++) {
                if (errorCode.equals(errorCodeArr[i])) {
                    return errorDescArr[i];
                }
            }
        }
        return "错误信息未知!";
    }

}
