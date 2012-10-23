package co.jirm.mapper.converter;

import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

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
