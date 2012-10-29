package co.jirm.orm.builder.update;




public class UpdateCustomClauseBuilder<I> extends AbstractSqlParameterizedUpdateClause<UpdateCustomClauseBuilder<I>, I> {
	
	
	private UpdateCustomClauseBuilder(UpdateClause<I> parent, String sql) {
		super(parent, UpdateClauseType.CUSTOM, sql);
	}
	
	public static <I> UpdateCustomClauseBuilder<I> newInstance(UpdateClause<I> clause, String sql) {
		return new UpdateCustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected UpdateCustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
