package co.jirm.core.builder;

import com.google.common.base.Function;


public class SqlTypedQueryForBuilder<QF> extends AbstractSqlQueryBuilder<SqlTypedQueryForBuilder<QF>, QF> {

	protected SqlTypedQueryForBuilder(
			String sql, 
			Function<SqlTypedQueryForBuilder<QF>, QF> handOffFunction) {
		super(sql, handOffFunction);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected SqlTypedQueryForBuilder<QF> getSelf() {
		return this;
	}

}
