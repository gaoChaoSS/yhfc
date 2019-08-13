package net.shopxx.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 订单统计对象-按供应商 excel 封装
 * @author yangli
 *
 */
public class OrderBuySheet implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Long rootCategory_id;//商品父类id
	private String rootCategory;//商品父类
	private List<OrderBuy> list;//订单统计
	
	
	public Long getRootCategory_id() {
		return rootCategory_id;
	}
	public void setRootCategory_id(Long rootCategory_id) {
		this.rootCategory_id = rootCategory_id;
	}
	public String getRootCategory() {
		return rootCategory;
	}
	public void setRootCategory(String rootCategory) {
		this.rootCategory = rootCategory;
	}
	public List<OrderBuy> getList() {
		return list;
	}
	public void setList(List<OrderBuy> list) {
		this.list = list;
	}
	
	

}
