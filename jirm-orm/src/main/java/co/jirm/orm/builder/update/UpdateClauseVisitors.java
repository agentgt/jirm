package co.jirm.orm.builder.update;

import java.util.concurrent.atomic.AtomicBoolean;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.core.util.SafeAppendable;
import co.jirm.orm.builder.ConditionVisitors;
import co.jirm.orm.builder.ConditionVisitors.ParametersConditionVisitor;


public class UpdateClauseVisitors {
	
	public abstract static class SimpleClauseVisitor extends UpdateClauseVisitor {

		protected abstract void doVisit(SqlUpdateClause<?> clause);
		
		
		@Override
		public void visit(SetClauseBuilder<?> set) {
			doVisit(set);
		}
		
		@Override
		public void visit(UpdateCustomClauseBuilder<?> custom) {
			doVisit(custom);
		}


		@Override
		public abstract void visit(UpdateWhereClauseBuilder<?> whereClauseBuilder);
		
	}
	
	public abstract static class ParametersClauseVisitor extends UpdateClauseVisitor {

		private final ParametersConditionVisitor conditionVisitor = new ParametersConditionVisitor() {
			@Override
			public void doParameters(Parameters p) {
				ParametersClauseVisitor.this.doParameters(p);
			}
		};
		
		@Override
		public void visit(UpdateWhereClauseBuilder<?> where) {
			where.getCondition().accept(conditionVisitor);

		}

		@Override
		public void visit(SetClauseBuilder<?> set) {
			doParameters(set);
		}

		@Override
		public void visit(UpdateCustomClauseBuilder<?> custom) {
			doParameters(custom);
		}

		public abstract void doParameters(Parameters p);
		
	}
	
	public static MutableParameters getParameters(UpdateVisitorAcceptor k) {
		final MutableParameters mp = new MutableParameters();
		new ParametersClauseVisitor() {
			@Override
			public void doParameters(Parameters p) {
				mp.addAll(p);
			}
		}.startOn(k);
		return mp;
	}
	
	public static UpdateClauseVisitor clauseVisitor(final Appendable appendable) {

		final SafeAppendable sb = new SafeAppendable(appendable);
		
		SimpleClauseVisitor cv = new SimpleClauseVisitor() {
			
			AtomicBoolean first = new AtomicBoolean(true);
			
			/*
			 * TODO take care of multiples and order.
			 */
			
			@Override
			protected void doVisit(SqlUpdateClause<?> clause) {
				if (! appendStart(clause)) return;
				sb.append(clause.getSql());
			}
			
			@Override
			public void visit(UpdateWhereClauseBuilder<?> whereClauseBuilder) {
				if (! appendStart(whereClauseBuilder)) return;
				whereClauseBuilder.getCondition().accept(ConditionVisitors.conditionVisitor(sb.getAppendable()));
			}
			
			private boolean appendStart(UpdateClause<?> k) {
				if (k.isNoOp()) return false;
				if (first.get() ) {
					first.set(false);
				}
				else {
					sb.append(" ");
				}
				if (k.getType() != UpdateClauseType.CUSTOM)
					sb.append(k.getType().getSql()).append(" ");
				return true;
			}

		};
		return cv;
	}
}
