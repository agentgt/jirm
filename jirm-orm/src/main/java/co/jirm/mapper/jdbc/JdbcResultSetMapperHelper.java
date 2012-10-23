package co.jirm.mapper.jdbc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import co.jirm.mapper.definition.SqlObjectDefinition;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


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
				pushPath(mapOfColValues, names, value);
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
		return checkNotNull(definition.resolveParameter(columnName).orNull(), columnName).getParameterName();
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
	
	@SuppressWarnings("unchecked")
	public static void pushPath(final Map<String, Object> m, List<String> names, Object value) {
		Map<String,Object> current = m;
		int j = 0;
		for (String n : names) {
			j++;
			if (j == names.size()) {
				current.put(n, value);
				break;
			}
			Object o = current.get(n);
			if (o == null) {
				Map<String, Object> sub = Maps.newLinkedHashMap();
				current.put(n, sub);
				current = sub;
			}
			else if (o instanceof Map) {
				current = (Map<String,Object>) o;
			}
			else {
				throw new IllegalArgumentException("Cannot set value to " + names);
			}
		}
	}

}
