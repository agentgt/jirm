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
package co.jirm.mapper;

import java.util.Map;

import co.jirm.core.execute.SqlMultiValueRowMapper;
import static co.jirm.core.util.JirmPrecondition.check;
import co.jirm.mapper.converter.SqlObjectConverter;
import co.jirm.mapper.definition.SqlObjectDefinition;


public class SqlObjectExecutorRowMapper<T> implements SqlMultiValueRowMapper<T> {
	
	private final SqlObjectDefinition<T> objectDefinition;
	private final SqlObjectConverter objectConverter;
	
	
	private SqlObjectExecutorRowMapper(
			SqlObjectDefinition<T> objectDefinition, 
			SqlObjectConverter objectConverter) {
		super();
		this.objectDefinition = objectDefinition;
		this.objectConverter = objectConverter;
	}
	
	public static <T> SqlObjectExecutorRowMapper<T> newInstance(SqlObjectDefinition<T> definition, SqlObjectConverter objectConverter) {
		return new SqlObjectExecutorRowMapper<T>(definition, objectConverter);
	}

	@Override
	public Class<T> getObjectType() {
		return objectDefinition.getObjectType();
	}

	@Override
	public T mapRow(Map<String, Object> m, int rowNum) {
		for (String simpleParameter : objectDefinition.getSimpleParameters().keySet()) {
			check.state(m.containsKey(simpleParameter), 
					"For type: {} simpleParameter: {} was not in resultset", 
					objectDefinition.getObjectType(), simpleParameter);
		}
		return objectConverter.convertSqlMapToObject(m, objectDefinition.getObjectType());
	}
	

}
