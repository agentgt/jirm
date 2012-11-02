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
