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

import static co.jirm.core.util.JirmPrecondition.check;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.nullToEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.jirm.core.JirmIllegalArgumentException;
import co.jirm.core.JirmIllegalStateException;
import co.jirm.core.sql.SqlPartialParser.ResourceLoader.CachedResourceLoader;
import co.jirm.core.util.JirmUrlEncodedUtils;
import co.jirm.core.util.ResourceUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.LineReader;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.UncheckedExecutionException;


public class SqlPartialParser {
	
	private static final Pattern tokenPattern = Pattern.compile("^[ \t]*-- ?\\{(.*?)\\}[ \t]*$");
	/*
	 * FileDeclaration = SQL and list of HashDeclaration and list of References
	 * HashDeclaration = list of References and SQL
	 * References = SQL
	 */
	
	protected abstract static class DeclarationSql {
		private final Path path;
		private final List<String> declaredSql;
		private final List<ReferenceSql> references;
		protected DeclarationSql(String path, List<String> declaredSql, List<ReferenceSql> references) {
			super();
			this.path = Path.create(path);
			this.declaredSql = declaredSql;
			this.references = references;
		}

		public Path getPath() {
			return path;
		}
		
		public List<String> getDeclaredSql() {
			return declaredSql;
		}
		
		public List<ReferenceSql> getReferences() {
			return references;
		}

		@Override
		public String toString() {
			return "DeclarationSql [path=" + path + ", declaredSql=" + declaredSql + ", references=" + references + "]";
		}
		
		public abstract boolean isHash();
		
		public abstract boolean isFile();
		
		public abstract int getStartIndex();
		
		public List<String> inner() {
			return isHash() ? declaredSql.subList(1, declaredSql.size() - 1) : declaredSql;
		}
		
		public String join() {
			return Joiner.on("\n").join(inner());
		}
		
	}
	
	protected static class FileDeclarationSql extends DeclarationSql {
		
		private final Map<String, HashDeclarationSql> hashDeclarations;

		private FileDeclarationSql(String path, List<String> declaredSql, List<ReferenceSql> references, Map<String, HashDeclarationSql> hashDeclarations) {
			super(path, declaredSql, references);
			this.hashDeclarations = hashDeclarations;
		}
		
		public Map<String, HashDeclarationSql> getHashDeclarations() {
			return hashDeclarations;
		}
		
		@Override
		public int getStartIndex() {
			return 0;
		}
		
		public boolean isHash() {
			return false;
		}
		
		public boolean isFile() {
			return true;
		}
		
	}
	
	protected static class HashDeclarationSql extends DeclarationSql {

		private final int startIndex;
		private final int length;
		
		private HashDeclarationSql(String path, List<String> declaredSql, List<ReferenceSql> references, int startIndex, int length) {
			super(path, declaredSql, references);
			this.startIndex = startIndex;
			this.length = length;
		}
		
		
		public int getStartIndex() {
			return startIndex;
		}
		
		public int getLength() {
			return length;
		}
		@Override
		public boolean isHash() {
			return true;
		}
		public boolean isFile() {
			return false;
		}
		
		public String header() {
			return getDeclaredSql().get(0);
		}
		
		public String footer() {
			return getDeclaredSql().get(getDeclaredSql().size() - 1);
		}
		
	}
	
	protected static class ReferenceHeader {
		private final Path path;
		private final Map<String, List<String>> parameters;
		
		private ReferenceHeader(Path path, Map<String, List<String>> parameters) {
			super();
			this.path = path;
			this.parameters = parameters;
		}
		
		public static ReferenceHeader parse(String tag) {
			tag = tag.trim();
			String t = tag.startsWith(">") ? tag.substring(1).trim() : tag;
			
			String[] parts = t.split("[ \t]+");
			//final String u;
			//final URI uri;
			final String path;
			final String fragment;
			final String query;
			if (parts.length > 1) {
				check.state( ! t.contains("?"), "Cannot mix space and '?' style");
				URI u = URI.create(parts[0]);
				path = u.getPath();
				fragment = u.getFragment();
				query = parts[1];
			}
			else {
				URI u = URI.create(t);
				path = u.getPath();
				fragment = u.getFragment();
				query = u.getQuery();				
			}
			
			StringBuilder s = new StringBuilder();
			if (path != null) {
				s.append(path);
			}
			if (emptyToNull(query) != null) {
				s.append("?").append(query);
			}
			if(fragment != null) {
				s.append("#").append(fragment);
			}
			URI uri = URI.create(s.toString());
			
			Map<String, List<String>> parameters = ImmutableMap.copyOf(JirmUrlEncodedUtils.parseParameters(uri, "UTF-8"));
			
			String p = (uri.getPath() != null ? uri.getPath() : "")
					+  (nullToEmpty(uri.getFragment()).isEmpty() ? "" : "#" + uri.getFragment());
			return new ReferenceHeader(Path.create(p), parameters);
		}
		
		
		public Path getPath() {
			return path;
		}
		
