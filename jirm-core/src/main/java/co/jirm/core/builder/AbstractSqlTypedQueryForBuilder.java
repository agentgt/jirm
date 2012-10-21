package co.jirm.core.builder;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.sql.ParametersSql;

import com.google.common.base.Function;


public abstract class AbstractSqlTypedQueryForBuilder<B extends ParametersSql, T> extends AbstractSqlQueryBuilder<B, SqlTypedQueryFor<T>> {

	protected AbstractSqlTypedQueryForBuilder(
			String sql,
			final SqlExecutor sqlExecutor,
			final SqlExecutorRowMapper<T> rowMapper) {
		super(sql, new Function<B, SqlTypedQueryFor<T>>() {
			@Override
			public SqlTypedQueryFor<T> apply(B input) {
				return SqlTypedQueryFor.newTypedQueryFor(sqlExecutor, rowMapper, input);
			}
		});
	}

}
