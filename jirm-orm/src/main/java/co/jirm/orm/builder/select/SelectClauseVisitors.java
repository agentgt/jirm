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

import static com.google.common.base.Strings.nullToEmpty;

import java.util.concurrent.atomic.AtomicBoolean;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.core.util.SafeAppendable;
import co.jirm.orm.builder.ConditionVisitors;
import co.jirm.orm.builder.ConditionVisitors.ParametersConditionVisitor;


public class SelectClauseVisitors {
	
	public abstract static class SimpleClauseVisitor extends SelectClauseVisitor {

		protected abstract void doVisit(SqlSelectClause<?> clause);
		
		@Override
		public abstract void visit(SelectWhereClauseBuilder<?> whereClauseBuilder);

		@Override
		public abstract void visit(OrderByClauseBuilder<?> clauseBuilder);

		@Override
		public void visit(LimitClauseBuilder<?> limitClauseBuilder) {
			doVisit(limitClauseBuilder);
		}

		@Override
		public void visit(OffsetClauseBuilder<?> clauseBuilder) {
			doVisit(clauseBuilder);
		}

		@Override
		public void visit(SelectCustomClauseBuilder<?> clauseBuilder) {
			doVisit(clauseBuilder);
		}
		
		@Override
		public void visit(ForUpdateClauseBuilder<?> clauseBuilder) {
			doVisit(clauseBuilder);
		}
		
		@Override
		public void visit(ForShareClauseBuilder<?> clauseBuilder) {
			doVisit(clauseBuilder);
		}
		
	}
	
	public abstract static class ParametersClauseVisitor extends SelectClauseVisitor {

		private final ParametersConditionVisitor conditionVisitor = new ParametersConditionVisitor() {
			@Override
			public void doParameters(Parameters p) {
				ParametersClauseVisitor.this.doParameters(p);
			}
		};
		
		@Override
		public void visit(SelectWhereClauseBuilder<?> whereClauseBuilder) {
			whereClauseBuilder.getCondition().accept(conditionVisitor);
		}

		@Override
		public void visit(LimitClauseBuilder<?> limitClauseBuilder) {
			doParameters(limitClauseBuilder);
		}
		
		@Override
		public void visit(OrderByClauseBuilder<?> clauseBuilder) {
			doParameters(clauseBuilder);
		}

		@Override
		public void visit(OffsetClauseBuilder<?> clauseBuilder) {
			doParameters(clauseBuilder);
		}

		@Override
		public void visit(SelectCustomClauseBuilder<?> clauseBuilder) {
			doParameters(clauseBuilder);
		}
		
		@Override
		public void visit(ForUpdateClauseBuilder<?> clauseBuilder) {
			//NOOP
		}
		@Override
		public void visit(ForShareClauseBuilder<?> clauseBuilder) {
			//NOOP
		}

		public abstract void doParameters(Parameters p);
		
	}
	
	public static MutableParameters getParameters(SelectVisitorAcceptor k) {
		final MutableParameters mp = new MutableParameters();
		new ParametersClauseVisitor() {
			@Override
			public void doParameters(Parameters p) {
				mp.addAll(p);
			}
		}.startOn(k);
		return mp;
	}
	
	/*
	 * TODO move this to writer package.
	 */
	public static SelectClauseVisitor clauseVisitor(final Appendable appendable) {

		final SafeAppendable sb = new SafeAppendable(appendable);
		
		SimpleClauseVisitor cv = new SimpleClauseVisitor() {
			
			AtomicBoolean first = new AtomicBoolean(true);
			
			/*
			 * TODO take care of multiples and order.
			 */
			
			@Override
			protected void doVisit(SqlSelectClause<?> clause) {
				if (! appendStart(clause)) return;
				sb.append(clause.getSql());
			}
			
			@Override
			public void visit(SelectWhereClauseBuilder<?> whereClauseBuilder) {
				if (! appendStart(whereClauseBuilder)) return;
				whereClauseBuilder.getCondition().accept(ConditionVisitors.conditionVisitor(sb.getAppendable()));
			}
			
			@Override
			public void visit(OrderByClauseBuilder<?> orderClauseBuilder) {
				if ( ! appendStart(orderClauseBuilder) ) return;
				if (nullToEmpty(orderClauseBuilder.getSql()).trim().isEmpty())
					OrderByPartialVisitors.visitor(sb.getAppendable()).visit(orderClauseBuilder.getOrderByPartial());
				else
					sb.append(orderClauseBuilder.getSql());
			}

			private boolean appendStart(SelectClause<?> k) {
				if (k.isNoOp()) return false;
				if (first.get() ) {
					first.set(false);
				}
				else {
					sb.append(" ");
				}
				if (k.getType() != SelectClauseType.CUSTOM) {
					sb.append(k.getType().getSql());
					if (k.getType() != SelectClauseType.FORUPDATE && 
							k.getType() != SelectClauseType.FORSHARE)
						sb.append(" ");
				}
				return true;
			}

		};
		return cv;
	}

}
