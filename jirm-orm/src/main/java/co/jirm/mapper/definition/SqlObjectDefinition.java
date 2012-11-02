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
package co.jirm.mapper.definition;

import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.emptyList;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.persistence.Table;

import co.jirm.core.util.JirmPrecondition;
import co.jirm.mapper.SqlObjectConfig;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;


public class SqlObjectDefinition<T> {
	private final Class<T> objectType;
	private final Map<String, SqlParameterDefinition> parameters;
	private final Map<String, SqlParameterDefinition> sqlNameToParameter;
	private final Map<String, SqlParameterDefinition> idParameters;
	private final Map<String, SqlParameterDefinition> manyToOneParameters;
	private final Map<String, SqlParameterDefinition> simpleParameters;
	
	private final String sqlName;
	
	private SqlObjectDefinition(SqlObjectConfig config, Class<T> objectType, Map<String, SqlParameterDefinition> parameters) {
		super();
		this.objectType = objectType;
		this.parameters = parameters;
		LinkedHashMap<String, SqlParameterDefinition> n = newLinkedHashMap();
		LinkedHashMap<String, SqlParameterDefinition> id = newLinkedHashMap();
		LinkedHashMap<String, SqlParameterDefinition> m = newLinkedHashMap();
		LinkedHashMap<String, SqlParameterDefinition> s = newLinkedHashMap();
		
		for(Entry<String, SqlParameterDefinition> e : parameters.entrySet()) {
			n.put(e.getValue().sqlName(), e.getValue());
			if (e.getValue().isId()) {
				id.put(e.getKey(), e.getValue());
			}
			if (e.getValue().getObjectDefinition().isPresent()) {
				m.put(e.getKey(), e.getValue());
			}
			else {
				s.put(e.getKey(), e.getValue());
			}
		}
		sqlNameToParameter = ImmutableMap.copyOf(n);
		idParameters = ImmutableMap.copyOf(id);
		manyToOneParameters = ImmutableMap.copyOf(m);
		simpleParameters = ImmutableMap.copyOf(s);
		
		Table t = objectType.getAnnotation(Table.class);
		if (t != null && t.name() != null) {
			sqlName = t.name();
		}
		else {
			sqlName = config.getNamingStrategy().classToTableName(objectType.getSimpleName());
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T> SqlObjectDefinition<T> _fromClass(Class<T> c, SqlObjectConfig config) {
		return new SqlObjectDefinition(config, c, ImmutableMap.copyOf(SqlParameterDefinition.getSqlBeanParameters(c, config)));
	}
	
//	public static <T> SqlObjectDefinition<T> fromClass(Class<T> c) {
//		return fromClass(c, SqlObjectConfig.DEFAULT);
//	}
	
	@SuppressWarnings("unchecked")
	public static <T> SqlObjectDefinition<T> 
		fromClass(final Class<T> objectType, final SqlObjectConfig config) {
		String name = "CLASS:" + objectType.getCanonicalName();
		try {
			return (SqlObjectDefinition<T>) config.getCache().get(name, new Callable<SqlObjectDefinition<?>>() {
				@Override
				public SqlObjectDefinition<?> call() throws Exception {
					return SqlObjectDefinition._fromClass(objectType, config);
				}
			});
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		}
	}
	
	public Class<T> getObjectType() {
		return objectType;
	}
	
	public Map<String,SqlParameterDefinition> getParameters() {
		return parameters;
	}
	
	
	public Map<String, SqlParameterDefinition> getManyToOneParameters() {
		return manyToOneParameters;
	}
	
	public Map<String, SqlParameterDefinition> getSqlNameToParameter() {
		return sqlNameToParameter;
	}
	
	public Optional<SqlParameterDefinition> resolveParameter(String parameter) {
		if (getSqlNameToParameter().containsKey(parameter))
			return Optional.of(getSqlNameToParameter().get(parameter));
		if (getParameters().containsKey(parameter))
			return Optional.of(getParameters().get(parameter));
		return Optional.absent();
	}
	
	public Optional<String> parameterNameToSql(String parameter) {
		SqlParameterDefinition d = getParameters().get(parameter);
		if (d == null) return Optional.absent();
		return Optional.of(d.sqlName());
	}
	
	public Optional<String> parameterPathToSql(String parameterDotPath) {
		List<SqlParameterDefinition> parts = resolveParameterPath(parameterDotPath);
		if (parts.isEmpty()) return Optional.absent();
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (SqlParameterDefinition p : parts) {
			if (first) {
				first = false;
				if ( ! p.isComplex() )
					sb.append(this.getSqlName());
			}
			if (p.isComplex()) {
				sb.append("_").append(p.sqlName());
			}
			else {
				sb.append(".").append(p.sqlName());
			}
			
		}
		return Optional.of(sb.toString());

	}
	
	private final static Splitter dotSplitter = Splitter.on(".");
	
	
	public List<SqlParameterDefinition> resolveParameterPath(String parameterDotPath) {
		Iterator<String> it = dotSplitter.split(parameterDotPath).iterator();
		List<SqlParameterDefinition> r = Lists.newArrayList();
		SqlObjectDefinition<?> currentObject = this;
		while(it.hasNext()) {
			String pathPart = it.next();
			Optional<SqlParameterDefinition> p = currentObject.resolveParameter(pathPart);
			if ( ! p.isPresent() ) return emptyList();
			JirmPrecondition.check.state( ! ( it.hasNext() && ! p.get().isComplex()), "Bad path parts references a simple parameter: {}", parameterDotPath);
			r.add(p.get());
			if (it.hasNext())
				currentObject = p.get().getObjectDefinition().get().getObjectDefintion();
		}
		return r;
	}
	
	
	
	
	public Map<String, SqlParameterDefinition> getIdParameters() {
		return idParameters;
	}
	
	static final Joiner joiner = Joiner.on(", ");
	
	public String sqlParameterNames() {
		return joiner.join(sqlParameterNames(null, null));
	}
	
	public Map<String, SqlParameterDefinition> getSimpleParameters() {
		return simpleParameters;
	}
	
	private Collection<String> sqlParameterNames(final String tablePrefix, final String as) {
		Collection<String> it = Collections2.transform(getSimpleParameters().values(), new Function<SqlParameterDefinition, String>() {

			@Override
			public String apply(SqlParameterDefinition input) {
				return input.sqlName(tablePrefix) + ((as == null) ? "" : " AS \"" + as + input.getParameterName() + "\"");
			}
			
		});
		return it;
	}
	
	private void sqlSelectParameters(List<String> parts, String prefix, 
			String as, SqlParameterDefinition parameter, SqlParameterObjectDefinition od, int depth) {
		parts.addAll(od.getObjectDefintion().sqlParameterNames(prefix, as));
		if (depth >= od.getMaximumLoadDepth()) return;
		for (Entry<String, SqlParameterDefinition> defs : od.getObjectDefintion().getManyToOneParameters().entrySet()) {
			String childAlias = prefix+"_"+ defs.getValue().getParameterName();
			String newAs = as + defs.getValue().getParameterName() + ".";
			sqlSelectParameters(parts, childAlias, newAs, defs.getValue(), defs.getValue().getObjectDefinition().get(), depth + 1);
		}
	}
	
	public void selectParameters(StringBuilder b) {
		
		List<String> params = Lists.newArrayList(this.sqlParameterNames(this.getSqlName(), null));
		for (Entry<String, SqlParameterDefinition> defs : this.getManyToOneParameters().entrySet()) {
			String as = defs.getValue().getParameterName() + ".";
			sqlSelectParameters(params, "_" + defs.getValue().getParameterName(), as, defs.getValue(), defs.getValue().getObjectDefinition().get(), 1);
		}
		joiner.appendTo(b, params);
	}
	
//	protected Map<String, JoinPath> resolveJoinPaths() {
//		LinkedHashMap<String, JoinPathPart> joinParts = newLinkedHashMap();
//	}
//	
//	protected Map<String, JoinPathPart> addJoinPath(Map<String,JoinPathPart> joinParts, List<String> pathParts) {
//
//		String joinColumnAlias = null;
//		SqlObjectDefinition<?> currentObject = this;
//		Iterator<String> it = pathParts.iterator();
//		String dotPath = "";
//		boolean first = true;
//		int i = 0;
//		while(it.hasNext()) {
//			String pathPart = it.next();
//			if (first) {
//				dotPath += pathPart; first = false;
//				if (isNullOrEmpty(joinColumnAlias)) {
//					joinColumnAlias = pathPart + i;
//				}
//			}
//			else {
//				dotPath = dotPath + "." + pathPart;
//				joinColumnAlias += "_" + pathPart + i;
//			}
//			SqlParameterDefinition p = currentObject.getParameters().get(pathPart);
//			SqlObjects.check.state( ! ( it.hasNext() && ! p.isComplex()), "Bad path parts references a simple parameter: {}", pathParts);
//			joinParts.put(dotPath, new JoinPathPart(dotPath, joinColumnAlias, p));
//			if (it.hasNext())
//				currentObject = p.getObjectDefinition().get();
//			i++;
//		}
//		
//		return joinParts;
//		
//	}
//	
//	
//	
//	public static class JoinPathPart {
//		private final String dotPath;
//		private final String joinColumnAlias;
//		private final SqlParameterDefinition parameter;
//		private JoinPathPart(String dotPath, String joinColumnAlias, SqlParameterDefinition parameter) {
//			super();
//			this.dotPath = dotPath;
//			this.joinColumnAlias = joinColumnAlias;
//			this.parameter = parameter;
//		}
//		
//		public String getDotPath() {
//			return dotPath;
//		}
//		public String getJoinColumnAlias() {
//			return joinColumnAlias;
//		}
//		
//		public SqlParameterDefinition getParameter() {
//			return parameter;
//		}
//		
//	}
	
	
	
	private void innerJoin(StringBuilder b, String parent, String prefix, SqlParameterDefinition parameter, SqlParameterObjectDefinition od, int depth) {
		SqlParameterDefinition pd = od.getObjectDefintion().idParameter().get();
		b.append(" INNER JOIN ").append(od.getObjectDefintion().getSqlName()).append(" ").append(prefix)
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
	
	public void innerJoin(StringBuilder b) {
		for (Entry<String, SqlParameterDefinition> defs : this.getManyToOneParameters().entrySet()) {
			innerJoin(b, getSqlName(), "_" + defs.getValue().getParameterName(), defs.getValue(), defs.getValue().getObjectDefinition().get(), 1);
		}
	}
	
	public Optional<SqlParameterDefinition> idParameter() {
		return Optional.fromNullable(Iterators.get(this.getIdParameters().values().iterator(), 0, null));
	}
	
	
	public String getSqlName() {
		return sqlName;
	}
}
