package co.jirm.orm.builder;



public class CustomClauseBuilder<I> extends AbstractSqlParameterizedClause<CustomClauseBuilder<I>, I> {
	
	
	private CustomClauseBuilder(Clause<I> parent, String sql) {
		super(parent, ClauseType.CUSTOM, sql);
	}
	
	public static <I> CustomClauseBuilder<I> newCustomClause(Clause<I> clause, String sql) {
		return new CustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected CustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends ClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
