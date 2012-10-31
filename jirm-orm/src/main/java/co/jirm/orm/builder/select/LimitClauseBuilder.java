package co.jirm.orm.builder.select;




public class LimitClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<LimitClauseBuilder<I>, I> implements SelectVisitorAcceptor {
	
	private LimitClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.LIMIT, sql);
	}
	
	static <I> LimitClauseBuilder<I> newInstance(SelectClause<I> parent, String sql) {
		return new LimitClauseBuilder<I>(parent, sql);
	}
	
	static <I> LimitClauseBuilder<I> newInstanceWithLimit(SelectClause<I> parent, Number i) {
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
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		for (SelectClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
}
