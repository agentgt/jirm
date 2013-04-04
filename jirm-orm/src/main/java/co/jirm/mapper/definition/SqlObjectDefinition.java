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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.persistence.Table;

import static co.jirm.core.util.JirmPrecondition.check;
import co.jirm.mapper.SqlObjectConfig;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.UncheckedExecutionException;


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
		try {
			return (SqlObjectDefinition<T>) config.getCache().get(objectType, new Callable<SqlObjectDefinition<?>>() {
				@Override
				public SqlObjectDefinition<?> call() throws Exception {
					return SqlObjectDefinition._fromClass(objectType, config);
				}
			});
		} catch (ExecutionException e) {
			throw Throwables.propagate(e);
		} catch (UncheckedExecutionException e) {
			throw Throwables.propagate(Objects.firstNonNull(e.getCause(), e));
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
	

	
	private final static Splitter dotSplitter = Splitter.on(".");
	
	
	public List<SqlParameterDefinition> resolveParameterPath(String parameterDotPath) {
		Iterator<String> it = dotSplitter.split(parameterDotPath).iterator();
		List<SqlParameterDefinition> r = Lists.newArrayList();
		SqlObjectDefinition<?> currentObject = this;
		while(it.hasNext()) {
			String pathPart = it.next();
			Optional<SqlParameterDefinition> p = currentObject.resolveParameter(pathPart);
			if ( ! p.isPresent() ) return emptyList();
			check.state( ! ( it.hasNext() && ! p.get().isComplex()), "Bad path parts references a simple parameter: {}", parameterDotPath);
			r.add(p.get());
			if (it.hasNext())
				currentObject = p.get().getObjectDefinition().get().getObjectDefintion();
		}
		return r;
	}
	
	public Map<String, SqlParameterDefinition> getIdParameters() {
		return idParameters;
	}
	
	public Map<String, SqlParameterDefinition> getSimpleParameters() {
		return simpleParameters;
	}
	
	public Optional<SqlParameterDefinition> idParameter() {
		return Optional.fromNullable(Iterators.get(this.getIdParameters().values().iterator(), 0, null));
	}
	
	public String getSqlName() {
		return sqlName;
	}
}
