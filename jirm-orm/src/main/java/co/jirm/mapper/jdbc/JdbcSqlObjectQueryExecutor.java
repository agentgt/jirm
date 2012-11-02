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
package co.jirm.mapper.jdbc;

import static java.util.Arrays.asList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.execute.SqlQueryExecutor;
import co.jirm.core.util.ObjectMapUtils;
import co.jirm.mapper.SqlObjectConfig;
import co.jirm.mapper.definition.SqlObjectDefinition;

import com.google.common.base.Optional;


public abstract class JdbcSqlObjectQueryExecutor implements SqlQueryExecutor {

	
	private final SqlObjectConfig objectConfig;
	private final JdbcResultSetMapperHelper helper = new JdbcResultSetMapperHelper();
	
	protected JdbcSqlObjectQueryExecutor(SqlObjectConfig objectConfig) {
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
		logSql(sql, objects);
		return queryForObject(sql, createJdbcMapper(rowMapper), objects);
	}

	protected void logSql(String sql, Object[] objects) {
		if (log.isDebugEnabled()) {
			log.debug("sql: {}; values: {}", sql, ObjectMapUtils.toStringList(new StringBuilder(), asList(objects), 100));
		}
	}

	@Override
	public <T> Optional<T> queryForOptional(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		logSql(sql, objects);
		return queryForOptional(sql, createJdbcMapper(rowMapper), objects);
	}
	
	@Override
	public <T> List<T> queryForList(String sql, SqlExecutorRowMapper<T> rowMapper, Object[] objects) {
		logSql(sql, objects);
		return queryForList(sql, createJdbcMapper(rowMapper), objects);
	}
	
	public <T> T queryForObject(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		logSql(sql, objects);
		return doQueryForObject(sql, rowMapper, objects);
	}

	public <T> Optional<T> queryForOptional(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		logSql(sql, objects);
		return doQueryForOptional(sql, rowMapper, objects);
	}
	
	public <T> List<T> queryForList(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects) {
		logSql(sql, objects);
		return doQueryForList(sql, rowMapper, objects);
	}

	
	public abstract <T> T doQueryForObject(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects);

	public abstract <T> Optional<T> doQueryForOptional(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects);
	
	public abstract <T> List<T> doQueryForList(String sql, JdbcResultSetRowMapper<T> rowMapper, Object[] objects);
	
	
	private static final Logger log = LoggerFactory.getLogger(JdbcSqlObjectQueryExecutor.class);
	
}
