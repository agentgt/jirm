/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	
	public static OrmConfig newInstance(SqlExecutor sqlExecutor, SqlObjectConfig objectConfig) {
		return new OrmConfig(sqlExecutor, SqlObjectConfig.DEFAULT, SqlWriterStrategy.newInstance("\n"));
	}
	
	public static OrmConfig newInstance(SqlExecutor sqlExecutor) {
		return new OrmConfig(sqlExecutor, SqlObjectConfig.DEFAULT, SqlWriterStrategy.newInstance(" "));
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
