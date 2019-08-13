package net.shopxx.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 订单统计对象-按供应商
 * @author Administrator
 *
 */
public class OrderBuy implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private BigInteger sku_id;
	private BigInteger productCategory_id;//商品分类id
	private String productCategory;//商品分类
	private String treePath;//商品分类树 eg: ,10101,10214,
	private String supplierName;//供货商
	private String productName_now; //当前的商品名字
	private String productName_buy;//购买是的商品名字
	private String sn;
	private String specifications;//规格
	private BigDecimal count;//数量
	private Integer status;//订单状态值
	private String statusDesc;//订单状态
	
	
	
	
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getProductCategory() {
		return productCategory;
	}
	public void setProductCategory(String productCategory) {
		this.productCategory = productCategory;
	}
	public String getSupplierName() {
		return supplierName;
	}
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}
	public BigInteger getSku_id() {
		return sku_id;
	}
	public void setSku_id(BigInteger sku_id) {
		this.sku_id = sku_id;
	}
	public BigInteger getProductCategory_id() {
		return productCategory_id;
	}
	public void setProductCategory_id(BigInteger productCategory_id) {
		this.productCategory_id = productCategory_id;
	}
	public String getTreePath() {
		return treePath;
	}
	public void setTreePath(String treePath) {
		this.treePath = treePath;
	}
	public String getProductName_now() {
		return productName_now;
	}
	public void setProductName_now(String productName_now) {
		this.productName_now = productName_now;
	}
	public String getProductName_buy() {
		return productName_buy;
	}
	public void setProductName_buy(String productName_buy) {
		this.productName_buy = productName_buy;
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
