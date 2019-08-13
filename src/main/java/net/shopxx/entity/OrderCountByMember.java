package net.shopxx.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;

import net.shopxx.entity.BaseEntity.BaseView;

/**
 * 按人统计订单（店长待取货功能）
 * @author yangli
 *
 */
public class OrderCountByMember implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public interface orderCountByMember extends BaseView {};
	
	private Long memberId;//用户id
	
	@JsonView(orderCountByMember.class)
	private String name;//姓名
	
	@JsonView(orderCountByMember.class)
	private String telphone;//手机
	
	@JsonView(orderCountByMember.class)
	private Long orderCount;//订单数
	
	
	
	public Long getMemberId() {
		return memberId;
	}
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTelphone() {
		return telphone;
	}
	public void setTelphone(String telphone) {
		this.telphone = telphone;
	}
	public Long getOrderCount() {
		return orderCount;
	}
	public void setOrderCount(Long orderCount) {
		this.orderCount = orderCount;
	}
	
	
	

}
