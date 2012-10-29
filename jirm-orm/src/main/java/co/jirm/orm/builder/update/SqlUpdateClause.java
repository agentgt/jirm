package co.jirm.orm.builder.update;

import co.jirm.core.sql.SqlSupplier;



public interface SqlUpdateClause<I> extends UpdateClause<I>, SqlSupplier{
}
