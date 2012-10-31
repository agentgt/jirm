package co.jirm.orm.builder.select;




public class SelectCustomClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<SelectCustomClauseBuilder<I>, I> {
	
	
	private SelectCustomClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.CUSTOM, sql);
	}
	
	public static <I> SelectCustomClauseBuilder<I> newInstance(SelectClause<I> clause, String sql) {
		return new SelectCustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected SelectCustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
