package co.jirm.orm.builder;

import co.jirm.core.sql.MutableParameterized;

public abstract class AbstractParameterizedClause<I, T> extends MutableParameterized<T> implements Clause<I> {
	
	private final ClauseType type;

	protected AbstractParameterizedClause(ClauseType type) {
		super();
		this.type = type;
	}
	
	public ClauseType getType() {
		return type;
	}

}
