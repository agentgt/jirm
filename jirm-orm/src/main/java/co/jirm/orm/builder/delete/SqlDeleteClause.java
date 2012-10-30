package co.jirm.orm.builder.delete;

import co.jirm.core.sql.SqlSupplier;



public interface SqlDeleteClause<I> extends DeleteClause<I>, SqlSupplier{
}
