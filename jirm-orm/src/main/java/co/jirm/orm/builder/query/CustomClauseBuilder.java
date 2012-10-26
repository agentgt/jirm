package co.jirm.orm.builder.query;




public class CustomClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<CustomClauseBuilder<I>, I> {
	
	
	private CustomClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.CUSTOM, sql);
	}
	
	public static <I> CustomClauseBuilder<I> newCustomClause(SelectClause<I> clause, String sql) {
		return new CustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected CustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
