package co.jirm.core.sql;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public abstract class MutableParameterized<T> implements ParametersBuilder<T> {
	
	private final List<Object> parameters = Lists.newArrayList();
	private final Map<String,Object> nameParameters = Maps.newLinkedHashMap();
	
	@Override
	public ImmutableList<Object> getParameters() {
		return ImmutableList.copyOf(parameters);
	}
	@Override
	public ImmutableMap<String, Object> getNameParameters() {
		return ImmutableMap.copyOf(nameParameters);
	}
	@Override
	public T set(String key, Object value) {
		nameParameters.put(key, value);
		return getSelf();
	}
	@Override
	public T with(Object ... value) {
		for (Object v : value) {
			parameters.add(v);
		}
		return getSelf();
	}
	
	protected abstract T getSelf();
		
	public T addAll(Parameters p) {
		if (p == this) throw new IllegalArgumentException("don't do that");
		this.nameParameters.putAll(p.getNameParameters());
		this.parameters.addAll(p.mergedParameters());
		return getSelf();
	}
	
	@Override
	public String toString() {
		return "MutableParameterized [parameters=" + parameters + ", nameParameters=" + nameParameters + "]";
	}
	
	
	
}
