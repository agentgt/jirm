package co.jirm.mapper.converter;

import co.jirm.mapper.definition.SqlParameterDefinition;


public interface SqlParameterConverter {

	public Object convertToSql(Object object, SqlParameterDefinition parameterDefinition );
	
}
