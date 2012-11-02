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




public class OrderByClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<OrderByClauseBuilder<I>, I> {
	
	private OrderByClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.ORDERBY, sql);
	}
	
	public static <I> OrderByClauseBuilder<I> newInstance(SelectClause<I> parent, String sql) {
		return new OrderByClauseBuilder<I>(parent, sql);
	}
	

	@Override
	protected OrderByClauseBuilder<I> getSelf() {
		return this;
	}
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		for (SelectClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	public LimitClauseBuilder<I> limit(String sql) {
		return addClause(LimitClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(Number i) {
		return addClause(LimitClauseBuilder.newInstanceWithLimit(this, i));
	}
	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(getSelf(), sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(getSelf(), i));
	}
	
	
	
	
}
