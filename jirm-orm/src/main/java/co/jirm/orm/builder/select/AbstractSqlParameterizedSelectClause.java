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
package co.jirm.orm.builder.select;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.List;

import co.jirm.core.sql.MutableParameterizedSql;

import com.google.common.collect.Lists;


public abstract class AbstractSqlParameterizedSelectClause<T, I> extends MutableParameterizedSql<T> implements SqlSelectClause<I>{

	protected final List<SelectClause<I>> children = Lists.newArrayList();
	private final SelectClause<I> parent;
	private final SelectClauseType type;

	protected AbstractSqlParameterizedSelectClause(SelectClause<I> parent, SelectClauseType type, String sql) {
		super(sql);
		this.parent = parent;
		this.type = type;
	}
	
	public SelectClauseType getType() {
		return type;
	}
	
	@Override
	public boolean isNoOp() {
		return (nullToEmpty(getSql()).trim().isEmpty());
	}
	
	protected <K extends SelectClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I query() {
		return parent.query();
	}

}
