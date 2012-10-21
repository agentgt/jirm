package co.jirm.core.builder;

import java.util.List;

import com.google.common.base.Optional;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.sql.ParametersSql;


public class SqlTypedQueryFor<T> extends SqlQueryFor implements TypedQueryFor<T> {

	private final SqlExecutorRowMapper<T> rowMapper;
	
	private SqlTypedQueryFor(
			SqlExecutor sqlExecutor, 
			SqlExecutorRowMapper<T> rowMapper,
			ParametersSql parametersSql) {
		super(sqlExecutor, parametersSql);
		this.rowMapper = rowMapper;
	}
	
	public static <T> SqlTypedQueryFor<T> newTypedQueryFor(
			SqlExecutor sqlExecutor, 
			SqlExecutorRowMapper<T> rowMapper,
			ParametersSql parametersSq) {
		return new SqlTypedQueryFor<T>(sqlExecutor, rowMapper, parametersSq);
	}

	@Override
	public List<T> forList() {
		return sqlExecutor.queryForList(parametersSql.getSql(),
				getRowMapper(),
				parametersSql.mergedParameters().toArray());
	}

	@Override
	public Optional<T> forOptional() {
		return sqlExecutor.queryForOptional(parametersSql.getSql(),
				getRowMapper(),
				parametersSql.mergedParameters().toArray());
	}

	@Override
	public T forObject() {
		return sqlExecutor.queryForObject(parametersSql.getSql(),
				getRowMapper(),
				parametersSql.mergedParameters().toArray());
	}
	
	
	
	public SqlExecutorRowMapper<T> getRowMapper() {
		return rowMapper;
	}

}
