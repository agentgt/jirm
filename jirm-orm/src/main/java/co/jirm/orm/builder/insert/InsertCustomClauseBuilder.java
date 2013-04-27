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
package co.jirm.orm.builder.insert;




public class InsertCustomClauseBuilder<I> extends AbstractSqlParameterizedInsertClause<InsertCustomClauseBuilder<I>, I> {
	
	
	private InsertCustomClauseBuilder(InsertClause<I> parent, String sql) {
		super(parent, InsertClauseType.CUSTOM, sql);
	}
	
	public static <I> InsertCustomClauseBuilder<I> newInstance(InsertClause<I> clause, String sql) {
		return new InsertCustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected InsertCustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends InsertClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
