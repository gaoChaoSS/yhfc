package net.shopxx.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 订单导出excle封装对象
 * @author yangli
 *
 */
public class OrderSheetDetails implements Serializable{
	
	private Long id;//店铺id
	private String storeName;//店铺名字
	private List<OrderDetailsByStore> list;//店铺数据(可以放汇总，可以放明细)
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public List<OrderDetailsByStore> getList() {
		return list;
	}
	public void setList(List<OrderDetailsByStore> list) {
		this.list = list;
	}
	
	

}
