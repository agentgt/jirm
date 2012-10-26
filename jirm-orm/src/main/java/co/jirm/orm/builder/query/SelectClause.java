package co.jirm.orm.builder.query;

import co.jirm.core.sql.Parameters;


public interface SelectClause<I> extends Parameters, SelectVisitorAcceptor {
	
	public SelectClauseType getType();
	
	public boolean isNoOp();
	
	public I query();
	
	public interface SelectClauseTransform<K extends SelectClause<I>, I> {
		I transform(K clause);
	}

}
