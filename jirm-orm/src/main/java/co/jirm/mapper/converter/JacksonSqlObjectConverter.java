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
package co.jirm.mapper.converter;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.datatype.guava.GuavaModule;


public class JacksonSqlObjectConverter implements SqlObjectConverter {

	private final ObjectMapper objectMapper = new ObjectMapper();
	{
		objectMapper.registerModule(new GuavaModule());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> LinkedHashMap<String, Object> convertObjectToSqlMap(T object) {
		return objectMapper.convertValue(object, LinkedHashMap.class);
	}

	@Override
	public <T> T convertSqlMapToObject(Map<String, Object> m, Class<T> type) {
		return objectMapper.convertValue(m, type);
	}

}
