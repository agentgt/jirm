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
package co.jirm.mapper.jdbc;

import static co.jirm.core.util.JirmPrecondition.check;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.jirm.core.util.ObjectMapUtils;
import co.jirm.mapper.definition.SqlObjectDefinition;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;


public class JdbcResultSetMapperHelper {
	
	private final static Splitter splitter = Splitter.on(".");
	
	public Map<String, Object> mapRow(
			SqlObjectDefinition<?> definition, 
			ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Map<String, Object> mapOfColValues = createColumnMap(columnCount);
		for (int i = 1; i <= columnCount; i++) {
			String columnName;
			if ( (columnName = rsmd.getColumnLabel(i)) != null && columnName.contains(".")) {
				Object value = getColumnValue(definition, rs, i);
				List<String> names = Lists.newArrayList(splitter.split(columnName));
				ObjectMapUtils.pushPath(mapOfColValues, names, value);
			}
			//else if ( (columnName = rsmd.getColumnName(i)) != null && columnName.contains(".")) {
				//TODO make this work.
			//}
			else {
				String key = getColumnKey(definition, rsmd, i);
				Object obj = getColumnValue(definition, rs, i);
				mapOfColValues.put(key, obj);
			}
		}
		return mapOfColValues;
	}

	protected  Object getColumnValue(SqlObjectDefinition<?> definition, 
			ResultSet rs, int index) throws SQLException {
		return getResultSetValue(rs, index);
	}


	protected String getColumnKey(SqlObjectDefinition<?> definition, ResultSetMetaData rsmd, int i) throws SQLException {
		String columnName = lookupColumnName(rsmd, i);
		return check.notNull(definition.resolveParameter(columnName).orNull(), 
				"The column name or label: '{}' did not match any properties " +
				"for the object definition: {}", columnName, definition.getObjectType())
				.getParameterName();
	}
	
	protected Map<String, Object> createColumnMap(int columnCount) {
		return new LinkedHashMap<String, Object>(columnCount);
	}
	
	
	public String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
		String name = resultSetMetaData.getColumnLabel(columnIndex);
		if (name == null || name.length() < 1) {
			name = resultSetMetaData.getColumnName(columnIndex);
		}
		return name;
	}
	
	public static Object getResultSetValue(ResultSet rs, int index) throws SQLException {
		Object obj = rs.getObject(index);
		String className = null;
		if (obj != null) {
			className = obj.getClass().getName();
		}
		if (obj instanceof Blob) {
			obj = rs.getBytes(index);
		}
		else if (obj instanceof Clob) {
			obj = rs.getString(index);
		}
		else if (className != null &&
				("oracle.sql.TIMESTAMP".equals(className) ||
				"oracle.sql.TIMESTAMPTZ".equals(className))) {
			obj = rs.getTimestamp(index);
		}
		else if (className != null && className.startsWith("oracle.sql.DATE")) {
			String metaDataClassName = rs.getMetaData().getColumnClassName(index);
			if ("java.sql.Timestamp".equals(metaDataClassName) ||
					"oracle.sql.TIMESTAMP".equals(metaDataClassName)) {
				obj = rs.getTimestamp(index);
			}
			else {
				obj = rs.getDate(index);
			}
		}
		else if (obj != null && obj instanceof java.sql.Date) {
			if ("java.sql.Timestamp".equals(rs.getMetaData().getColumnClassName(index))) {
				obj = rs.getTimestamp(index);
			}
		}
		return obj;
	}
	

}
