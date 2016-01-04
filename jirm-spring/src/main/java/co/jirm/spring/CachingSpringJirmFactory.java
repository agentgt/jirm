package co.jirm.spring;

import co.jirm.orm.dao.*;

import javax.sql.*;
import java.util.*;

/**
 * @author Denis Buzdalov
 */
public class CachingSpringJirmFactory extends SpringJirmFactory {
    // informal invariant: forall k :: k in daoCache.keys ==> k.erasure == daoCache.get(k).erasure
    protected final Map<Class<?>, JirmDao<?>> daoCache = new HashMap<Class<?>, JirmDao<?>>();

    public CachingSpringJirmFactory(final DataSource dataSource) {
        super(dataSource);
    }

    public CachingSpringJirmFactory(final DataSource dataSource, final boolean recursive) {
        super(dataSource, recursive);
    }

    @SuppressWarnings("unchecked") // due to invariant of the daoCache
    @Override
    public <T> JirmDao<T> daoFor(final Class<T> k) {
        final JirmDao<?> existingDao = daoCache.get(k);
        if (existingDao == null) {
            final JirmDao<T> newDao = super.daoFor(k);
            daoCache.put(k, newDao);

            return newDao;
        } else {
            return (JirmDao<T>) existingDao;
        }
    }
}
