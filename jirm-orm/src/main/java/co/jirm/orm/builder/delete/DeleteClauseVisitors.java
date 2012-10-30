package co.jirm.orm.builder.delete;

import java.util.concurrent.atomic.AtomicBoolean;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.core.util.SafeAppendable;
import co.jirm.orm.builder.ConditionVisitors;
import co.jirm.orm.builder.ConditionVisitors.ParametersConditionVisitor;


public class DeleteClauseVisitors {
	
	public abstract static class SimpleClauseVisitor extends DeleteClauseVisitor {

		protected abstract void doVisit(SqlDeleteClause<?> clause);
		
		@Override
		public void visit(DeleteCustomClauseBuilder<?> custom) {
			doVisit(custom);
		}

		@Override
		public abstract void visit(DeleteWhereClauseBuilder<?> whereClauseBuilder);
		
	}
	
	public abstract static class ParametersClauseVisitor extends DeleteClauseVisitor {

		private final ParametersConditionVisitor conditionVisitor = new ParametersConditionVisitor() {
			@Override
			public void doParameters(Parameters p) {
				ParametersClauseVisitor.this.doParameters(p);
			}
		};
		
		@Override
		public void visit(DeleteWhereClauseBuilder<?> where) {
			where.getCondition().accept(conditionVisitor);

		}

		@Override
		public void visit(DeleteCustomClauseBuilder<?> custom) {
			doParameters(custom);
		}

		public abstract void doParameters(Parameters p);
		
	}
	
	public static MutableParameters getParameters(DeleteVisitorAcceptor k) {
		final MutableParameters mp = new MutableParameters();
		new ParametersClauseVisitor() {
			@Override
			public void doParameters(Parameters p) {
				mp.addAll(p);
			}
		}.startOn(k);
		return mp;
	}
	
	public static DeleteClauseVisitor clauseVisitor(final Appendable appendable) {

		final SafeAppendable sb = new SafeAppendable(appendable);
		
		SimpleClauseVisitor cv = new SimpleClauseVisitor() {
			
			AtomicBoolean first = new AtomicBoolean(true);
			
			/*
			 * TODO take care of multiples and order.
			 */
			
			@Override
			protected void doVisit(SqlDeleteClause<?> clause) {
				if (! appendStart(clause)) return;
				sb.append(clause.getSql());
			}
			
			@Override
			public void visit(DeleteWhereClauseBuilder<?> whereClauseBuilder) {
				if (! appendStart(whereClauseBuilder)) return;
				whereClauseBuilder.getCondition().accept(ConditionVisitors.conditionVisitor(sb.getAppendable()));
			}
			
			private boolean appendStart(DeleteClause<?> k) {
				if (k.isNoOp()) return false;
				if (first.get() ) {
					first.set(false);
				}
				else {
					sb.append(" ");
				}
				if (k.getType() != DeleteClauseType.CUSTOM)
					sb.append(k.getType().getSql()).append(" ");
				return true;
			}

		};
		return cv;
	}
}
