package net.shopxx.service.impl;

import net.shopxx.dao.OSSDao;
import net.shopxx.service.OSSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OSSServiceImpl implements OSSService{

    @Autowired
    private OSSDao ossDao ;


    @Override
    public Map<String, Object> queryConfig() {
        return ossDao.queryConfig();
    }
}
