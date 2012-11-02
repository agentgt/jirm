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

import java.util.List;

import co.jirm.orm.builder.AbstractWhereClauseBuilder;
import co.jirm.orm.builder.ImmutableCondition;

import com.google.common.collect.Lists;


public class DeleteWhereClauseBuilder<I> extends AbstractWhereClauseBuilder<DeleteWhereClauseBuilder<I>, I> implements DeleteClause<I> {

	protected final List<DeleteClause<I>> children = Lists.newArrayList();
	private final DeleteClause<I> parent;
	
	private DeleteWhereClauseBuilder(DeleteClause<I> parent, ImmutableCondition condition) {
		super(condition);
		this.parent = parent;
	}
	
	static <I> DeleteWhereClauseBuilder<I> newInstance(DeleteClause<I> parent) {
		return new DeleteWhereClauseBuilder<I>(parent, ImmutableCondition.where());
	}

	@Override
	protected DeleteWhereClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	public DeleteClauseType getType() {
		return DeleteClauseType.WHERE;
	}
	
	protected <K extends DeleteClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I execute() {
		return parent.execute();
	}

	@Override
	public <C extends DeleteClauseVisitor> C accept(C visitor) {
		visitor.visit(getSelf());
		for (DeleteClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}


}
