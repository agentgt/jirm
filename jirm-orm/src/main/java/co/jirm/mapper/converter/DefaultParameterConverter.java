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

import static co.jirm.core.util.JirmPrecondition.check;

import java.sql.Date;
import java.util.Calendar;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.joda.time.DateTime;

import co.jirm.core.util.ObjectMapUtils;
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
		else if (parameterDefinition.getEnumerated().isPresent() 
				&& Enum.class.isAssignableFrom(parameterDefinition.getParameterType())) {
			Enumerated anno = check.notNull(parameterDefinition.getEnumerated().orNull(),
					"Required @Enumerated annotation for enum arguments: {}", 
					parameterDefinition.getParameterName());
			if (original instanceof String) {
				if (anno.value() == EnumType.ORDINAL) {
					return ObjectMapUtils.enumFrom(parameterDefinition.getParameterType(), 
							(String)original).ordinal();
				}
				else {
					return original;
				}
			}
			else if (original instanceof Integer) {
				if (anno.value() == EnumType.STRING) {
					return ObjectMapUtils.enumFrom(parameterDefinition.getParameterType(), 
							(Integer)original).name();
				}
				else {
					return original;
				}				
			}
			else if (original.getClass().isAssignableFrom(parameterDefinition.getParameterType())) {
				Enum<?> e = (Enum<?>) original;
				if (anno.value() == EnumType.ORDINAL) {
					return e.ordinal();
				}
				else {
					return e.name();
				}
			}
			else {
				throw check.argumentInvalid("enum object: {} is not valid for: {}", 
						original, parameterDefinition.getParameterName());
			}
		}
		else {
			return original;
		}
	}

}
