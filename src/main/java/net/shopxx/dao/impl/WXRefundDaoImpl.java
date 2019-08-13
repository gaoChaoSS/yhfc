package net.shopxx.dao.impl;

import net.shopxx.dao.WXRefundDao;
import net.shopxx.entity.OrderReturns;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;


@Repository
public class WXRefundDaoImpl  implements WXRefundDao {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public BigDecimal queryAllRefundMoneyByOrderId(String orderId) {
        String sql = "select SUM(orderreturnsitem.quantity*product.price) AS refundMoney FROM orderreturnsitem " +
                     " LEFT JOIN orderreturns ON orderreturns.id = orderreturnsitem.orderReturns_id " +
                     " LEFT JOIN product ON product.sn = orderreturnsitem.sn " +
                     " WHERE orderreturns.orders = ?" ;
        Session session = entityManager.unwrap(Session.class);
        List list = session.createSQLQuery(sql).setParameter(0, orderId).list();
        return  new BigDecimal(list.get(0).toString());
    }

    @Override
    public void updateRefundNum(String sn, String refundNum) {
        String sql ="UPDATE orderreturns SET refundNum = ? WHERE sn = ? " ;
        Session session = entityManager.unwrap(Session.class);
        session.createSQLQuery(sql).setParameter(0,refundNum).setParameter(1,sn).executeUpdate();
    }
}
