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

import static co.jirm.core.util.JirmPrecondition.check;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;

import java.beans.ConstructorProperties;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import co.jirm.mapper.SqlObjectConfig;
import co.jirm.mapper.converter.SqlParameterConverter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;

public class SqlParameterDefinition {
	private final String parameterName;
	private final String sqlName;
	private final Class<?> parameterType;
	private final int order;
	private final boolean id;
	private final boolean version;
	private final boolean generated;
	private final Optional<Enumerated> enumerated;
	private final Optional<SqlParameterObjectDefinition> objectDefinition;
	private final SqlParameterConverter parameterConverter;
	
	
	
	private SqlParameterDefinition(
			SqlParameterConverter parameterConverter, String parameterName, String sqlName, Class<?> parameterType, int order,
			boolean id, boolean version, boolean generated, Optional<Enumerated> enumerated,
			Optional<SqlParameterObjectDefinition> objectDefinition) {
		super();
		this.parameterName = check.notNull(parameterName, "parameterName");
		this.sqlName = check.notNull(sqlName, "sqlName");
		this.parameterType = check.notNull(parameterType, "parameterType");
		this.order = order;
		this.id = id;
		this.version = version;
		this.enumerated = check.notNull(enumerated, "enumerated");
		this.objectDefinition = check.notNull(objectDefinition, "objectDefinition");
		this.parameterConverter = check.notNull(parameterConverter, "parameterConverter");
		this.generated = generated;
	}
	
	static SqlParameterDefinition newSimpleInstance(
			SqlParameterConverter parameterConverter, 
			String parameterName, Class<?> parameterType, 
			int order, String sqlName, boolean id, boolean version, boolean generated, Optional<Enumerated> enumerated) {
		Optional<SqlParameterObjectDefinition> objectDefinition = Optional.absent();
		return new SqlParameterDefinition(parameterConverter, 
				parameterName, 
				sqlName, 
				parameterType, 
				order, 
				id, version, generated, enumerated, objectDefinition);
	}
	
	
	static SqlParameterDefinition newComplexInstance(
			SqlParameterConverter parameterConverter,
			String parameterName,
			@Nonnull
			SqlParameterObjectDefinition objDef, 
			int order, String sqlName) {
		
		Optional<SqlParameterObjectDefinition> objectDefinition = Optional.<SqlParameterObjectDefinition>of(objDef);
		Class<?> parameterType = objectDefinition.get().getObjectDefintion().getObjectType();
		boolean id = false;
		boolean version = false;
		boolean generated = false;
		Optional<Enumerated> enumerated = Optional.absent();
		
		return new SqlParameterDefinition(parameterConverter, 
				parameterName, 
				sqlName, 
				parameterType, 
				order, 
				id, version, generated, enumerated, objectDefinition);
	}
	
	public Object convertToSql(Object original) {
		return this.parameterConverter.convertToSql(original, this);
	}
	
	static Map<String, SqlParameterDefinition> getSqlBeanParameters(Class<?> k, SqlObjectConfig config) {
		Map<String, SqlParameterDefinition> parameters = new LinkedHashMap<String, SqlParameterDefinition>();
		Constructor<?> cons[] = k.getDeclaredConstructors();
		for (Constructor<?> c : cons) {
			if (c.isAnnotationPresent(JsonCreator.class)) {
			  return getSqlBeanParametersFromJsonCreatorConstructor(c, config);
			}
			
			if (c.isAnnotationPresent(ConstructorProperties.class)) {
			  return getSqlBeanParametersFromConstructorProperties(c, config);
			}
		}
		check.argument(! parameters.isEmpty(), 
				"No SQL columns/parameters found for: {}", k);
		return parameters;
	}
	
  public Optional<SqlParameterObjectDefinition> getObjectDefinition() {
		return objectDefinition;
	}

	static SqlParameterDefinition parameterDef(
			SqlObjectConfig config,
			Class<?> objectType, 
			String parameterName, 
			Class<?> parameterType, int order) {
		
		final SqlParameterDefinition definition;
		String sn = null;
		ManyToOne manyToOne = getAnnotation(objectType, parameterName, ManyToOne.class);
		if (manyToOne != null) {
			Class<?> subK = checkNotNull(manyToOne.targetEntity(), "targetEntity not set");
			JoinColumn joinColumn = getAnnotation(objectType, parameterName, JoinColumn.class);
			SqlObjectDefinition<?> od = SqlObjectDefinition.fromClass(subK, config);
			checkState( ! od.getIdParameters().isEmpty(), "No id parameters");
			if (joinColumn != null)
				sn = joinColumn.name();
			if (sn == null)
				sn = config.getNamingStrategy().propertyToColumnName(parameterName);
			FetchType fetch = manyToOne.fetch();
			int depth;
			if (FetchType.LAZY == fetch) {
				depth = 1;
			}
			else {
				depth = config.getMaximumLoadDepth();
				
			}
			SqlParameterObjectDefinition sod = new SqlParameterObjectDefinition(od, depth);
			definition = SqlParameterDefinition.newComplexInstance(config.getConverter(), parameterName, sod, order, sn);
		}
		else {
			Column col = getAnnotation(objectType, parameterName, Column.class);
			if (col != null && ! isNullOrEmpty(col.name()))
				sn = col.name();
			Id id = getAnnotation(objectType, parameterName, Id.class);
			Version version = getAnnotation(objectType, parameterName, Version.class);
			GeneratedValue generated = getAnnotation(objectType, parameterName, GeneratedValue.class);
			Enumerated enumerated = getAnnotation(objectType, parameterName, Enumerated.class);
			
			boolean idFlag = id != null;
			boolean versionFlag = version != null;
			boolean generatedFlag = generated != null;
			if (sn == null)
				sn = config.getNamingStrategy().propertyToColumnName(parameterName);
			definition = SqlParameterDefinition.newSimpleInstance(config.getConverter(), parameterName, 
					parameterType, order, sn, idFlag, versionFlag, generatedFlag, Optional.fromNullable(enumerated));
		}
		return definition;
	}

