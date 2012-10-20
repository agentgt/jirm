package co.jirm.core.builder;

import java.util.List;

import com.google.common.base.Optional;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.sql.ParametersSql;


public class SqlTypedQueryFor<T> implements TypedQueryFor<T> {
	private final SqlExecutor sqlExecutor;
	private final SqlExecutorRowMapper<T> rowMapper;
	private final ParametersSql parametersSql;
	
	private SqlTypedQueryFor(
			SqlExecutor sqlExecutor, 
			SqlExecutorRowMapper<T> rowMapper,
			ParametersSql parametersSql) {
		super();
		this.sqlExecutor = sqlExecutor;
		this.rowMapper = rowMapper;
		this.parametersSql = parametersSql;
	}

	@Override
	public int forInt() {
		return sqlExecutor.queryForInt(parametersSql.getSql(), 
				parametersSql.mergedParameters().toArray());
	}

	@Override
	public long forLong() {
		return sqlExecutor.queryForLong(parametersSql.getSql(), 
				parametersSql.mergedParameters().toArray());
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
