package co.jirm.orm.builder.delete;




public class DeleteCustomClauseBuilder<I> extends AbstractSqlParameterizedDeleteClause<DeleteCustomClauseBuilder<I>, I> {
	
	
	private DeleteCustomClauseBuilder(DeleteClause<I> parent, String sql) {
		super(parent, DeleteClauseType.CUSTOM, sql);
	}
	
	public static <I> DeleteCustomClauseBuilder<I> newInstance(DeleteClause<I> clause, String sql) {
		return new DeleteCustomClauseBuilder<I>(clause, sql);
	}

	@Override
	protected DeleteCustomClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	@Override
	public <C extends DeleteClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		return visitor;
	}
	
	
}