		public Map<String, List<String>> getParameters() {
			return parameters;
		}
		
	}
	
	protected static class ReferenceSql {
		private final Path referencePath;
		private final Path currentPath;
		private final List<String> declaredSql;
		private final int startIndex;
		private final int length;
		private final Map<String,List<String>> parameters;
		
		private ReferenceSql(String referencePath, String currentPath, List<String> declaredSql, int startIndex, int length, Map<String,List<String>> parameters) {
			super();
			this.referencePath = Path.create(referencePath);
			this.currentPath = Path.create(currentPath);
			this.declaredSql = declaredSql;
			this.startIndex = startIndex;
			this.length = length;
			this.parameters = parameters;
		}
		
		
		public List<String> getDeclaredSql() {
			return declaredSql;
		}
		
		public Path getCurrentPath() {
			return currentPath;
		}
		
		public Path getReferencePath() {
			return referencePath;
		}
		
		public int getStartIndex() {
			return startIndex;
		}
		
		public int getLength() {
			return length;
		}
		
		public Map<String, List<String>> getParameters() {
			return parameters;
		}
		
		public boolean isSame() {
			List<String> values = getParameters().get("same");
			if (values == null) {
				return false;
			}
			if (values.isEmpty()) {
				return false;
			}
			String v = values.get(values.size() - 1);
			if ("false".equals(v)) {
				return false;
			}
			
			return true;
		}
		
		public List<String> inner() {
			return declaredSql.subList(1, declaredSql.size() - 1);
		}
		
		public String join() {
			return Joiner.on("\n").join(inner());
		}
		
	}
	
	public static class ExpandedSql {
		private final List<String> expanded;
		private final DeclarationSql declaration;
		
		private ExpandedSql(List<String> expanded, DeclarationSql declaration) {
			super();
			this.expanded = expanded;
			this.declaration = declaration;
		}
		
		public static ExpandedSql create(DeclarationSql declaration, List<String> expanded) {
			return new ExpandedSql(expanded, declaration);
		}
		
		public List<String> getExpanded() {
			return expanded;
		}
		
		public DeclarationSql getDeclaration() {
			return declaration;
		}
		
		public List<String> inner() {
			return declaration.isHash() ? expanded.subList(1, expanded.size() - 1) : expanded;
		}
		
		public String join() {
			return Joiner.on("\n").join(inner());
		}

		@Override
		public String toString() {
			return "ExpandedSql [expanded=" + expanded + ", declaration=" + declaration + "]";
		}
		
		
	}
	
	public static class Path {
		final String path;
		final Optional<String> hash;
		private Path(String path, Optional<String> hash) {
			super();
			this.path = path;
			this.hash = hash;
		}
		public static Path create(String path) {
			String[] parts = path.split("#");
			if (parts.length > 1) {
				return new Path(parts[0], Optional.of(parts[1]));
			}
			return new Path(parts[0], Optional.<String>absent());
		}
		
		public String getPathWithOutHash() {
			return path;
		}
		
		public Optional<String> getHash() {
			return hash;
		}
		
		public String getFullPath() {
			return hash.isPresent() ? path + "#" + hash.get() : path;
		}
		
		public boolean isRelative() {
			return isJustHash() || ! path.startsWith("/");
		}
		
		public boolean isJustHash() {
			return hash.isPresent() && nullToEmpty(path).isEmpty();
		}
		
		public Path parent() {
			check.state(! isJustHash(), "no parent for just hash");
			int index = path.lastIndexOf("/");
			check.state( index > -1, "no parent");
			String parentPath = path.substring(0, index);
			return Path.create(parentPath);
		}
		
