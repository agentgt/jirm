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
package co.jirm.mapper.copy;

import java.util.LinkedHashMap;

import static co.jirm.core.util.JirmPrecondition.check;
import co.jirm.mapper.converter.SqlObjectConverter;


public class CopyBuilder<T> extends AbstractCopyBuilder<CopyBuilder<T>> {

	private final SqlObjectConverter converter;
	private final Class<T> type;
	
	protected CopyBuilder(Class<T> type, SqlObjectConverter converter) {
		super();
		this.type = type;
		this.converter = converter;
	}
	
	public static <T> CopyBuilder<T> newInstance(Class<T> type, SqlObjectConverter converter) {
		return new CopyBuilder<T>(type, converter);
	}

	public T copy(T newObject, T original) { 
		check.argument(newObject != null && original != null, 
				"new and original must not be null");
		LinkedHashMap<String, Object> n = converter.convertObjectToSqlMap(newObject);
		LinkedHashMap<String, Object> o = converter.convertObjectToSqlMap(original);
		for (String s : n.keySet()) {
			if (! excludeProperties.contains(s) || includeProperties.contains(s) ) {
				o.put(s, n.get(s));
			}
		}
		final T r = converter.convertSqlMapToObject(o, type);
		return r;
	}
	
	
	@Override
	protected CopyBuilder<T> getSelf() {
		return this;
	}

}
