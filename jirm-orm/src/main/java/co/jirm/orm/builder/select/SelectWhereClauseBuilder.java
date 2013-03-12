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

import java.util.List;

import co.jirm.orm.builder.AbstractWhereClauseBuilder;
import co.jirm.orm.builder.ImmutableCondition;

import com.google.common.collect.Lists;


public class SelectWhereClauseBuilder<I> extends AbstractWhereClauseBuilder<SelectWhereClauseBuilder<I>, I> implements SelectClause<I> {
	
	private final SelectClause<I> parent;
	private final List<SelectClause<I>> children = Lists.newArrayList();
	
	private SelectWhereClauseBuilder(SelectClause<I> parent, ImmutableCondition condition) {
		super(condition);
		this.parent = parent;
	}
	
	static <I> SelectWhereClauseBuilder<I> newWhereClauseBuilder(SelectClause<I> parent, ImmutableCondition condition) {
		return new SelectWhereClauseBuilder<I>(parent, condition);
	}
	
	public OrderByClauseBuilder<I> orderBy(String sql) {
		return addClause(OrderByClauseBuilder.newInstanceForProperty(parent, sql));
	}
	
	public OrderByClauseBuilder<I> orderByCustom(String sql) {
		return addClause(OrderByClauseBuilder.newInstanceForCustom(parent, sql));
	}
	
	public LimitClauseBuilder<I> limit(String sql) {
		return addClause(LimitClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(Number i) {
		return addClause(LimitClauseBuilder.newInstanceWithLimit(this, i));
	}
	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(this, sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(this, i));
	}
	
	public ForUpdateClauseBuilder<I> forUpdate() {
		return addClause(ForUpdateClauseBuilder.newInstance(this));
	}
	
	public ForShareClauseBuilder<I> forShare() {
		return addClause(ForShareClauseBuilder.newInstance(this));
	}

	@Override
	protected SelectWhereClauseBuilder<I> getSelf() {
		return this;
	}
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(getSelf());
		for (SelectClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	protected <K extends SelectClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}

	@Override
	public SelectClauseType getType() {
		return SelectClauseType.WHERE;
	}

	@Override
	public I query() {
		return parent.query();
	}

}
