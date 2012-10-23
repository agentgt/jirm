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
	private final transient Cache<String, SqlObjectDefinition<?>> cache;
	private final int maximumLoadDepth = 4;
	
	private SqlObjectConfig(
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
	
	public static SqlObjectConfig DEFAULT = 
			new SqlObjectConfig(DefaultNamingStrategy.INSTANCE,
			new JacksonSqlObjectConverter(), new DefaultParameterConverter(), CacheBuilder.newBuilder()
			.maximumSize(1000)
			.<String, SqlObjectDefinition<?>>build());

}
