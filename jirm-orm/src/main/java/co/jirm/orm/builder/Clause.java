package co.jirm.orm.builder;

import co.jirm.core.sql.Parameters;
import co.jirm.orm.builder.ClauseVisitor.Acceptor;


public interface Clause<I> extends Parameters, Acceptor {
	
	public ClauseType getType();
	
	public boolean isNoOp();
	
	public I query();
	
	public interface ClauseTransform<K extends Clause<I>, I> {
		I transform(K clause);
	}

}
