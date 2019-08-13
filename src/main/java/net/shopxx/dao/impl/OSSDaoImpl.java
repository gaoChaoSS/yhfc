package net.shopxx.dao.impl;

import net.shopxx.dao.OSSDao;
import org.hibernate.Session;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;

@Repository
public class OSSDaoImpl implements OSSDao {

    @PersistenceContext
    protected EntityManager entityManager;
    @Override
    public Map<String, Object> queryConfig() {
        String sql = "SELECT attributes FROM  pluginconfig WHERE pluginId = 'ossStoragePlugin' " ;
        Session session = entityManager.unwrap(Session.class);
        List<Map<String,Object>> list = session.createSQLQuery(sql).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
        return list.get(0);
    }
}
