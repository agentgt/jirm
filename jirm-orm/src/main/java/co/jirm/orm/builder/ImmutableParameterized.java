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
package co.jirm.orm.builder;

import co.jirm.core.sql.ParametersBuilder;
import static co.jirm.core.util.JirmPrecondition.check;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public abstract class ImmutableParameterized<T> implements ParametersBuilder<T> {
	
	protected final ImmutableList<Object> parameters;
	protected final ImmutableMap<String, Object> nameParameters;

	protected ImmutableParameterized() {
		this(ImmutableList.<Object>of(), ImmutableMap.<String,Object>of());
	}
	
	protected ImmutableParameterized(ImmutableParameterized<?> child) {
		this.parameters = child.parameters;
		this.nameParameters = child.nameParameters;
	}
	
	protected ImmutableParameterized(ImmutableList<Object> parameters, ImmutableMap<String, Object> nameParameters) {
		super();
		this.parameters = parameters;
		this.nameParameters = nameParameters;
	}
	
	protected ImmutableParameterized(ImmutableParameterized<?> child, String key, Object value) {
		this(child.parameters, addParameter(child, key, value));
	}

	protected static ImmutableMap<String, Object> addParameter(ImmutableParameterized<?> child, String key, Object value) {
		check.argument( ! child.parameters.contains(key), 
				"parameter has already been set: {}", key);
		return ImmutableMap.<String,Object>builder()
				.putAll(child.nameParameters).put(key, value).build();
	}
	
	protected ImmutableParameterized(ImmutableParameterized<?> child, Object value) {
		this(addParameter(child, value), child.nameParameters);
	}

	protected static ImmutableList<Object> addParameter(ImmutableParameterized<?> child, Object value) {
		return ImmutableList.builder().addAll(child.parameters).add(value).build();
	}
	
	public ImmutableList<Object> getParameters() {
		return parameters;
	}
	
	public ImmutableMap<String, Object> getNameParameters() {
		return nameParameters;
	}
	
	public abstract T bind(String key, Object value);
	public abstract T with(Object ... value);
	
	public ImmutableList<Object> mergedParameters() {
		return getParameters();
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nameParameters == null) ? 0 : nameParameters.hashCode());
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
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
		@SuppressWarnings("rawtypes")
		ImmutableParameterized other = (ImmutableParameterized) obj;
		if (nameParameters == null) {
			if (other.nameParameters != null)
				return false;
		}
		else if (!nameParameters.equals(other.nameParameters))
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		}
		else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}
	
	
	
}
