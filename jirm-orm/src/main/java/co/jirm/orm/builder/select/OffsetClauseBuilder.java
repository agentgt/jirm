package co.jirm.orm.builder.select;




public class OffsetClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<OffsetClauseBuilder<I>, I> {
	
	private OffsetClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.OFFSET, sql);
	}
	
	static <I> OffsetClauseBuilder<I> newInstance(SelectClause<I> parent, String sql) {
		return new OffsetClauseBuilder<I>(parent, sql);
	}
	
	static <I> OffsetClauseBuilder<I> newInstanceWithOffset(SelectClause<I> parent, Number i) {
		return new OffsetClauseBuilder<I>(parent, "?").with(i.longValue());
	}

	@Override
	protected OffsetClauseBuilder<I> getSelf() {
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
