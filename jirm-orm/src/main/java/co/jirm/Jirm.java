package co.jirm;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.orm.dao.DaoTemplate;
import co.jirm.orm.query.QueryObjectTemplate;


public class Jirm {
	private final SqlExecutor sqlExecutor;
	private final SqlObjectConfig sqlObjectConfig;
	
	
	
	private Jirm(SqlExecutor sqlExecutor, SqlObjectConfig sqlObjectConfig) {
		super();
		this.sqlExecutor = sqlExecutor;
		this.sqlObjectConfig = sqlObjectConfig;
	}

	public static <T> DaoTemplate<T> daoFor(Class<T> k) {
		return null;
		
	}
	
	public static <T> QueryObjectTemplate<T> queryTemplate() {
		return null;
	}
	

}
