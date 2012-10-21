package co.jirm.mapper;

import java.sql.Date;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;

import co.jirm.mapper.converter.SqlObjectConverter;
import co.jirm.mapper.converter.SqlParameterConverter;
import co.jirm.mapper.definition.DefaultNamingStrategy;
import co.jirm.mapper.definition.NamingStrategy;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.mapper.definition.SqlParameterDefinition;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;


public class SqlObjectConfig {
	
	private final NamingStrategy namingStrategy;
	private final SqlParameterConverter converter;
	private final SqlObjectConverter objectMapper;
	private final transient Cache<String, SqlObjectDefinition<?>> cache;
	private final int maximumLoadDepth = 4;
	
	public SqlObjectConfig(
			NamingStrategy namingStrategy, 
			SqlObjectConverter objectMapper, 
			SqlParameterConverter converter, 
			Cache<String, SqlObjectDefinition<?>> cache) {
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
	
	public Cache<String, SqlObjectDefinition<?>> getCache() {
		return cache;
	}
	
	public static SqlObjectConfig DEFAULT = new SqlObjectConfig(DefaultNamingStrategy.INSTANCE,
			new SqlObjectConverter() {
				private final ObjectMapper objectMapper = new ObjectMapper();
				{
					objectMapper.registerModule(new GuavaModule());
				}
				@Override
				public <T> T convertSqlMapToObject(Map<String, Object> m, Class<T> type) {
					return objectMapper.convertValue(m, type);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> LinkedHashMap<String, Object> convertObjectToSqlMap(T object) {
					return objectMapper.convertValue(object, LinkedHashMap.class);
				}
			}, new SqlParameterConverter() {

				@Override
				public Object convertToSql(Object original, SqlParameterDefinition parameterDefinition) {
					if (Date.class.isAssignableFrom(parameterDefinition.getParameterType())) {
						return new DateTime(original).toDate();
					}
					else if (Calendar.class.isAssignableFrom(parameterDefinition.getParameterType())) {
						return new DateTime(original).toCalendar(null);
					}
					else {
						return original;
					}
				}
	}, CacheBuilder.newBuilder()
		.maximumSize(1000)
		.<String, SqlObjectDefinition<?>>build());

}
