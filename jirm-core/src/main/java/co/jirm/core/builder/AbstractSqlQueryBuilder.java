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

import com.google.common.base.Function;

import co.jirm.core.sql.MutableParameterizedSql;


public abstract class AbstractSqlQueryBuilder<B, H> 
	extends MutableParameterizedSql<B> implements QueryHandoff<H>{

	private final Function<B, H> handoffFunction;
	
	protected AbstractSqlQueryBuilder(String sql, Function<B, H> handOffFunction) {
		super(sql);
		this.handoffFunction = handOffFunction;
	}

	@Override
	public H query() {
		return handoffFunction.apply(getSelf());
	}

}
