package co.jirm.orm.builder;

import java.util.concurrent.atomic.AtomicBoolean;

import co.jirm.core.sql.Parameters;
import co.jirm.core.util.SafeAppendable;
import co.jirm.orm.builder.ClauseVisitor.SimpleClauseVisitor;
import co.jirm.orm.builder.query.WhereClauseBuilder;

import com.google.common.collect.ImmutableList;


public class Visitors {
	
	public static ClauseVisitor clauseVisitor(final Appendable appendable) {

		final SafeAppendable sb = new SafeAppendable(appendable);
		
		SimpleClauseVisitor cv = new SimpleClauseVisitor() {
			
			AtomicBoolean first = new AtomicBoolean(true);
			
			/*
			 * TODO take care of multiples and order.
			 */
			
			@Override
			protected void doVisit(SqlClause<?> clause) {
				if (! appendStart(clause)) return;
				sb.append(clause.getSql());
			}
			
			@Override
			public void visit(WhereClauseBuilder<?> whereClauseBuilder) {
				if (! appendStart(whereClauseBuilder)) return;
				whereClauseBuilder.getCondition().accept(conditionVisitor(sb.getAppendable()));
			}
			
			private boolean appendStart(Clause<?> k) {
				if (k.isNoOp()) return false;
				if (first.get() ) {
					first.set(false);
				}
				else {
					sb.append(" ");
				}
				if (k.getType() != ClauseType.CUSTOM)
					sb.append(k.getType().getSql()).append(" ");
				return true;
			}

		};
		return cv;
	}
	public static ConditionVisitor conditionVisitor(final Appendable appendable) {
		
		final SafeAppendable sb = new SafeAppendable(appendable);
		
		ConditionVisitor v = new ConditionVisitor() {
			private int depth = 0;
			
			@Override
			public void visitAnd(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
				depth++;
				doCondition(" AND ", conditions, parameters);
				
			}
			@Override
			public void visitOr(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
				depth++;
				doCondition(" OR ", conditions, parameters);
			}
			
			private void doCondition(String op, ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
				if (conditions.size() == 1) {
					sb.append(op);
					conditions.get(0).accept(this);
					
				}
				else {
					boolean top = depth == 1;
					if (! top)
						sb.append("( ");
					boolean first = true;
					for (ImmutableCondition c : conditions) {
						if (first) { 
							first = false; 
							c.accept(this);
						}
						else {
							sb.append(op);
							c.accept(this);
						}
						
					}
					if (! top)
						sb.append(" )");
				}
			}

			@Override
			public void visitSql(String sql, Parameters parameters) {
				sb.append(sql);
			}
		};
		return v;
	}
}
