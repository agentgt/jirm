/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.jirm.spring;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlSingleValueRowMapper;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.mapper.jdbc.JdbcResultSetRowMapper;
import co.jirm.mapper.jdbc.JdbcSqlObjectQueryExecutor;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;


public class SpringJdbcSqlExecutor extends JdbcSqlObjectQueryExecutor implements SqlExecutor {
	
	private final JdbcOperations jdbcTemplate;

	private SpringJdbcSqlExecutor(SqlObjectConfig config, JdbcOperations jdbcTemplate) {
		super(config);
		this.jdbcTemplate = jdbcTemplate;
	}
	
	public static SpringJdbcSqlExecutor newInstance(SqlObjectConfig config, JdbcOperations jdbcTemplate) {
		return new SpringJdbcSqlExecutor(config, jdbcTemplate);
	}

	@Override
	protected long doQueryForLong(String sql, Object[] objects) {
		return jdbcTemplate.queryForLong(sql, objects);
	}

	@Override
	protected int doQueryForInt(String sql, Object[] objects) {
		return jdbcTemplate.queryForInt(sql, objects);
	}

	@Override
	protected <T> T doQueryForObject(String sql, final JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		return jdbcTemplate.queryForObject(sql, new RowMapper<T> () {
			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rowMapper.mapRow(rs, rowNum);
			}		
		}, objects);
	}

	@Override
	protected <T> Optional<T> doQueryForOptional(String sql, final JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
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
	protected <T> List<T> doQueryForList(String sql, final JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		return jdbcTemplate.query(sql, objects, new RowMapper<T> () {
			@Override
			public T mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rowMapper.mapRow(rs, rowNum);
			}		
		});
	}
	
	
	@Override
	protected <T> T doQueryForObject(String sql, SqlSingleValueRowMapper rowMapper, Class<T> type, Object[] objects) {
		T o = jdbcTemplate.queryForObject(sql, objects, type);
		return rowMapper.mapRow(type, o, 0);
	}

	@Override
	protected <T> Optional<T> doQueryForOptional(String sql, SqlSingleValueRowMapper rowMapper, Class<T> type,
			Object[] objects) {
		try {
			T o = jdbcTemplate.queryForObject(sql, objects, type);
			return Optional.fromNullable(rowMapper.mapRow(type, o, 0));
		} catch (EmptyResultDataAccessException e) {
			return Optional.absent();
		}
	}

	@Override
	protected <T> List<T> doQueryForList(final String sql, final SqlSingleValueRowMapper rowMapper, final Class<T> type, Object[] objects) {
		List<T> list = jdbcTemplate.queryForList(sql, objects, type);
		return Lists.transform(list, new Function<T, T>() {
			int i = 0;
			public T apply(T input) {
				return rowMapper.mapRow(type, input, i++); 
			};
		});
	}

	@Override
	public int[] batchUpdate(String sql, List<Object[]> objects) {
		for (Object[] os : objects) {
			nullify(os);
		}
		return jdbcTemplate.batchUpdate(sql, objects);
	}

	@Override
	public int update(String sql, Object[] objects) {
		logSql(sql, objects);
		nullify(objects);
		return jdbcTemplate.update(sql, objects);
	}

}
