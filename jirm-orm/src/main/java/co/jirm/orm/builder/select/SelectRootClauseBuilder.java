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

import co.jirm.orm.builder.ImmutableCondition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


public class SelectRootClauseBuilder<I> implements SelectClause<I> {
	
	private final SelectClauseTransform<SelectRootClauseBuilder<I>, I> factory;
	protected final List<SelectClause<I>> children = Lists.newArrayList();
	
	
	private SelectRootClauseBuilder(SelectClauseTransform<SelectRootClauseBuilder<I>, I> factory) {
		super();
		this.factory = factory;
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
	public SelectClauseType getType() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public boolean isNoOp() {
		return true;
	}
	@Override
	public I query() {
		return factory.transform(this);
	}
	
	public SelectWhereClauseBuilder<I> where() {
		return addClause(SelectWhereClauseBuilder.newWhereClauseBuilder(this, ImmutableCondition.where()));
	}
	
	public SelectWhereClauseBuilder<I> where(String sql) {
		return addClause(SelectWhereClauseBuilder.newWhereClauseBuilder(this, ImmutableCondition.where(sql)));
	}
	
	public SelectRootClauseBuilder<I> header(String sql) {
		addFirstClause(SelectCustomClauseBuilder.newInstance(this, sql));
		return this;
	}

	public static <I> SelectRootClauseBuilder<I> newInstance(SelectClauseTransform<SelectRootClauseBuilder<I>, I> factory) {
		return new SelectRootClauseBuilder<I>(factory);
	}
	
	public SelectCustomClauseBuilder<I> sql(String sql) {
		return addClause(SelectCustomClauseBuilder.newInstance(this, sql));
	}
	
	public SelectCustomClauseBuilder<I> sqlFromResource(Class<?> k, String resource) {
		return addClause(SelectCustomClauseBuilder.newInstance(this, "").useResource(k, resource));
	}
	
	protected <K extends SelectClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	protected <K extends SelectClause<I>> K addFirstClause(K k) {
		children.add(0, k);
		return k;
	}
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		for (SelectClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

}
