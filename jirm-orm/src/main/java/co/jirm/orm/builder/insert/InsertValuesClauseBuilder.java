package co.jirm.orm.builder.insert;

import java.util.Map;



public class InsertValuesClauseBuilder<I> extends AbstractSqlParameterizedInsertClause<InsertValuesClauseBuilder<I>, I>{

	protected InsertValuesClauseBuilder(InsertClause<I> parent, String sql) {
		super(parent, InsertClauseType.VALUES, sql);
	}
	
	static <I> InsertValuesClauseBuilder<I> newInstance(InsertClause<I> parent, Map<String,Object> o) {
		return new InsertValuesClauseBuilder<I>(parent, "");
	}

	@Override
	public <C extends InsertClauseVisitor> C accept(C visitor) {
		visitor.visit(getSelf());
		for (InsertClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

	@Override
	protected InsertValuesClauseBuilder<I> getSelf() {
		return this;
	}
}
