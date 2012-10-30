package co.jirm.orm.writer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.mapper.definition.SqlParameterDefinition;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;


public class SqlWriterStrategy {
	
	public StringBuilder insertStatement(StringBuilder qb, final SqlObjectDefinition<?> definition, Map<String, Object> m) {
		qb.append("INSERT INTO ").append(definition.getSqlName()).append(" (");
		Joiner.on(",").appendTo(qb, insertColumns(definition, m));
		qb.append(") VALUES (");
		Joiner.on(",").appendTo(qb, valueMarks(m.values()));
		qb.append(")");
		return qb;
	}
	
	public StringBuilder selectStatementBeforeWhere(StringBuilder sb, final SqlObjectDefinition<?> definition) {
		sb.append("SELECT ");
		definition.selectParameters(sb);
		sb.append(" FROM ").append(definition.getSqlName());
		definition.innerJoin(sb);
		return sb;
	}
	
	public StringBuilder deleteStatementBeforeWhere(StringBuilder sb, final SqlObjectDefinition<?> definition) {
		sb.append("DELETE ").append(definition.getSqlName()).append(" ");
		return sb;
	}
	
	public StringBuilder updateStatementBeforeSet(StringBuilder sb, final SqlObjectDefinition<?> definition) {
		sb.append("UPDATE ").append(definition.getSqlName()).append(" ");
		return sb;
	}
	
	protected List<String> insertColumns(SqlObjectDefinition<?> definition, Map<String, Object> m) {
		List<String> equalsKeys = new ArrayList<String>();
		for (Entry<String, Object> e : m.entrySet()) {
			equalsKeys.add(definition.parameterNameToSql(e.getKey()));
		}
		return equalsKeys;
	}
	
	public void appendValues(final SqlObjectDefinition<?> definition, List<Object> values, Map<String, Object> keysAndValues) {
		for (Entry<String, Object> e : keysAndValues.entrySet()) {
			SqlParameterDefinition d = definition.getParameters().get(e.getKey());
			if (d == null) {
				values.add(e.getValue());	
			}
			else {
				values.add(d.convertToSql(e.getValue()));
			}
		}
	}
	
	public List<Object> fillValues(final SqlObjectDefinition<?> definition, Map<String, Object> keysAndValues) {
		List<Object> values = Lists.newArrayListWithCapacity(keysAndValues.size());
		appendValues(definition, values, keysAndValues);
		return values;
	}
	
	protected List<String> valueMarks(Iterable<Object> values) {
		List<String> marks = new ArrayList<String>();
		for (@SuppressWarnings("unused") Object v : values) {
			marks.add("?");
		}
		return marks;
	}
	
	public String replacePropertyPaths(final SqlObjectDefinition<?> definition, final String sql) {
		StrLookup<String> lookup = new StrLookup<String>() {
			@Override
			public String lookup(String key) {
				return definition.parameterPathToSql(key).orNull();
			}
		};
		StrSubstitutor s = new StrSubstitutor(lookup, "{{", "}}", '$');
		String result = s.replace(sql);
		return result;
	}
	
	
//	public String selectConjuntionQuery() {
//		StringBuilder qb = new StringBuilder();
//		qb.append("SELECT ");
//		definition.selectParameters(qb);
//		qb.append(" FROM ").append(definition.getSqlName());
//		definition.innerJoin(qb);
//		qb.append(" WHERE ").append(this.andClause());
//		if (limit.isPresent()) {
//			qb.append(" ");
//			qb.append("LIMIT ?");	
//		}
//		if (offset.isPresent()) {
//			qb.append(" ");
//			qb.append("OFFSET ?");
//		}
//		String q = qb.toString();
//		return q;
//	}
//	

}
