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
