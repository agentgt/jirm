package co.jirm.orm;

import co.jirm.orm.dao.JirmDao;


public interface JirmFactory {
	
	public <K> JirmDao<K> daoFor(Class<K> k);

}
