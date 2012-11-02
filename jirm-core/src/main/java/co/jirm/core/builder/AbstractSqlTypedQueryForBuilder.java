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
