package net.shopxx.entity;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 *  订单取货通知短信发送记录
 *  @Auther: Demaxiya
 *  @Create: 2019-08-01 15:54
 */
@Entity
@Table(name = "OrderNoticeSms")
public class OrderNoticeSms extends BaseEntity<Long>{

    private static final long serialVersionUID = 1L;

    private Long orderId; // 订单

    private String phone;// 电话

    private String content;// 短信内容

    private boolean isSucess;// 是否发送成功

    private Date successDate;// 发送成功时间

    public Date getSuccessDate() {
        return successDate;
    }

    public void setSuccessDate(Date successDate) {
        this.successDate = successDate;
    }

    public Long getOrder() {
        return orderId;
    }

    public void setOrder(Long orderId) {
        this.orderId = orderId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSucess() {
        return isSucess;
    }

    public void setSucess(boolean sucess) {
        isSucess = sucess;
    }
}
