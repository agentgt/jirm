package co.jirm.orm.builder.query;

import co.jirm.orm.builder.AbstractSqlParameterizedClause;
import co.jirm.orm.builder.Clause;
import co.jirm.orm.builder.ClauseType;
import co.jirm.orm.builder.ClauseVisitor;



public class LimitClauseBuilder<I> extends AbstractSqlParameterizedClause<LimitClauseBuilder<I>, I> {
	
	private LimitClauseBuilder(Clause<I> parent, String sql) {
		super(parent, ClauseType.LIMIT, sql);
	}
	
	static <I> LimitClauseBuilder<I> newInstance(Clause<I> parent, String sql) {
		return new LimitClauseBuilder<I>(parent, sql);
	}
	
	static <I> LimitClauseBuilder<I> newInstanceWithLimit(Clause<I> parent, Number i) {
		return new LimitClauseBuilder<I>(parent, "?").with(i.longValue());
	}

	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(getSelf(), sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(getSelf(), i));
	}
	
	@Override
	protected LimitClauseBuilder<I> getSelf() {
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
