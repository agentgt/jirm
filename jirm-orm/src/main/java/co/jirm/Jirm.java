package co.jirm;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.orm.dao.DaoTemplate;
import co.jirm.orm.query.QueryObjectTemplate;


public class Jirm {
	private final SqlExecutor sqlExecutor;
	private final SqlObjectConfig sqlObjectConfig;
	
	public static <T> DaoTemplate<T> daoFor(Class<T> k) {
		
	}
	
	public static <T> QueryObjectTemplate<T> queryTemplate() {
		
	}
	

}
