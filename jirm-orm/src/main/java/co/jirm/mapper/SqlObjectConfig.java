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

import co.jirm.mapper.converter.DefaultParameterConverter;
import co.jirm.mapper.converter.JacksonSqlObjectConverter;
import co.jirm.mapper.converter.SqlObjectConverter;
import co.jirm.mapper.converter.SqlParameterConverter;
import co.jirm.mapper.definition.DefaultNamingStrategy;
import co.jirm.mapper.definition.NamingStrategy;
import co.jirm.mapper.definition.SqlObjectDefinition;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class SqlObjectConfig {
	
	private final NamingStrategy namingStrategy;
	private final SqlParameterConverter converter;
	private final SqlObjectConverter objectMapper;
	private final transient Cache<Class<?>, SqlObjectDefinition<?>> cache;
	private final int maximumLoadDepth = 4;
	
	protected SqlObjectConfig(
			NamingStrategy namingStrategy, 
			SqlObjectConverter objectMapper, 
			SqlParameterConverter converter, 
			Cache<Class<?>, SqlObjectDefinition<?>> cache) {
		super();
		this.namingStrategy = namingStrategy;
		this.converter = converter;
		this.objectMapper = objectMapper;
		this.cache = cache;
	}
	
	
	public NamingStrategy getNamingStrategy() {
		return namingStrategy;
	}
	
	public SqlParameterConverter getConverter() {
		return converter;
	}
	
	public SqlObjectConverter getObjectMapper() {
		return objectMapper;
	}
	
	public <T> SqlObjectDefinition<T> resolveObjectDefinition(Class<T> objectType) {
		return SqlObjectDefinition.fromClass(objectType, this);
	}
	
	
	public int getMaximumLoadDepth() {
		return maximumLoadDepth;
	}
	
	public Cache<Class<?>, SqlObjectDefinition<?>> getCache() {
		return cache;
	}
	
	public static SqlObjectConfig DEFAULT = 
			new SqlObjectConfig(DefaultNamingStrategy.INSTANCE,
			new JacksonSqlObjectConverter(), new DefaultParameterConverter(), 
			CacheBuilder.newBuilder()
			.weakKeys()
			.maximumSize(1000)
			.<Class<?>, SqlObjectDefinition<?>>build());

}
