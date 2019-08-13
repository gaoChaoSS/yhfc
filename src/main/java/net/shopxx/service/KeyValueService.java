package net.shopxx.service;

import java.util.List;
import java.util.Map;

public interface KeyValueService {
    String getKey(String key);

    /**
     * 获取指定的KEY的默认值
     * @param key
     * @return
     */
    String getDefaultForKey(String key);
}