		public Path fromRelative(Path p) {
			check.state(p.isRelative(), "path should be relative");
			final Path r;
			if (p.isJustHash()) {
				r = Path.create(this.getPathWithOutHash() + p.getFullPath());
			}
			else {
				r = Path.create(this.parent().getPathWithOutHash() + "/" + p.getFullPath());
			}
			return r;
		}
		@Override
		public String toString() {
			return "Path [" + getFullPath() + "]";
		}
		
		
	}
	
	
	
	private enum PSTATE {
		HASH,
		REFERENCE,
		OTHER,
	}
	
	protected static interface ResourceLoader {
		public String load(String path);
		public static ResourceLoader DEFAULT_LOADER = new ResourceLoader() {
			@Override
			public String load(String path) {
				try {
					String p = check.notNull(path, "path is null");
					check.state(p.startsWith("/"), "path should start with '/' but was: {}", path);
					p = p.substring(1);
					return Resources.toString(Resources.getResource(p), 
							Charsets.UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		public static class CachedResourceLoader implements ResourceLoader {
			@Override
			public String load(String path) {
				try {
					String p = check.notNull(path, "path is null");
					check.state(p.startsWith("/"), "path should start with '/' but was: {}", path);
					p = p.substring(1);
					return ResourceUtils.getClasspathResourceAsString(p);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		public static ResourceLoader CACHED_LOADER = new CachedResourceLoader();
	}
	
	private static Cache<String, ExpandedSql> fromPathCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(ResourceUtils.expire, TimeUnit.SECONDS)
			.build();
	
	private static Cache<String, String> fromPathToStringCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.expireAfterAccess(ResourceUtils.expire, TimeUnit.SECONDS)
			.build();
	
	public static String parseFromPath(final String path) {
		try {
			return fromPathToStringCache.get(path, new Callable<String>() {
				@Override
				public String call() throws Exception {
					return _parseFromPath(path);
				}
			});
		} 
		catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
		catch (UncheckedExecutionException e) {
			if (e.getCause() instanceof JirmIllegalStateException 
					|| e.getCause() instanceof JirmIllegalArgumentException) {
				throw (RuntimeException) e.getCause();
			}
			throw e;
		}
	}
	
	private static String _parseFromPath(final String path) {
		Map<String, ExpandedSql> c = newLinkedHashMap(fromPathCache.asMap());
		Parser p = new Parser(CachedResourceLoader.CACHED_LOADER, c);
		fromPathCache.asMap().putAll(c);
		return p.expand(path).join();
	}
	
	public static String parseFromPath(Class<?> k , final String path) {
		String resolved;
		resolved = "/" + check.notNull(ResourceUtils.resolvePath(k, path), "bug path failed: {}", path);
		return parseFromPath(resolved);
	}
	
	public static class Parser {

		private final ResourceLoader loader;
		private final Map<String, ExpandedSql> cache;
		
		private Parser(ResourceLoader resourceLoader, Map<String, ExpandedSql> cache) {
			super();
			this.loader = resourceLoader;
			this.cache = cache;
		}
		
		public static Parser create() {
			return new Parser(ResourceLoader.DEFAULT_LOADER, Maps.<String, ExpandedSql> newLinkedHashMap());
		}

		public ExpandedSql expand(String path) {
			return _expand(path, Sets.<String>newHashSet());
		}

		protected ExpandedSql _expand(String path, Set<String> seenPaths) {
			check.state(! seenPaths.contains(path), "Cycle detected for path: {}, paths involved: {}",  
					path, seenPaths);
			if (seenPaths.contains(path)) {
			}
			Path p = Path.create(path);
			ExpandedSql loaded = cache.get(p.getFullPath());
			if (loaded != null)
				return loaded;
			boolean hash = p.getHash().isPresent();
			final ExpandedSql e;
			if (hash) {
				ExpandedSql ef = cache.get(p.getPathWithOutHash());
				final FileDeclarationSql fd;
				if (ef != null) {
					fd = (FileDeclarationSql) ef.getDeclaration();
				}
				else {
					fd = loadFile(p.getPathWithOutHash(), loader);
				}
				HashDeclarationSql h = fd.getHashDeclarations().get(p.getHash().get());
				check.notNull(h, "Not Found: {}, Hash #{} not found in file: {}", 
						p.getFullPath(), p.getHash().get(), p.getPathWithOutHash());
				
				e = _expand(h, seenPaths);
			}
			else {
				FileDeclarationSql fd = loadFile(path, loader);
				e = _expand(fd, seenPaths);
			}
			cache.put(path, e);
			return e;
		}

		protected ExpandedSql _expand(ReferenceSql f, Set<String> seenPaths) {
			Path cp = f.getCurrentPath();
			Path rp = f.getReferencePath();
			final Path path;
			if (rp.isRelative()) {
				path = cp.fromRelative(rp);
			}
			else {
				path = rp;
			}
			return _expand(path.getFullPath(), seenPaths);
		}

		protected ExpandedSql _expand(DeclarationSql f, Set<String> seenPaths) {

			ImmutableList.Builder<String> sb = ImmutableList.builder();
			
			List<String> lines = f.getDeclaredSql();

			List<ReferenceSql> references = f.getReferences();
			Map<Integer, ExpandedSql> byLine = Maps.newLinkedHashMap();
			Map<Integer, ReferenceSql> referenceSql = Maps.newLinkedHashMap();
			
			for (ReferenceSql r : references) {

				ExpandedSql e = _expand(r, seenPaths);
				r.getStartIndex();
				
				DeclarationSql ds = e.getDeclaration();
				boolean validate = ! r.isSame() || ds.inner().equals(r.inner());
				check.state(validate, 
						"Reference '> {}' in {} at line: {}" +
						" does" +
						"\nNOT MATCH declaration {} at line: {}" +
						"\nREFERENCE:" +
						"\n{}\n" +
						"DECLARATION:" +
						"\n{}\n",
						r.getReferencePath().getFullPath(),
						r.getCurrentPath().getFullPath(),
						r.getStartIndex(),
						ds.getPath().getFullPath(),
						ds.getStartIndex(),
						r.join(), 
						ds.join());
				byLine.put(r.getStartIndex(), e);
				referenceSql.put(r.getStartIndex(), r);
			}
			
			
			final Set<String> hashLinesToRemove;
			if (f.isFile()) {
				FileDeclarationSql fd = (FileDeclarationSql) f;
				hashLinesToRemove = Sets.newHashSetWithExpectedSize(fd.getHashDeclarations().size() * 2);
				for (HashDeclarationSql hd : fd.getHashDeclarations().values()) {
					hashLinesToRemove.add(hd.header());
					hashLinesToRemove.add(hd.footer());
				}
			}
			else {
				hashLinesToRemove = ImmutableSet.<String>of();
			}
			
			/*
			 * Replace the reference SQL with the expanded SQL
			 */
			for (int i = 0; i < lines.size();) {
				ExpandedSql e = byLine.get(i + f.getStartIndex());
				ReferenceSql r = referenceSql.get(i + f.getStartIndex());
				
				String line = lines.get(i);
				if (e != null) {
					sb.addAll(e.inner());
					i += r.getLength();
				}
				else if (hashLinesToRemove.contains(line)) {
					i++;
				}
				else {
					sb.add(line);
					i++;
				}
			}
			return ExpandedSql.create(f, sb.build());
		}


	}
	
	
	private static FileDeclarationSql loadFile(String path, ResourceLoader loader) {
		return processFile(path, loader.load(path));
	}
	
	public static FileDeclarationSql processFile(String path, String sql) {
		try {
			return _processFile(path, sql);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private static FileDeclarationSql _processFile(String path, String sql) throws IOException {
		
		LineReader lr = new LineReader(new StringReader(sql));
		String line;
		ImmutableList.Builder<ReferenceSql> references = ImmutableList.builder();
		ImmutableMap.Builder<String,HashDeclarationSql> hashes = ImmutableMap.builder();
		ImmutableList.Builder<ReferenceSql> hashReferences = ImmutableList.builder();
		Map<String, HashDeclarationSql> nameToHash = newHashMap();
		
		ImmutableList.Builder<String> referenceContent = ImmutableList.builder();
		ImmutableList.Builder<String> hashContent = ImmutableList.builder();
		ImmutableList.Builder<String> fileContent = ImmutableList.builder();

		
		boolean first = true;
		PSTATE state = PSTATE.OTHER;
		PSTATE previousState = PSTATE.OTHER;
		
		String currentHash = null;
		String currentReference = null;
		Map<String,List<String>> currentReferenceParameters = ImmutableMap.of();
		int hashStartIndex = 0;
		int referenceStartIndex = 0;
		int lineIndex = 0;
		
		String PE = "For path: '{}', ";
		
		while ( (line = lr.readLine()) != null) {
			if (first) first = false;
			Matcher m = tokenPattern.matcher(line);
			String tag;
			if (m.matches() && (tag = m.group(1)) != null && ! (tag = tag.trim()).isEmpty()) {
				if (tag != null && tag.startsWith("#")) {
					check.state(state != PSTATE.HASH, PE + "Cannot hash within hash at line: {}.", path, lineIndex);
					state = PSTATE.HASH;
					hashContent = ImmutableList.builder();
					hashReferences = ImmutableList.builder();
					currentHash = tag.substring(1).trim();
					HashDeclarationSql existing = nameToHash.get(currentHash);
					if (existing != null) {
						throw check.stateInvalid( 
								PE + "Hash: '#{}' already defined at line: {}, new definition at line: {}",
								path,
								currentHash, existing.getStartIndex(), lineIndex);
					}
					hashContent.add(line);
					hashStartIndex = lineIndex;
				}
				else if (tag != null && tag.startsWith(">")) {
					check.state(state != PSTATE.REFERENCE, PE + "Cannot reference within reference at line: {}.", path, lineIndex);
					previousState = state;
					state = PSTATE.REFERENCE;
					referenceContent = ImmutableList.builder();
					ReferenceHeader h = ReferenceHeader.parse(tag);
					currentReference = h.getPath().getFullPath();
					currentReferenceParameters = h.getParameters();
					
					check.state(! currentReference.isEmpty(), PE + "No reference defined", path);
					referenceStartIndex = lineIndex;
					referenceContent.add(line);
					if (previousState == PSTATE.HASH) {
						hashContent.add(line);
					}
				}
				else if (tag != null && tag.equals("<")) {
					check.state(state == PSTATE.REFERENCE, PE + "Invalid close of reference line: {}", path, lineIndex);
					state = previousState;
					int length = lineIndex - referenceStartIndex + 1;
					referenceContent.add(line);
					check.state(length > -1, "length should be greater than -1");
					check.state(length >= 0, PE + "Hash Line index incorrect. Index: {}, Reference start: {}", path, lineIndex, referenceStartIndex);
					ReferenceSql rsql = new ReferenceSql(currentReference, 
							path, referenceContent.build(), referenceStartIndex, length, currentReferenceParameters);
					references.add(rsql);
					if (PSTATE.HASH == previousState) {
						hashReferences.add(rsql);
						hashContent.add(line);
					}
				}
				else if (tag != null && tag.startsWith("/")) {
					check.state(state == PSTATE.HASH, PE + "Hash not started or reference not finished line: {}", path, lineIndex);
					String t = tag.substring(1).trim();
					check.state(! t.isEmpty(), PE + "No close hash is defined at line: {}", path, lineIndex);
					check.state(t.equals(currentHash), PE + "Should be current hash tag: {} at line: {}", path, currentHash, lineIndex);
					state = PSTATE.OTHER;
					int length = lineIndex - hashStartIndex + 1;
					hashContent.add(line);
					check.state(length >= 0, PE + "Hash Line index incorrect. Index: {}, Hash start: {}", path, lineIndex, hashStartIndex);
					HashDeclarationSql hash = new HashDeclarationSql(path + "#" + currentHash, hashContent.build(), hashReferences.build(), hashStartIndex, length);
					nameToHash.put(currentHash, hash);
					hashes.put(currentHash, hash);
				}
				else {
					throw check.stateInvalid(PE + "Malformed hash or reference: {} at line: {}", path, tag, lineIndex);
				}
			}
			else {
				if (PSTATE.HASH == state || PSTATE.HASH == previousState) {
					hashContent.add(line);
				}
				if (PSTATE.REFERENCE == state) {
					referenceContent.add(line);
				}
			}
			fileContent.add(line);
			
			lineIndex++;
		}

		check.state(PSTATE.OTHER == state, "Reference or hash not closed");
		FileDeclarationSql f = new FileDeclarationSql(path, fileContent.build(), references.build(), hashes.build());
		return f;

	}
	

}
