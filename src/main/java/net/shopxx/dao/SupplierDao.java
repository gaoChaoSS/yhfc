package net.shopxx.dao;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.entity.Supplier;

public interface SupplierDao extends BaseDao<Supplier,Long>{
    Page<Supplier> findPage(String supplierName,String name , String position, String tel,Pageable pageable);

    void saveSupplier(Supplier supplier);

    void updateSupplier(Supplier supplier);

    void deleteSupplier(Long ids);
}
