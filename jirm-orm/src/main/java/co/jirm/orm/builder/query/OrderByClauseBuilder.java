package co.jirm.orm.builder.query;

import co.jirm.orm.builder.AbstractSqlParameterizedClause;
import co.jirm.orm.builder.Clause;
import co.jirm.orm.builder.ClauseType;
import co.jirm.orm.builder.ClauseVisitor;



public class OrderByClauseBuilder<I> extends AbstractSqlParameterizedClause<OrderByClauseBuilder<I>, I> {
	
	private OrderByClauseBuilder(Clause<I> parent, String sql) {
		super(parent, ClauseType.ORDERBY, sql);
	}
	
	public static <I> OrderByClauseBuilder<I> newInstance(Clause<I> parent, String sql) {
		return new OrderByClauseBuilder<I>(parent, sql);
	}
	

	@Override
	protected OrderByClauseBuilder<I> getSelf() {
		return this;
	}
	
	@Override
	public <C extends ClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		for (Clause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	public LimitClauseBuilder<I> limit(String sql) {
		return addClause(LimitClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(Number i) {
		return addClause(LimitClauseBuilder.newInstanceWithLimit(this, i));
	}
	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(getSelf(), sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(getSelf(), i));
	}
	
	
	
	
}
