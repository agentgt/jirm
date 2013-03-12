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




public class ForShareClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<ForShareClauseBuilder<I>, I> {
	
	private ForShareClauseBuilder(SelectClause<I> parent) {
		super(parent, SelectClauseType.FORSHARE, "");
	}
	
	static <I> ForShareClauseBuilder<I> newInstance(SelectClause<I> parent) {
		return new ForShareClauseBuilder<I>(parent);
	}

	@Override
	protected ForShareClauseBuilder<I> getSelf() {
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
	
	@Override
	public boolean isNoOp() {
		return false;
	}
	
	
}
