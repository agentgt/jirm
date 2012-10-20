package co.jirm.core.sql;

import co.jirm.core.util.JirmPrecondition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public abstract class ImmutableParameterized<T> implements ParametersBuilder<T> {
	
	protected final ImmutableList<Object> parameters;
	protected final ImmutableMap<String, Object> nameParameters;

	public ImmutableParameterized() {
		this(ImmutableList.<Object>of(), ImmutableMap.<String,Object>of());
	}
	
	public ImmutableParameterized(ImmutableParameterized<?> child) {
		this.parameters = child.parameters;
		this.nameParameters = child.nameParameters;
	}
	
	public ImmutableParameterized(ImmutableList<Object> parameters, ImmutableMap<String, Object> nameParameters) {
		super();
		this.parameters = parameters;
		this.nameParameters = nameParameters;
	}
	
	protected ImmutableParameterized(ImmutableParameterized<?> child, String key, Object value) {
		this(child.parameters, addParameter(child, key, value));
	}

	protected static ImmutableMap<String, Object> addParameter(ImmutableParameterized<?> child, String key, Object value) {
		JirmPrecondition.check.argument( ! child.parameters.contains(key), 
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
	
	public abstract T set(String key, Object value);
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
