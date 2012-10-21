package co.jirm.core.builder;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.sql.ParametersSql;


public class SqlQueryFor implements QueryFor {
	protected final SqlExecutor sqlExecutor;
	protected final ParametersSql parametersSql;
	
	protected SqlQueryFor(SqlExecutor sqlExecutor, ParametersSql parametersSql) {
		super();
		this.sqlExecutor = sqlExecutor;
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

}
