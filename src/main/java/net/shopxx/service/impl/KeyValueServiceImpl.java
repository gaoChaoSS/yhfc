package net.shopxx.service.impl;

import net.shopxx.dao.KeyValueDao;
import net.shopxx.service.KeyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Service
public class KeyValueServiceImpl implements KeyValueService{

    @Inject
    private KeyValueDao keyValueDao ;

    @Override
    public String getKey(String key) {
        List<Map<String, Object>> list = keyValueDao.getKey(key);
        String value = (String) list.get(0).get("value");
        return value;
    }

    /**
     * 获取指定的KEY的默认值
     *
     * @param key
     * @return
     */
    @Override
    public String getDefaultForKey(String key) {
        Map<String, Object> map = keyValueDao.getDefaultForKey(key);
        return  map.getOrDefault("value", "baishitong").toString();
    }
}
