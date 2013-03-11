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
package co.jirm.core.util;

import static co.jirm.core.util.JirmPrecondition.check;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import co.jirm.core.JirmIllegalArgumentException;

import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;


public class ObjectMapUtils {
	
	public static StringBuilder toStringList(StringBuilder sb, List<?> objects, int length) {
		sb.append("[");
		boolean first = true;
		for(Object o : objects) {
			if (first) {
				first = false;
			}
			else {
				sb.append(", ");
			}
			final String s;
			if (o instanceof Calendar) {
				s = new DateTime(o).toString();
			}
			else {
				s = o.toString();
			}
			if (length < s.length())
				sb.append(s, 0, length).append("...");
			else
				sb.append(s);
		}
		sb.append("]");
		return sb;
	}
	
	//TODO cache
	private static Enum<?>[] _enumValues(Class<?> enumClass) throws SecurityException, NoSuchMethodException, 
		IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Method m = enumClass.getMethod("values");
		return (Enum<?>[]) m.invoke((Object) null);
	}
	
	public static Enum<?>[] enumValues(Class<?> enumClass) {
		try {
			return _enumValues(enumClass);
		} catch (Exception e) {
			throw new JirmIllegalArgumentException("Cannot dynamically resolve enum values for:" + enumClass, e);
		}
	}
	
	public static Enum<?> enumFrom(Class<?> enumClass, int oridinal) {
		return enumValues(enumClass)[oridinal];
	}
	public static Enum<?> enumFrom(Class<?> enumClass, String name) {
		Enum<?>[] enums = enumValues(enumClass);
		for (Enum<?> e : enums) {
			if (e.name().equals(name)) {
				return e;
			}
		}
		throw check.argumentInvalid("name: {} is not valid for: {}", name, enumClass);
	}
	
	@SuppressWarnings("unchecked")
	public static void pushPath(final Map<String, Object> m, List<String> names, Object value) {
		Map<String,Object> current = m;
		int j = 0;
		for (String n : names) {
			j++;
			if (j == names.size()) {
				current.put(n, value);
				break;
			}
			Object o = current.get(n);
			if (o == null) {
				Map<String, Object> sub = Maps.newLinkedHashMap();
				current.put(n, sub);
				current = sub;
			}
			else if (o instanceof Map) {
				current = (Map<String,Object>) o;
			}
			else {
				throw new IllegalArgumentException("Cannot set value to " + names);
			}
		}
	}
	
	public static <T> NestedKeyValue<T> getNestedKeyValue(Map<?,?> m, Object... keys) {
		return _getNestedKeyValue(m, null, null, keys);
	}
	
	@SuppressWarnings("unchecked")
	private static <T> NestedKeyValue<T> _getNestedKeyValue(final Map<?,?> m, final List<?> l, final Class<T> c, Object... keys) {
		Object t = null;
		check.argument(m == null || l == null, "Either m or l should be null");
		Map<?,?> tm = m;
		List<?> tl = l;
		
		int i = 0;
		for(Object k : keys) {
			if (tm != null && tm.containsKey(k)) {
				t =  tm.get(k);
			}
			else if (tl != null && k instanceof Number) {
				t = tl.get(((Number) k).intValue());
			}
			else if ( tl != null && k instanceof String 
					&& ! ((String) k).isEmpty() 
					&&  Ints.tryParse((String)k) != null) {
				t = tl.get(Integer.parseInt(k.toString()));
			}
			else {
				t = null;
				break;
			}
			
			if (t instanceof Map) {
				tm = (Map<?,?>) t;
				tl = null;
			}
			else if (t instanceof List) {
				tl = (List<?>) t;
				tm = null;
			}
			else if ( t == null ) {
				break;
			}
			i++;
		}
		boolean conflict = t != null && c != null && ! c.isInstance(t);
		T object = conflict ? null : (T) t;
		
		
		return new NestedKeyValue<T>(object, keys, i, conflict);
		
	}
	
	public static class NestedKeyValue<T> {
		public final T object;
		public final int depthStopped;
		public final Object[] keys;
		public final boolean conflict;
		
		private NestedKeyValue(T object, Object[] keys, int depthStopped, boolean conflict) {
			super();
			this.object = object;
			this.keys = keys;
			this.depthStopped = depthStopped;
			this.conflict = conflict;
		}
		
		public boolean isPresent() {
			return depthStopped == keys.length;
		}
	}

}
