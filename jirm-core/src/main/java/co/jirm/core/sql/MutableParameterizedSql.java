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
package co.jirm.core.sql;

import co.jirm.core.sql.SqlPlaceholderParser.ParsedSql;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;


public abstract class MutableParameterizedSql<T> extends MutableParameterized<T> implements ParameterizedSql<T>  {

	private ParsedSql parsedSql;
	
	protected MutableParameterizedSql(String sql) {
		setSql(sql);
	}
	
	@Override
	public String getSql() {
		return parsedSql.getResultSql();
	}
	
	public T setSql(String sql) {
		parsedSql = SqlPlaceholderParser.parseSql(sql);
		return getSelf();
	}
	
	public String getOriginalSql() {
		return parsedSql.getOriginalSql();
	}
	
	public T useResource(Class<?> k, String resource) {
		return setSql(SqlPartialParser.parseFromPath(k, resource));
	}

	@Override
	public ImmutableList<Object> mergedParameters() {
		return parsedSql.mergeParameters(this);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
		.add("parsedSql", parsedSql)
		.add("parameters", getParameters())
		.add("nameParameters", getNameParameters())
		.toString();
	}
	
	
	
	
}
