package net.shopxx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "key_value")
public class KeyValue extends BaseEntity<Long>{

    /**
     * 配置Key
     */
    @Column(name = "key")
    private String key ;

    /**
     * 配置value
     */
    @Column(name = "value")
    private String value ;

    /**
     * 是否默认
     */
    @Column(name = "isDefault")
    private Integer isDefault;


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Integer isDefault) {
        this.isDefault = isDefault;
    }
}
