package net.shopxx.dao.impl;

import net.shopxx.dao.KeyValueDao;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class KeyValueIDaoImpl implements KeyValueDao {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    public List<Map<String, Object>> getKey(String key) {
        Session session = entityManager.unwrap(Session.class);

        String sql = " SELECT key_value.`key`,key_value.`value` FROM key_value WHERE `key` = ? ";
        List list = session.createSQLQuery(sql)
                .setParameter(0, key)
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();

        return list;
    }

    /**
     * 获取指定KEY的默认值
     *
     * @param key
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getDefaultForKey(String key) {
        Session session = entityManager.unwrap(Session.class);

        String sql = " SELECT * FROM key_value WHERE `key` = ?  AND isDefault = 1";
        List list = session.createSQLQuery(sql)
                .setParameter(0, key)
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();

        return list != null ? (Map) list.get(0) : new HashMap<>();
    }


}
