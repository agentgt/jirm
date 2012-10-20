package co.jirm.core.execute;

import java.util.List;

import com.google.common.base.Optional;


public interface SqlQueryExecutor {
	
	<T> T queryForObject(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects);
	
	<T> Optional<T> queryForOptional(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects);
	
	<T> List<T> queryForList(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects);
	
	long queryForLong(String sql, Object[] objects);
	
	int queryForInt(String sql, Object[] objects);
}
