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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;



public class DeleteRootClauseBuilder<I> implements DeleteClause<I> {
	
	private final DeleteClauseTransform<DeleteRootClauseBuilder<I>, I> handoff;
	protected final List<DeleteClause<I>> children = Lists.newArrayList();
	
	private DeleteRootClauseBuilder(DeleteClauseTransform<DeleteRootClauseBuilder<I>, I> handoff) {
		super();
		this.handoff = handoff;
	}
	
	public static <I> DeleteRootClauseBuilder<I> newInstance(DeleteClauseTransform<DeleteRootClauseBuilder<I>, I> factory) {
		return new DeleteRootClauseBuilder<I>(factory);
	}
	
	
	public DeleteRootClauseBuilder<I> header(String sql) {
		addFirstClause(DeleteCustomClauseBuilder.newInstance(this, sql));
		return this;
	}
	
	public DeleteWhereClauseBuilder<I> where() {
		return addClause(DeleteWhereClauseBuilder.newInstance(this));
	}
	
	protected <K extends DeleteClause<I>> K addFirstClause(K k) {
		children.add(0, k);
		return k;
	}
	
	@Override
	public ImmutableList<Object> getParameters() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public ImmutableMap<String, Object> getNameParameters() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public ImmutableList<Object> mergedParameters() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public DeleteClauseType getType() {
		return DeleteClauseType.ROOT;
	}
	
	@Override
	public boolean isNoOp() {
		return true;
	}
	@Override
	public I execute() {
		return handoff.transform(this);
	}

	@Override
	public <C extends DeleteClauseVisitor> C accept(C visitor) {
		for (DeleteClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	protected <K extends DeleteClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}

}
