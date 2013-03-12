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



public abstract class SelectClauseVisitor {
	
	public abstract void visit(SelectWhereClauseBuilder<?> whereClauseBuilder);
	public abstract void visit(OrderByClauseBuilder<?> clauseBuilder);
	public abstract void visit(LimitClauseBuilder<?> limitClauseBuilder);
	public abstract void visit(OffsetClauseBuilder<?> clauseBuilder);
	public abstract void visit(ForUpdateClauseBuilder<?> clauseBuilder);
	public abstract void visit(ForShareClauseBuilder<?> clauseBuilder);
	public abstract void visit(SelectCustomClauseBuilder<?> clauseBuilder);

	
	
	public Object startOn(SelectVisitorAcceptor klause) {
		klause.accept(this);
		return this;
	}
	
}
