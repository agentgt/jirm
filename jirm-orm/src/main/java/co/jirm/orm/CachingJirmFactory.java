package co.jirm.orm;

import co.jirm.orm.dao.*;

import java.util.*;

/**
 * @author Denis Buzdalov
 */
public class CachingJirmFactory implements JirmFactory {
    protected final JirmFactory mainFactory;

    // informal invariant: forall k :: k in daoCache.keys ==> k.erasure == daoCache.get(k).erasure
    protected final Map<Class<?>, JirmDao<?>> daoCache = new HashMap<Class<?>, JirmDao<?>>();

    public CachingJirmFactory(final JirmFactory mainFactory) {
        this.mainFactory = mainFactory;
    }

    @SuppressWarnings("unchecked") // due to invariant of the daoCache
    @Override
    public <K> JirmDao<K> daoFor(final Class<K> k) {
        final JirmDao<?> existingDao = daoCache.get(k);
        if (existingDao == null) {
            final JirmDao<K> newDao = mainFactory.daoFor(k);
            daoCache.put(k, newDao);

            return newDao;
        } else {
            return (JirmDao<K>) existingDao;
        }
    }
}
