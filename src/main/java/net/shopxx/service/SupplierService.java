package net.shopxx.service;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.entity.Supplier;

public interface SupplierService extends BaseService<Supplier,Long>{

    Page<Supplier> findPage(String supplierName,String  name,String position,String tel ,Pageable pageable);

    void saveSupplier(Supplier supplier);

    void updateSupplier(Supplier supplier);

    void deleteSupplier(Long ids);
}
