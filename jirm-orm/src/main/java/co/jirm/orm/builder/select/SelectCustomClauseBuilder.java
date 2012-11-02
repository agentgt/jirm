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




public class SelectCustomClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<SelectCustomClauseBuilder<I>, I> {
	
	
	private SelectCustomClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.CUSTOM, sql);
	}
	
	public static <I> SelectCustomClauseBuilder<I> newInstance(SelectClause<I> clause, String sql) {
		return new SelectCustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected SelectCustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
