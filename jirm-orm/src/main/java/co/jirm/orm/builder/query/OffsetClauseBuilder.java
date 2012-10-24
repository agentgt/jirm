package co.jirm.orm.builder.query;

import co.jirm.orm.builder.AbstractSqlParameterizedClause;
import co.jirm.orm.builder.Clause;
import co.jirm.orm.builder.ClauseType;
import co.jirm.orm.builder.ClauseVisitor;



public class OffsetClauseBuilder<I> extends AbstractSqlParameterizedClause<OffsetClauseBuilder<I>, I> {
	
	private OffsetClauseBuilder(Clause<I> parent, String sql) {
		super(parent, ClauseType.OFFSET, sql);
	}
	
	static <I> OffsetClauseBuilder<I> newInstance(Clause<I> parent, String sql) {
		return new OffsetClauseBuilder<I>(parent, sql);
	}
	
	static <I> OffsetClauseBuilder<I> newInstanceWithOffset(Clause<I> parent, Number i) {
		return new OffsetClauseBuilder<I>(parent, "?").with(i.longValue());
	}

	@Override
	protected OffsetClauseBuilder<I> getSelf() {
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
	
	
}
