package co.jirm.orm;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.orm.writer.SqlWriterStrategy;


public class OrmConfig {
	private final SqlExecutor sqlExecutor;
	private final SqlObjectConfig sqlObjectConfig;
	private final SqlWriterStrategy sqlWriterStrategy;
	
	private OrmConfig(SqlExecutor sqlExecutor, SqlObjectConfig sqlObjectConfig, SqlWriterStrategy sqlWriterStrategy) {
		super();
		this.sqlExecutor = sqlExecutor;
		this.sqlObjectConfig = sqlObjectConfig;
		this.sqlWriterStrategy = sqlWriterStrategy;
	}
	
	public SqlWriterStrategy getSqlWriterStrategy() {
		return sqlWriterStrategy;
	}
	
	public SqlObjectConfig getSqlObjectConfig() {
		return sqlObjectConfig;
	}
	
	public SqlExecutor getSqlExecutor() {
		return sqlExecutor;
	}

}
