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

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Resources;


public class ResourceUtils {
	
	private final static Supplier<Cache<ClasspathResourceKey, String>> cacheSupplier = 
			Suppliers.memoize(new Supplier<Cache<ClasspathResourceKey, String>>() {
				@Override
				public Cache<ClasspathResourceKey, String> get() {
					return CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(10000, TimeUnit.SECONDS).build();
				}
			});
	
	public static String getClasspathResourceAsString(final Class<?> klass, final String path) throws IOException {
		try {
			return cacheSupplier.get().get(new ClasspathResourceKey(path, klass),  new Callable<String>() {
				@Override
				public String call() throws Exception {
					return _getClasspathResourceAsString(klass, path);
				}
				
			});
		} catch (ExecutionException e) {
			throw new IOException(e);
		}
	}
	
	private static String _getClasspathResourceAsString(Class<?> k, String path) throws IOException {
		return Resources.toString(Resources.getResource(k, path), Charsets.UTF_8);
	}
	
	private static class ClasspathResourceKey {
		private final String path;
		private final String klassName;
		
		private ClasspathResourceKey(String path, Class<?> klass) {
			super();
			this.path = path;
			/*
			 * Lets avoid leaky permgen errors.
			 */
			this.klassName = "CLASS:" +  klass.getCanonicalName();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((klassName == null) ? 0 : klassName.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
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
			ClasspathResourceKey other = (ClasspathResourceKey) obj;
			if (klassName == null) {
				if (other.klassName != null)
					return false;
			}
			else if (!klassName.equals(other.klassName))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			}
			else if (!path.equals(other.path))
				return false;
			return true;
		}
		
		
	}

}
