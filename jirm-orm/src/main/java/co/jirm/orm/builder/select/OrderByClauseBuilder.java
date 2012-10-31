package co.jirm.orm.builder.select;




public class OrderByClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<OrderByClauseBuilder<I>, I> {
	
	private OrderByClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.ORDERBY, sql);
	}
	
	public static <I> OrderByClauseBuilder<I> newInstance(SelectClause<I> parent, String sql) {
		return new OrderByClauseBuilder<I>(parent, sql);
	}
	

	@Override
	protected OrderByClauseBuilder<I> getSelf() {
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
	
	public LimitClauseBuilder<I> limit(String sql) {
		return addClause(LimitClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(Number i) {
		return addClause(LimitClauseBuilder.newInstanceWithLimit(this, i));
	}
	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(getSelf(), sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(getSelf(), i));
	}
	
	
	
	
}
