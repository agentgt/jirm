package co.jirm.spring;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import co.jirm.mapper.SqlObjectConfig;
import co.jirm.mapper.jdbc.JdbcResultSetRowMapper;
import co.jirm.mapper.jdbc.JdbcSqlObjectExecutor;

import com.google.common.base.Optional;


public class JdbcTemplateSqlExecutor extends JdbcSqlObjectExecutor {
	
	private final JdbcOperations jdbcTemplate;

	private JdbcTemplateSqlExecutor(SqlObjectConfig config, JdbcOperations jdbcTemplate) {
		super(config);
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public long queryForLong(String sql, Object[] objects) {
		return jdbcTemplate.queryForLong(sql, objects);
	}

	@Override
	public int queryForInt(String sql, Object[] objects) {
		return jdbcTemplate.queryForInt(sql, objects);
	}

	@Override
	public <T> T queryForObject(String sql, final JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		return jdbcTemplate.queryForObject(sql, new RowMapper<T> () {
			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rowMapper.mapRow(rs, rowNum);
			}		
		}, objects);
	}

	@Override
	public <T> Optional<T> queryForOptional(String sql, final JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		try {
			T object = jdbcTemplate.queryForObject(sql, new RowMapper<T> () {
				@Override
				public T mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rowMapper.mapRow(rs, rowNum);
				}		
			}, objects);
			return Optional.of(object);
		} catch (EmptyResultDataAccessException e) {
			return Optional.absent();
		}
	}

	@Override
	public <T> List<T> queryForList(String sql, final JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		return jdbcTemplate.query(sql, new RowMapper<T> () {
			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rowMapper.mapRow(rs, rowNum);
			}		
		}, objects);
	}
	
	

}
