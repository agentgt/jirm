package co.jirm.orm.builder.query;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.orm.builder.ConditionVisitor.ParametersConditionVisitor;


public abstract class SelectClauseVisitor {
	
	public abstract void visit(WhereClauseBuilder<?> whereClauseBuilder);
	public abstract void visit(OrderByClauseBuilder<?> clauseBuilder);
	public abstract void visit(LimitClauseBuilder<?> limitClauseBuilder);
	public abstract void visit(OffsetClauseBuilder<?> clauseBuilder);
	public abstract void visit(CustomClauseBuilder<?> clauseBuilder);

	
	
	public Object startOn(SelectVisitorAcceptor klause) {
		klause.accept(this);
		return this;
	}
	
	
	public abstract static class SimpleClauseVisitor extends SelectClauseVisitor {

		protected abstract void doVisit(SqlSelectClause<?> clause);
		
		@Override
		public abstract void visit(WhereClauseBuilder<?> whereClauseBuilder);

		@Override
		public void visit(OrderByClauseBuilder<?> clauseBuilder) {
			doVisit(clauseBuilder);
		}

		@Override
		public void visit(LimitClauseBuilder<?> limitClauseBuilder) {
			doVisit(limitClauseBuilder);
		}

		@Override
		public void visit(OffsetClauseBuilder<?> clauseBuilder) {
			doVisit(clauseBuilder);
		}

		@Override
		public void visit(CustomClauseBuilder<?> clauseBuilder) {
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
		public void visit(WhereClauseBuilder<?> whereClauseBuilder) {
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
		public void visit(CustomClauseBuilder<?> clauseBuilder) {
			doParameters(clauseBuilder);
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
	
}
