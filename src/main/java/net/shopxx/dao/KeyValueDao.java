package net.shopxx.dao;

import java.util.List;
import java.util.Map;

public interface KeyValueDao {
    List<Map<String,Object>> getKey(String key);

    /**
     * 获取指定KEY的默认值
     * @param key
     * @return
     */
    Map<String, Object> getDefaultForKey(String key);
}
