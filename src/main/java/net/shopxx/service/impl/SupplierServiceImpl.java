package net.shopxx.service.impl;

import net.shopxx.Page;
import net.shopxx.Pageable;
import net.shopxx.dao.SupplierDao;
import net.shopxx.entity.Supplier;
import net.shopxx.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SupplierServiceImpl extends  BaseServiceImpl<Supplier,Long> implements SupplierService {

    @Autowired
    private SupplierDao supplierDao;

    @Override
    public Page<Supplier> findPage(String supplierName, String name ,String position,String tel, Pageable pageable) {
        return supplierDao.findPage(supplierName,name,position,tel,pageable);
    }

    @Override
    public void saveSupplier(Supplier supplier) {
        supplierDao.saveSupplier(supplier);
    }

    @Override
    public void updateSupplier(Supplier supplier) {
        supplierDao.updateSupplier(supplier);
    }

    @Override
    public void deleteSupplier(Long ids) {
        supplierDao.deleteSupplier(ids);
    }
}
