package co.jirm.mapper.converter;

import java.util.LinkedHashMap;
import java.util.Map;


public interface SqlObjectConverter {
	
	<T> LinkedHashMap<String, Object> convertObjectToSqlMap(T object);
	
	<T> T convertSqlMapToObject(Map<String, Object> m, Class<T> type);
	
}
