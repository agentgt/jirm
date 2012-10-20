package co.jirm.core.execute;

import java.util.Map;


public interface SqlExecutorRowMapper<T> {
	T mapRow(Map<String, Object> row, int rowNum);
}
