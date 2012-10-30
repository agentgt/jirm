package co.jirm.orm.builder.delete;

import co.jirm.core.sql.Parameters;


public interface DeleteClause<I> extends Parameters, DeleteVisitorAcceptor {
	
	public DeleteClauseType getType();
	
	public boolean isNoOp();
	
	public I execute();
	
	public interface DeleteClauseTransform<K extends DeleteClause<I>, I> {
		I transform(K clause);
	}

}
