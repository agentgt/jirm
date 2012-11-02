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
package co.jirm.orm.builder.delete;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.List;

import co.jirm.core.sql.MutableParameterizedSql;
import co.jirm.core.sql.SqlSupplier;

import com.google.common.collect.Lists;


public abstract class AbstractSqlParameterizedDeleteClause<B, I> extends MutableParameterizedSql<B> implements SqlDeleteClause<I>, SqlSupplier{

	protected final List<DeleteClause<I>> children = Lists.newArrayList();
	private final DeleteClause<I> parent;
	private final DeleteClauseType type;

	protected AbstractSqlParameterizedDeleteClause(DeleteClause<I> parent, DeleteClauseType type, String sql) {
		super(sql);
		this.parent = parent;
		this.type = type;
	}
	
	public DeleteClauseType getType() {
		return type;
	}
	
	@Override
	public boolean isNoOp() {
		return (nullToEmpty(getSql()).trim().isEmpty());
	}
	
	protected <K extends DeleteClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I execute() {
		return parent.execute();
	}

}
