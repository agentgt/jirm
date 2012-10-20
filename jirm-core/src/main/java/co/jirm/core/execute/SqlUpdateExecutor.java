package co.jirm.core.execute;

import java.util.List;


public interface SqlUpdateExecutor {

	int[] batchUpdate(String sql, List<Object[]> objects);
	
	int update(String sql, Object[] objects);

}
