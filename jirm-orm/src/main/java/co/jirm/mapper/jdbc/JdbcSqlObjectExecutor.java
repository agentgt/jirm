package co.jirm.mapper.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.execute.SqlQueryExecutor;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.mapper.definition.SqlObjectDefinition;

import com.google.common.base.Optional;


public abstract class JdbcSqlObjectExecutor implements SqlQueryExecutor {

	
	private final SqlObjectConfig objectConfig;
	private final JdbcResultSetMapperHelper helper = new JdbcResultSetMapperHelper();
	
	private JdbcSqlObjectExecutor(SqlObjectConfig objectConfig) {
		super();
		this.objectConfig = objectConfig;
	}

	protected <T> JdbcResultSetRowMapper<T> createJdbcMapper(final SqlExecutorRowMapper<T> rowMapper) {
		final SqlObjectDefinition<T> objectDefinition = 
				objectConfig.resolveObjectDefinition(rowMapper.getObjectType());
		return new JdbcResultSetRowMapper<T>() {

			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				Map<String,Object> m = helper.mapRow(objectDefinition, rs, rowNum);
				return rowMapper.mapRow(m, rowNum);
			}
		};
	}

	@Override
	public <T> T queryForObject(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		return queryForObject(sql, createJdbcMapper(rowMapper), objects);
	}

	@Override
	public <T> Optional<T> queryForOptional(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		return queryForOptional(sql, createJdbcMapper(rowMapper), objects);
	}
	
	@Override
	public <T> List<T> queryForList(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		return queryForList(sql, createJdbcMapper(rowMapper), objects);
	}
	
	
	public abstract <T> T queryForObject(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects);

	public abstract <T> Optional<T> queryForOptional(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects);
	
	public abstract <T> List<T> queryForList(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects);
	
}
