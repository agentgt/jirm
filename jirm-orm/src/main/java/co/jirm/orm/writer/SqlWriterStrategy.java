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
package co.jirm.orm.writer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import static co.jirm.core.util.JirmPrecondition.check;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.mapper.definition.SqlParameterDefinition;
import co.jirm.mapper.definition.SqlParameterObjectDefinition;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;


public class SqlWriterStrategy {
	protected final Joiner commaJoiner;
	protected final String clauseSpaceSeparator;
	
	
	protected SqlWriterStrategy(Joiner commaJoiner, String clauseSpaceSeparator) {
		super();
		this.commaJoiner = commaJoiner;
		this.clauseSpaceSeparator = clauseSpaceSeparator;
	}

	public static SqlWriterStrategy newInstance(String sep) {
		return new SqlWriterStrategy(Joiner.on("," + sep),sep);
	}

	public StringBuilder insertStatement(StringBuilder qb, final SqlObjectDefinition<?> definition, Map<String, Object> m) {
		qb.append("INSERT INTO ").append(definition.getSqlName()).append(" (");
		Joiner.on(", ").appendTo(qb, insertColumns(definition, m));
		qb.append(") VALUES (");
		Joiner.on(", ").appendTo(qb, valueMarks(m.values()));
		qb.append(")");
		return qb;
	}
	
	public StringBuilder selectStatementBeforeWhere(StringBuilder sb, final SqlObjectDefinition<?> definition) {
		//sb.append(clauseSpaceSeparator);
		sb.append("SELECT ");
		selectParameters(sb, definition);
		sb.append(clauseSpaceSeparator);
		sb.append("FROM ").append(definition.getSqlName());
		innerJoin(sb, definition);
		return sb;
	}
	
	private Collection<String> sqlParameterNames(final SqlObjectDefinition<?> definition, final String tablePrefix, final String as) {
		Collection<String> it = Collections2.transform(definition.getSimpleParameters().values(), new Function<SqlParameterDefinition, String>() {

			@Override
			public String apply(SqlParameterDefinition input) {
				return input.sqlName(tablePrefix) + ((as == null) ? "" : " AS \"" + as + input.getParameterName() + "\"");
			}
			
		});
		return it;
	}
	
	private void sqlSelectParameters(
			List<String> parts, String prefix, 
			String as, SqlParameterDefinition parameter, 
			SqlParameterObjectDefinition od, int depth) {
		parts.addAll(sqlParameterNames(od.getObjectDefintion(),prefix, as));
		if (depth >= od.getMaximumLoadDepth()) return;
		for (Entry<String, SqlParameterDefinition> defs : od.getObjectDefintion().getManyToOneParameters().entrySet()) {
			String childAlias = prefix+"_"+ defs.getValue().getParameterName();
			String newAs = as + defs.getValue().getParameterName() + ".";
			sqlSelectParameters(parts, childAlias, newAs, defs.getValue(), defs.getValue().getObjectDefinition().get(), depth + 1);
		}
	}
	
	public void selectParameters(StringBuilder b, SqlObjectDefinition<?> definition) {
		
		List<String> params = Lists.newArrayList(sqlParameterNames(definition, definition.getSqlName(), null));
		for (Entry<String, SqlParameterDefinition> defs : definition.getManyToOneParameters().entrySet()) {
			String as = defs.getValue().getParameterName() + ".";
			sqlSelectParameters(params, "_" + defs.getValue().getParameterName(), as, defs.getValue(), defs.getValue().getObjectDefinition().get(), 1);
		}
		commaJoiner.appendTo(b, params);
	}
	
	private void innerJoin(StringBuilder b, String parent, String prefix, SqlParameterDefinition parameter, SqlParameterObjectDefinition od, int depth) {
		SqlParameterDefinition pd = od.getObjectDefintion().idParameter().get();
		b.append(clauseSpaceSeparator).append("INNER JOIN ").append(od.getObjectDefintion().getSqlName()).append(" ").append(prefix)
		.append(" ON ")
		.append(pd.sqlName(prefix))
		.append(" = ")
		.append(parameter.sqlName(parent));
		if (depth >= od.getMaximumLoadDepth()) return;
		for (Entry<String, SqlParameterDefinition> defs : od.getObjectDefintion().getManyToOneParameters().entrySet()) {
			String childAlias = prefix+"_"+defs.getValue().getParameterName();
			SqlParameterDefinition childParameter = defs.getValue();
			SqlParameterObjectDefinition childObjectDef = defs.getValue().getObjectDefinition().get();
			
			innerJoin(b, prefix, childAlias, childParameter, childObjectDef, depth + 1);
		}
	}
	
	public void innerJoin(StringBuilder b, SqlObjectDefinition<?> definition) {
		for (Entry<String, SqlParameterDefinition> defs : definition.getManyToOneParameters().entrySet()) {
			innerJoin(b, definition.getSqlName(), "_" + defs.getValue().getParameterName(), 
					defs.getValue(), defs.getValue().getObjectDefinition().get(), 1);
		}
	}
	
	public StringBuilder deleteStatementBeforeWhere(StringBuilder sb, final SqlObjectDefinition<?> definition) {
		sb.append("DELETE FROM ").append(definition.getSqlName());
		return sb;
	}
	
	public StringBuilder updateStatementBeforeSet(StringBuilder sb, final SqlObjectDefinition<?> definition) {
		sb.append("UPDATE ").append(definition.getSqlName());
		return sb;
	}
	
	protected List<String> insertColumns(SqlObjectDefinition<?> definition, Map<String, Object> m) {
		List<String> equalsKeys = new ArrayList<String>();
		for (Entry<String, Object> e : m.entrySet()) {
			Optional<String> sqlName = definition.parameterNameToSql(e.getKey());
			check.state(sqlName.isPresent(), 
					"Property: {} not found in object.", e.getKey());
			equalsKeys.add(sqlName.get());
		}
		return equalsKeys;
	}
	
	public void appendValues(List<Object> values, final SqlObjectDefinition<?> definition, Map<String, Object> keysAndValues) {
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
		appendValues(values, definition, keysAndValues);
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
				Optional<String> p = parameterPathToSql(definition,key);
				check.state(p.isPresent(),
						"Invalid object path: {}", key);
				return p.get();
			}
		};
		StrSubstitutor s = new StrSubstitutor(lookup, "{{", "}}", '$');
		String result = s.replace(sql);
		return result;
	}
	
	public String replaceProperties(final SqlObjectDefinition<?> definition, final String sql) {
		StrLookup<String> lookup = new StrLookup<String>() {
			@Override
			public String lookup(String key) {
				Optional<String> sqlName = definition.parameterNameToSql(key);
				check.state(sqlName.isPresent(), "Property: {} not found in object.", key);
				return sqlName.get();
			}
		};
		StrSubstitutor s = new StrSubstitutor(lookup, "{{", "}}", '$');
		String result = s.replace(sql);
		return result;
	}
	
	public Optional<String> parameterPathToSql(final SqlObjectDefinition<?> definition, String parameterDotPath) {
		List<SqlParameterDefinition> parts = definition.resolveParameterPath(parameterDotPath);
		if (parts.isEmpty()) return Optional.absent();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (SqlParameterDefinition p : parts) {
			if (first) {
				first = false;
				if ( ! p.isComplex() )
					sb.append(definition.getSqlName());
			}
			if (p.isComplex()) {
				sb.append("_").append(p.getParameterName());
			}
			else {
				sb.append(".").append(p.sqlName());
			}
			
		}
		return Optional.of(sb.toString());

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
