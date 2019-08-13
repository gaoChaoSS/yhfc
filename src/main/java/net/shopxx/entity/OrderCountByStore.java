package net.shopxx.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 按店铺统计订单bean
 * @author yangli
 *
 */
public class OrderCountByStore implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BigInteger id;//店铺id
	private String storeName;//店铺名字
	private String productName;//产品名字
	private String sn;//产品sn
	private String specifications;//规格
	private BigDecimal count;//数量
	private Integer status;//订单状态值
	private String statusDesc;//订单状态
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public BigInteger getId() {
		return id;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getSn() {
		return sn;
	}
	public void setSn(String sn) {
		this.sn = sn;
	}
	public String getSpecifications() {
		return specifications;
	}
	public void setSpecifications(String specifications) {
		this.specifications = specifications;
	}
	public BigDecimal getCount() {
		return count;
	}
	public void setCount(BigDecimal count) {
		this.count = count;
	}
	

}
