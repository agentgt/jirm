package co.jirm.core.sql;

import java.io.IOException;

import co.jirm.core.sql.SqlPlaceholderParser.ParsedSql;
import co.jirm.core.util.ResourceUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;


public abstract class MutableParameterizedSql<T> extends MutableParameterized<T> implements ParameterizedSql<T>  {

	private ParsedSql parsedSql;
	
	protected MutableParameterizedSql(String sql) {
		setSql(sql);
	}
	
	@Override
	public String getSql() {
		return parsedSql.getResultSql();
	}
	
	public T setSql(String sql) {
		parsedSql = SqlPlaceholderParser.parseSql(sql);
		return getSelf();
	}
	
	public String getOriginalSql() {
		return parsedSql.getOriginalSql();
	}
	
	public T fromResource(Class<?> k, String resource) {
		try {
			return setSql(ResourceUtils.getClasspathResourceAsString(k, resource));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public ImmutableList<Object> mergedParameters() {
		return parsedSql.mergeParameters(this);
	}

	@Override
	public String toString() {
		return "MutableParameterizedSql [parsedSql=" + parsedSql + ", getParameters()=" + getParameters()
				+ ", getNameParameters()=" + getNameParameters() + "]";
	}
	
	
	
	
}
