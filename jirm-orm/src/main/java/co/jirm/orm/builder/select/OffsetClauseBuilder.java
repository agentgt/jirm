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




public class OffsetClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<OffsetClauseBuilder<I>, I> {
	
	private OffsetClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.OFFSET, sql);
	}
	
	static <I> OffsetClauseBuilder<I> newInstance(SelectClause<I> parent, String sql) {
		return new OffsetClauseBuilder<I>(parent, sql);
	}
	
	static <I> OffsetClauseBuilder<I> newInstanceWithOffset(SelectClause<I> parent, Number i) {
		return new OffsetClauseBuilder<I>(parent, "?").with(i.longValue());
	}

	@Override
	protected OffsetClauseBuilder<I> getSelf() {
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
	
	
}
