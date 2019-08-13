package net.shopxx.dao.impl;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.dao.SupplierDao;
import net.shopxx.entity.Order;
import net.shopxx.entity.Supplier;
import net.shopxx.util.DateUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;

@Repository
public class SupplierDaoImpl extends BaseDaoImpl<Supplier,Long> implements SupplierDao{


    @Override
    public Page<Supplier> findPage(String supplierName, String name, String position, String tel,Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Supplier> criteriaQuery = criteriaBuilder.createQuery(Supplier.class);
        Root<Supplier> root = criteriaQuery.from(Supplier.class);
        criteriaQuery.select(root);
        Predicate restrictions = criteriaBuilder.conjunction();

        if (supplierName !=null && supplierName !=""){
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("supplierName"), supplierName));
        }
        if (position !=null && position !=""){
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("position"), position));
        }
        if (tel !=null && tel !=""){
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("tel"), tel));
        }
        if (name !=null && name !=""){
            restrictions = criteriaBuilder.and(restrictions, criteriaBuilder.equal(root.get("name"), name));
        }

        criteriaQuery.where(restrictions);
        return super.findPage(criteriaQuery, pageable);

    }

    @Override
    public void saveSupplier(Supplier supplier) {
        String sql = "INSERT INTO supplier (id,version,supplierName,name,position,tel,createdDate,lastModifiedDate) VALUES (?,1,?,?,?,?,?,?)" ;
        Session session = entityManager.unwrap(Session.class);
        session.createSQLQuery(sql)
                .setParameter(0,supplier.getId())
                .setParameter(1,supplier.getSupplierName())
                .setParameter(2,supplier.getName())
                .setParameter(3,supplier.getPosition())
                .setParameter(4,supplier.getTel())
                .setParameter(5, DateUtils.myFormatDate(new Date().toString()))
                .setParameter(6, DateUtils.myFormatDate(new Date().toString())).executeUpdate();
    }

    @Override
    public void updateSupplier(Supplier supplier) {
        String sql =" UPDATE supplier SET" +
                    " supplierName = ? ," +
                    " name = ? ," +
                    " position = ? ," +
                    " tel = ? " +
                    " WHERE" +
                    " id = ? " ;
        Session session = entityManager.unwrap(Session.class);
        session.createSQLQuery(sql)
                .setParameter(0,supplier.getSupplierName())
                .setParameter(1,supplier.getName())
                .setParameter(2,supplier.getPosition())
                .setParameter(3,supplier.getTel())
                .setParameter(4,supplier.getId()).executeUpdate();
    }

    @Override
    public void deleteSupplier(Long ids) {
        String sql =" DELETE From supplier WHERE id = ? ";
        Session session = entityManager.unwrap(Session.class);
        session.createSQLQuery(sql)
                .setParameter(0,ids).executeUpdate();
    }
}
