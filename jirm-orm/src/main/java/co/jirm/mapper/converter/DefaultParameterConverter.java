package co.jirm.mapper.converter;

import java.sql.Date;
import java.util.Calendar;

import org.joda.time.DateTime;

import co.jirm.mapper.definition.SqlParameterDefinition;


public class DefaultParameterConverter implements SqlParameterConverter {

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

}
