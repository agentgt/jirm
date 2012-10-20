package co.jirm.core.builder;

import com.google.common.base.Function;

import co.jirm.core.sql.MutableParameterizedSql;


public abstract class AbstractSqlQueryBuilder<B, H> 
	extends MutableParameterizedSql<B> implements QueryHandoff<H>{

	private final Function<B, H> handoffFunction;
	
	protected AbstractSqlQueryBuilder(String sql, Function<B, H> handOffFunction) {
		super(sql);
		this.handoffFunction = handOffFunction;
	}

	@Override
	public H query() {
		return handoffFunction.apply(getSelf());
	}

}
