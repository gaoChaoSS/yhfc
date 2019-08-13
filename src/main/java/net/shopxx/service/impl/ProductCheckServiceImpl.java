package net.shopxx.service.impl;


import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.shopxx.dao.ProductCheckDao;
import org.springframework.stereotype.Service;

import net.shopxx.entity.ProductCheck;
import net.shopxx.service.ProductCheckService;

import javax.inject.Inject;


@Service
public class ProductCheckServiceImpl extends BaseServiceImpl<ProductCheck, Long> implements ProductCheckService{


    @Inject
    private ProductCheckDao productCheckDao ;

    @Override
    public String queryImgBySn(String sn) {

        String imgBySn = productCheckDao.queryImgBySn(sn);
        if("".equals(imgBySn) || null == imgBySn){
            return "";
        }
        JSONObject jsonObject = JSONObject.fromObject(imgBySn);
        String productImages = jsonObject.getString("productImages");
        JSONArray jsonArray = JSONArray.fromObject(productImages);
        if(jsonArray.size()<1){
            return "";
        }
        String source = jsonArray.getJSONObject(0).getString("thumbnail");
        System.err.println(source);
        return source;

    }
}
