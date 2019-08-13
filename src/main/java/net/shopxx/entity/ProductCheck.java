package net.shopxx.entity;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Entity - 盘点
 * @author yangli
 *
 */
@Entity
@Table(name = "ProductCheck")
public class ProductCheck extends BaseEntity<Long>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Store store;//店铺

	private Long productId; //产品Id
	
	private String productName;//产品名字

	private String img; //产品图片
	
	private String sn; //产品编号
	
	private String specifications; //规格
	
	private BigDecimal count;//数量
	
	private BigInteger countCheck;//盘点数量

	public Store getStore() {
		return store;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public void setStore(Store store) {
		this.store = store;
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

	public BigInteger getCountCheck() {
		return countCheck;
	}

	public void setCountCheck(BigInteger countCheck) {
		this.countCheck = countCheck;
	}
	
	

}
