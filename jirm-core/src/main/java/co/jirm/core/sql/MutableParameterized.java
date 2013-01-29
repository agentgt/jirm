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
package co.jirm.core.sql;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static co.jirm.core.util.JirmPrecondition.check;

import com.google.common.base.Optional;
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
	public T bind(String key, Object value) {
		check.argument(
				value != null, "Null is not allowed use Guava's Optional. Key: {}", key);
		nameParameters.put(key, value);
		return getSelf();
	}
	@Override
	public T with(Object ... value) {
		int i = 0;
		for (Object v : value) {
			check.argument(
					v != null, "Null is not allowed use Guava's Optional. Index: {}", i);
			parameters.add(v);
			i++;
		}
		return getSelf();
	}
	
	protected abstract T getSelf();
		
	public T addAll(Parameters p) {
		check.argument(p != this, "Cannot pass 'this' object into its own addAll");
		//this.nameParameters.putAll(p.getNameParameters());
		this.parameters.addAll(p.mergedParameters());
		return getSelf();
	}
	
	public T bindAll(Map<String,Object> m) {
		for (Entry<String, Object> e : m.entrySet()) {
			Object o = e.getValue();
			if (o == null) {
				o = Optional.absent();
			}
			this.nameParameters.put(e.getKey(), o);
		}
		this.nameParameters.putAll(m);
		return getSelf();
	}
	
	@Override
	public String toString() {
		return "MutableParameterized [parameters=" + parameters + ", nameParameters=" + nameParameters + "]";
	}
	
	
	
}
