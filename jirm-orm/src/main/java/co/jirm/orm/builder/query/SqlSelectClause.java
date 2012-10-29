package co.jirm.orm.builder.query;

import co.jirm.core.sql.SqlSupplier;



public interface SqlSelectClause<I> extends SelectClause<I>, SqlSupplier {
}
