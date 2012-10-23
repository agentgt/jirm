package co.jirm.mapper;

import java.util.Map;

import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.util.JirmPrecondition;
import co.jirm.mapper.converter.SqlObjectConverter;
import co.jirm.mapper.definition.SqlObjectDefinition;


public class SqlObjectExecutorRowMapper<T> implements SqlExecutorRowMapper<T> {
	
	private final SqlObjectDefinition<T> objectDefinition;
	private final SqlObjectConverter objectConverter;
	
	
	private SqlObjectExecutorRowMapper(
			SqlObjectDefinition<T> objectDefinition, 
			SqlObjectConverter objectConverter) {
		super();
		this.objectDefinition = objectDefinition;
		this.objectConverter = objectConverter;
	}

	@Override
	public Class<T> getObjectType() {
		return objectDefinition.getObjectType();
	}

	@Override
	public T mapRow(Map<String, Object> m, int rowNum) {
		for (String simpleParameter : objectDefinition.getSimpleParameters().keySet()) {
			JirmPrecondition.check.state(m.containsKey(simpleParameter), 
					"For type: {} simpleParameter: {} was not in resultset", 
					objectDefinition.getObjectType(), simpleParameter);
		}
		return objectConverter.convertSqlMapToObject(m, objectDefinition.getObjectType());
	}
	

}
