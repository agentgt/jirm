package co.jirm.orm.builder.update;


public class SetClauseBuilder<I> extends AbstractSqlParameterizedUpdateClause<SetClauseBuilder<I>, I> {

	private SetClauseBuilder(UpdateClause<I> parent, String sql) {
		super(parent, UpdateClauseType.SET, sql);
	}

	@Override
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		for (UpdateClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

	@Override
	protected SetClauseBuilder<I> getSelf() {
		return this;
	}

}
