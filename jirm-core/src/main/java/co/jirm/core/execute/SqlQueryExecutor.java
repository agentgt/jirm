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
package co.jirm.core.execute;

import java.util.List;

import com.google.common.base.Optional;


public interface SqlQueryExecutor {
	
	<T> T queryForObject(String sql, SqlMultiValueRowMapper<T> rowMapper, Object[] objects);
	
	<T> Optional<T> queryForOptional(String sql, SqlMultiValueRowMapper<T> rowMapper, Object[] objects);
	
	<T> List<T> queryForList(String sql, SqlMultiValueRowMapper<T> rowMapper, Object[] objects);
	
	<T> T queryForObject(String sql, SqlSingleValueRowMapper rowMapper, Class<T> type, Object[] objects);
	
	<T> Optional<T> queryForOptional(String sql, SqlSingleValueRowMapper rowMapper, Class<T> type, Object[] objects);
	
	<T> List<T> queryForList(String sql, SqlSingleValueRowMapper rowMapper, Class<T> type, Object[] objects);
	
//	Map<String, Object> queryForObjectMap(
//			String sql, 
//			Class<?> k, 
//			Object[] objects);
//	
//	Optional<Map<String, Object>> queryForObjectMapOptional(
//			String sql, 
//			Class<?> k, 
//			Object[] objects);
//
//	List<Map<String, Object>> queryForObjectMapList(
//			String sql, 
//			Class<?> k, 
//			Object[] objects);
	
	long queryForLong(String sql, Object[] objects);
	
	int queryForInt(String sql, Object[] objects);
}
