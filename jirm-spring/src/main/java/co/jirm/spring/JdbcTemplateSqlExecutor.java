package co.jirm.spring;

import java.util.List;

import org.springframework.jdbc.core.JdbcOperations;

import com.google.common.base.Optional;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlExecutorRowMapper;


public class JdbcTemplateSqlExecutor implements SqlExecutor {
	
	private final JdbcOperations jdbcTemplate;

	private JdbcTemplateSqlExecutor(JdbcOperations jdbcTemplate) {
		super();
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public <T> T queryForObject(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException("queryForObject is not yet supported");
	}

	@Override
	public <T> Optional<T> queryForOptional(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException("queryForOptional is not yet supported");
	}

	@Override
	public <T> List<T> queryForList(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException("queryForList is not yet supported");
	}

	@Override
	public long queryForLong(String sql, Object[] objects) {
		// TODO Auto-generated method stub
		//return 0;
		throw new UnsupportedOperationException("queryForLong is not yet supported");
	}

	@Override
	public int queryForInt(String sql, Object[] objects) {
		// TODO Auto-generated method stub
		//return 0;
		throw new UnsupportedOperationException("queryForInt is not yet supported");
	}

	@Override
	public int[] batchUpdate(String sql, List<Object[]> objects) {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException("batchUpdate is not yet supported");
	}

	@Override
	public int update(String sql, Object[] objects) {
		// TODO Auto-generated method stub
		//return 0;
		throw new UnsupportedOperationException("update is not yet supported");
	}
	
	

}
