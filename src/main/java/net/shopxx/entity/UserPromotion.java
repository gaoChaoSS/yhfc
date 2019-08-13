package net.shopxx.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "user_promotion")
public class UserPromotion extends BaseEntity<Long> {


    /****
     *
     * 推广码
     *
     */
    @Column(name = "promotionCode")
    private String promotionCode;

    /****
     *
     * 类型
     *
     */
    @Column(name = "type")
    private int type;

    /****
     *
     * 二维码地址
     *
     */
    @Column(name = "codeUrl")
    private String codeUrl;

    /****
     *
     * 备注说明
     *
     */
    @Column(name = "remark")
    private String remark;

    /****
     *
     * 推广人电话
     *
     */
    @Column(name = "phone")
    private String phone ;

    /****
     *
     * 推广人姓名
     *
     */
    @Column(name = "name")
    private String name;

    /****
     * 创建人
     *
     */
    private String createBy;

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public void setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public static void main(String[] args) {
        for(int i=0;i<=9;i++){
            System.out.println(UUID.randomUUID().toString().toUpperCase().replaceAll("-",""));
        }
    }

}
