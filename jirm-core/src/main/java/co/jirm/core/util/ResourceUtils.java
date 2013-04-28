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
import com.google.common.base.Objects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.UncheckedExecutionException;


public class ResourceUtils {
	
	public static long expire = 10;
	
	public static void setCacheExpire(long expire) {
		ResourceUtils.expire = expire;
	}
	
	private final static Supplier<Cache<ClasspathResourceKey, String>> cacheSupplier = 
			Suppliers.memoize(new Supplier<Cache<ClasspathResourceKey, String>>() {
				@Override
				public Cache<ClasspathResourceKey, String> get() {
					return CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(expire, TimeUnit.SECONDS).build();
				}
			});
	
	public static String getClasspathResourceAsString(final Class<?> klass, final String path, boolean cache) throws IOException {
		if (cache) {
			return getClasspathResourceAsString(klass, path);
		}
		else {
			return _getClasspathResourceAsString(klass, path);
		}
	}
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
		} catch (UncheckedExecutionException e) {
			throw Throwables.propagate(Objects.firstNonNull(e.getCause(), e));
		}
	}
	
	public static String getClasspathResourceAsString(final String path) throws IOException {
		try {
			return cacheSupplier.get().get(new ClasspathResourceKey(path),  new Callable<String>() {
				@Override
				public String call() throws Exception {
					return _getClasspathResourceAsString(path);
				}
				
			});
		} catch (ExecutionException e) {
			throw new IOException(e);
		} catch (UncheckedExecutionException e) {
			throw Throwables.propagate(Objects.firstNonNull(e.getCause(), e));
		}
	}
	
	private static String _getClasspathResourceAsString(Class<?> k, String path) throws IOException {
		return Resources.toString(Resources.getResource(k, path), Charsets.UTF_8);
	}
	
	public static String resolvePath(Class<?> c, String path) {
		return resolveName(c, path);
	}
	
	/*
    * Add a package name prefix if the name is not absolute Remove leading "/"
    * if name is absolute
    */
   private static String resolveName(Class<?> c, String name) {
       if (name == null) {
           return name;
       }
       if (!name.startsWith("/")) {
           while (c.isArray()) {
               c = c.getComponentType();
           }
           String baseName = c.getName();
           int index = baseName.lastIndexOf('.');
           if (index != -1) {
               name = baseName.substring(0, index).replace('.', '/')
                   +"/"+name;
           }
       } else {
           name = name.substring(1);
       }
       return name;
   }
	
	private static String _getClasspathResourceAsString(String path) throws IOException {
		return Resources.toString(Resources.getResource(path), Charsets.UTF_8);
	}
	
	private static class ClasspathResourceKey {
		private final String path;
		private final String klassName;
		
		private ClasspathResourceKey(String path) {
			super();
			this.path = path;
			this.klassName = "";
		}
		
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