	private static <T extends Annotation> T getAnnotation(Class<?> k, String value, Class<T> a) {
		try {
			Field f = k.getDeclaredField(value);
			return f.getAnnotation(a);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getParameterName() {
		return parameterName;
	}
	public Class<?> getParameterType() {
		return parameterType;
	}
	public int getOrder() {
		return order;
	}
	public String sqlName() {
		return sqlName;
	}
	public String sqlName(String prefix) {
		if (prefix != null)
			return prefix + "." + sqlName;
		return sqlName;
	}
	
	public boolean isId() {
		return id;
	}
	public boolean isComplex() {
		return this.getObjectDefinition().isPresent();
	}
	
	public boolean isVersion() {
		return version;
	}
	public boolean isGenerated() {
		return generated;
	}
	
	public Optional<Enumerated> getEnumerated() {
		return enumerated;
	}
	
	//@SuppressWarnings("unchecked")
	public Optional<Object> valueFrom(Map<String,Object> m) {
		Object o = m.get(this.getParameterName());
		return Optional.fromNullable(o);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (id ? 1231 : 1237);
		result = prime * result + ((objectDefinition == null) ? 0 : objectDefinition.hashCode());
		result = prime * result + order;
		result = prime * result + ((parameterName == null) ? 0 : parameterName.hashCode());
		result = prime * result + ((parameterType == null) ? 0 : parameterType.hashCode());
		result = prime * result + ((sqlName == null) ? 0 : sqlName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SqlParameterDefinition other = (SqlParameterDefinition) obj;
		if (id != other.id)
			return false;
		if (objectDefinition == null) {
			if (other.objectDefinition != null)
				return false;
		}
		else if (!objectDefinition.equals(other.objectDefinition))
			return false;
		if (order != other.order)
			return false;
		if (parameterName == null) {
			if (other.parameterName != null)
				return false;
		}
		else if (!parameterName.equals(other.parameterName))
			return false;
		if (parameterType == null) {
			if (other.parameterType != null)
				return false;
		}
		else if (!parameterType.equals(other.parameterType))
			return false;
		if (sqlName == null) {
			if (other.sqlName != null)
				return false;
		}
		else if (!sqlName.equals(other.sqlName))
			return false;
		return true;
	}
	
	private static Map<String, SqlParameterDefinition> getSqlBeanParametersFromJsonCreatorConstructor(Constructor<?> c, SqlObjectConfig config) {
	  Map<String, SqlParameterDefinition> parameters = new LinkedHashMap<String, SqlParameterDefinition>();
    Annotation[][] aas = c.getParameterAnnotations();
    Class<?>[] pts = c.getParameterTypes();
    if (aas == null || aas.length == 0) {
      return parameters;
    }
    
    for (int i = 0; i < aas.length; i++) {
      Annotation[] as = aas[i];
      Class<?> parameterType = pts[i];
      for (int j = 0; j < as.length; j++) {
        Annotation a = as[j];
        if (JsonProperty.class.equals(a.annotationType())) {
          JsonProperty p = (JsonProperty) a;
          String value = p.value();
          final SqlParameterDefinition definition = parameterDef(config, c.getDeclaringClass(), value, parameterType, i);
          parameters.put(value, definition);
        }
      }
      
    }
    
    return parameters;
	}
  
  private static Map<String, SqlParameterDefinition> getSqlBeanParametersFromConstructorProperties(Constructor<?> c, SqlObjectConfig config) {
    Map<String, SqlParameterDefinition> parameters = new LinkedHashMap<String, SqlParameterDefinition>();
    
    String[] constructorProperties = c.getAnnotation(ConstructorProperties.class).value();
    Class<?>[] pts = c.getParameterTypes();

    for (int i = 0; i < pts.length; i++) {
      parameters.put(constructorProperties[i], parameterDef(config, c.getDeclaringClass(), constructorProperties[i], pts[i], i));
    }
    
    return parameters;
  }
}