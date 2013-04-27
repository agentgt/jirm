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

import java.util.concurrent.atomic.AtomicBoolean;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.core.util.SafeAppendable;
import co.jirm.orm.builder.ConditionVisitors.ParametersConditionVisitor;


public class InsertClauseVisitors {
	
	public abstract static class SimpleClauseVisitor extends InsertClauseVisitor {

		protected abstract void doVisit(SqlInsertClause<?> clause);
		
		@Override
		public void visit(InsertCustomClauseBuilder<?> custom) {
			doVisit(custom);
		}

		@Override
		public abstract void visit(InsertValuesClauseBuilder<?> valuesClauseBuilder);
		
	}
	
	public abstract static class ParametersClauseVisitor extends InsertClauseVisitor {

		private final ParametersConditionVisitor conditionVisitor = new ParametersConditionVisitor() {
			@Override
			public void doParameters(Parameters p) {
				ParametersClauseVisitor.this.doParameters(p);
			}
		};
		
		@Override
		public void visit(InsertValuesClauseBuilder<?> values) {
//			where.getCondition().accept(conditionVisitor);

		}

		@Override
		public void visit(InsertCustomClauseBuilder<?> custom) {
			doParameters(custom);
		}

		public abstract void doParameters(Parameters p);
		
	}
	
	public static MutableParameters getParameters(InsertVisitorAcceptor k) {
		final MutableParameters mp = new MutableParameters();
		new ParametersClauseVisitor() {
			@Override
			public void doParameters(Parameters p) {
				mp.addAll(p);
			}
		}.startOn(k);
		return mp;
	}
	
	public static InsertClauseVisitor clauseVisitor(final Appendable appendable) {

		final SafeAppendable sb = new SafeAppendable(appendable);
		
		SimpleClauseVisitor cv = new SimpleClauseVisitor() {
			
			AtomicBoolean first = new AtomicBoolean(true);
			
			/*
			 * TODO take care of multiples and order.
			 */
			
			@Override
			protected void doVisit(SqlInsertClause<?> clause) {
				if (! appendStart(clause)) return;
				sb.append(clause.getSql());
			}
			
			@Override
			public void visit(InsertValuesClauseBuilder<?> valuesClauseBuilder) {
			}
			
			private boolean appendStart(InsertClause<?> k) {
				if (k.isNoOp()) return false;
				if (first.get() ) {
					first.set(false);
				}
				else {
					sb.append(" ");
				}
				if (k.getType() != InsertClauseType.CUSTOM)
					sb.append(k.getType().getSql()).append(" ");
				return true;
			}

		};
		return cv;
	}
}
