package net.shopxx.dao.impl;

import net.shopxx.entity.ProductCheck;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import net.shopxx.dao.ProductCheckDao;
import net.shopxx.entity.Ad;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class ProductCheckDaoImpl extends BaseDaoImpl<ProductCheck, Long> implements ProductCheckDao {


    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    public String queryImgBySn(String sn) {
        try {
            //String sql = "SELECT product.productImages FROM product WHERE sn = ? ";
            String sql = "SELECT p.productImages FROM sku s " +
                    "LEFT JOIN product p ON s.product_id=p.id " +
                    "WHERE s.sn= ? ";
            Session session = entityManager.unwrap(Session.class);
            List list = session.createSQLQuery(sql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                    .setParameter(0, sn).list();
            if (list.size() < 1) {
                return "";
            }
            return list.get(0).toString();
        } catch (Exception e) {
            return "";
        }
    }
}
