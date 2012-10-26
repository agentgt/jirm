package co.jirm.orm.builder.update;

import co.jirm.core.sql.Parameters;


public interface UpdateClause<I> extends Parameters, UpdateVisitorAcceptor {
	
	public UpdateClauseType getType();
	
	public boolean isNoOp();
	
	public I query();
	
	public interface UpdateClauseTransform<K extends UpdateClause<I>, I> {
		I transform(K clause);
	}

}
