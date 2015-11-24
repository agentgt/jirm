package co.jirm.orm.dao;

import co.jirm.mapper.definition.*;

import java.util.*;

public interface DaoHooks {
    public void beforeInsert(SqlObjectDefinition<?> definition, Map<String, Object> values);

    // TODO to add beforeInsertMaps() method and its usage to the JirmDao.
}
