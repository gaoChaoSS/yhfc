package net.shopxx.entity;


import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 供货商
 */
@Entity
@Table(name="supplier")
public class Supplier extends BaseEntity<Long>{

    private static final long serialVersionUID = -6977025562650112567L;

    @Id
    private Long id ;

    //供应商名字
    @Column
    private String supplierName;

    //联系人名字
    @Column
    private String name ;

    //供应商联系电话
    @Column
    private String tel;

    //供应商创建时间
    @Column
    private Date createdDate ;

    //供应商地址
    @Column
    private String  position ;

    //提供的商品
    @Transient
    @OneToMany(mappedBy = "supplier")
    @Cascade(value = {org.hibernate.annotations.CascadeType.DELETE})
    private List<Product> productList ;


    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date date) {
        this.createdDate = date;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public List<Product> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> productList) {
        this.productList = productList;
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", supplierName='" + supplierName + '\'' +
                ", name='" + name + '\'' +
                ", tel='" + tel + '\'' +
                ", createdDate=" + createdDate +
                ", position='" + position + '\'' +
                ", productList=" + productList +
                '}';
    }
}
