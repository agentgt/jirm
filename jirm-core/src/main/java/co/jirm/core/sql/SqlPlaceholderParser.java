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

import static com.google.common.base.Strings.nullToEmpty;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static co.jirm.core.util.JirmPrecondition.check;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.io.LineReader;


public class SqlPlaceholderParser {
	
	private static final Pattern tokenPattern = Pattern.compile("^(.*?)-- ?\\{(.*?)\\}[ \t]*$");
	private static final LoadingCache<CacheKey, ParsedSql> cache = CacheBuilder
			.newBuilder()
			.maximumSize(100)
			.build(new CacheLoader<CacheKey, ParsedSql>() {
				@Override
				public ParsedSql load(CacheKey key) throws Exception {
					return _parseSql(key.sql, key.config);
				}
				
			});
	
	public static ParsedSql parseSql(String sql) {
		return _parseSqlCache(sql, SqlPlaceholderParserConfig.DEFAULT);
	}
	
	private static ParsedSql _parseSqlCache(String sql, SqlPlaceholderParserConfig config) {
		try {
			return cache.get(new CacheKey(config, sql));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static ParsedSql _parseSql(String sql, SqlPlaceholderParserConfig config) throws IOException {
		
		StringBuilder sb = new StringBuilder(sql.length());
		LineReader lr = new LineReader(new StringReader(sql));
		String line;
		ImmutableList.Builder<PlaceHolder> placeHolders = ImmutableList.builder();
		int nameIndex = 0;
		int positionalIndex = 0;
		int position = 0;
		boolean first = true;
		
		while ( (line = lr.readLine()) != null) {
			if (first) first = false;
			else if (! config.isStripNewLines()) sb.append("\n");
			Matcher m = tokenPattern.matcher(line);
			if (m.matches()) {
				String leftHand = m.group(1);
				int start = m.start(1);
				check.state(start == 0, "start should be 0");
				int[] ind = parseForReplacement(leftHand);
				check.state(ind != null, "Problem parsing {}", line);
				String before = leftHand.substring(0, ind[0]);
				String after = leftHand.substring(ind[1], leftHand.length());
				sb.append(before);
				sb.append("?");
				sb.append(after);
				
				String name = null;
				final PlaceHolder ph;
				if (m.groupCount() == 2 && ! (name = nullToEmpty(m.group(2))).isEmpty() ) {
					ph = new NamePlaceHolder(position, name, nameIndex);
					++nameIndex;
				}
				else {
					ph = new PositionPlaceHolder(position, positionalIndex);
					++positionalIndex;
				}
				placeHolders.add(ph);
				++position;
			}
			else {
				sb.append(line);
			}
		}
		if (sql.endsWith("\r\n") || sql.endsWith("\n") || sql.endsWith("\r")) {
			if (! config.isStripNewLines() )
				sb.append("\n");
		}
		return new ParsedSql(config, sql, sb.toString(), placeHolders.build());
	}
	
	protected static int[] parseForReplacement (String leftHand) {
		int end = leftHand.length();
		while (end > 0 && Character.isWhitespace(leftHand.charAt(end - 1))) {
			--end;
		}
		if (end == 0) return null;
		int start = end - 1;
		if (leftHand.charAt(start) == '\'') {
			//looking for another '
			if (start == 0) return null;
			int apos = 1;
			--start;
			while ( start >= 0 ) {
				if (leftHand.charAt(start) == '\'') {
					apos++;
					if (start == 0) {
						break;
					}
					if (leftHand.charAt(start - 1) != '\'' && apos % 2 == 0) {
						break;
					}
				}
				--start;
			}
			if (apos < 2 || apos % 2 != 0) {
				return null;
			}
		}
		else {
			//looking for space or end.
			while ( start > 0 && ! Character.isWhitespace(leftHand.charAt(start - 1))) {
				--start;
			}
		}
		return new int [] {start, end};
	}
	
	public static class ParsedSql {
		private final String resultSql;
		private final String originalSql;
		private final List<PlaceHolder> placeHolders;
		private final SqlPlaceholderParserConfig config;
		
		private ParsedSql(SqlPlaceholderParserConfig config, 
				String originalSql, String resultSql, ImmutableList<PlaceHolder> placeHolders) {
			super();
			this.config = config;
			this.originalSql = originalSql;
			this.resultSql = resultSql;
			this.placeHolders = placeHolders;
		}
		public String getResultSql() {
			return resultSql;
		}
		
		public String getOriginalSql() {
			return originalSql;
		}
		public List<PlaceHolder> getPlaceHolders() {
			return placeHolders;
		}
		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("resultSql", resultSql)
				.add("originalSql", originalSql)
				.add("placeHolders", placeHolders).toString();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((config == null) ? 0 : config.hashCode());
			result = prime * result + ((originalSql == null) ? 0 : originalSql.hashCode());
			result = prime * result + ((placeHolders == null) ? 0 : placeHolders.hashCode());
			result = prime * result + ((resultSql == null) ? 0 : resultSql.hashCode());
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
			ParsedSql other = (ParsedSql) obj;
			if (config == null) {
				if (other.config != null)
					return false;
			}
			else if (!config.equals(other.config))
				return false;
			if (originalSql == null) {
				if (other.originalSql != null)
					return false;
			}
			else if (!originalSql.equals(other.originalSql))
				return false;
			if (placeHolders == null) {
				if (other.placeHolders != null)
					return false;
			}
			else if (!placeHolders.equals(other.placeHolders))
				return false;
			if (resultSql == null) {
				if (other.resultSql != null)
					return false;
			}
			else if (!resultSql.equals(other.resultSql))
				return false;
			return true;
		}
		public ImmutableList<Object> mergeParameters(Parameters parameters) {
			check.argument(parameters.getNameParameters().isEmpty() 
					|| parameters.getParameters().isEmpty(), 
					"Mixing of name and positional parameters not supported yet: {}", parameters);
			Optional<PlaceHolderType> allType = allOfType();
			if (parameters.getNameParameters().isEmpty() 
					&& parameters.getParameters().isEmpty()
					&& ! this.getPlaceHolders().isEmpty()) {
				throw check.argumentInvalid("ParsedSql expected arguments but got none: {}, {}", this, parameters);
			}
			else if (parameters.getNameParameters().isEmpty() 
					&& parameters.getParameters().isEmpty()
					&& this.getPlaceHolders().isEmpty()) {
				return ImmutableList.of();
			}
			else if (parameters.getNameParameters().isEmpty() 
					&& ! parameters.getParameters().isEmpty()
					&& this.getPlaceHolders().isEmpty()) {
				/*
				 * The sql probably already contains JDBC placeholders.
				 */
				check.state(this.getOriginalSql().contains("?"), 
						"Expecting already included JDBC placeholders: {} in sql:", parameters, this);
				return parameters.getParameters();
			}
			else if (this.getPlaceHolders().isEmpty() && ! parameters.getNameParameters().isEmpty()) {
				throw check.stateInvalid("Name parameters bound but not defined in sql template " +
						" sql: {}, parameters: {}", this, parameters);
			}
			else if (! allType.isPresent() ){
				throw check.stateInvalid("Mixing of name and positional parameters in parsed" +
						" sql: {}, parameters: {}", this, parameters);
			}
			else if ( allType.get() == PlaceHolderType.POSITION && ! parameters.getNameParameters().isEmpty() ) {
				throw check.stateInvalid("Expected positional parameters in: {} but was passed name parameters: {}", 
						this, parameters);
			}
			else if ( allType.get() == PlaceHolderType.NAME && ! parameters.getParameters().isEmpty() ) {
				check.argument(parameters.getParameters().size() >= getPlaceHolders().size(), 
						"Insufficient positional parameters: {} for ParsedSql name parameters: {}", parameters, this);
				return parameters.getParameters();
			}
			else if ( allType.get() == PlaceHolderType.NAME && ! parameters.getNameParameters().isEmpty() ) {
				ImmutableList.Builder<Object> b = ImmutableList.builder();
				for (PlaceHolder ph : getPlaceHolders()) {
					NamePlaceHolder np = ph.asName();
					String name = np.getName();
					check.state(parameters.getNameParameters().containsKey(name), 
							"ParsedSql wants parameter '{}' but name parameters do not have it: {}", name, parameters);
					Object o = parameters.getNameParameters().get(name);
					b.add(o);
				}
				return b.build();
			}
			else if ( allType.get() == PlaceHolderType.POSITION && ! parameters.getParameters().isEmpty() ) {
				check.argument(parameters.getParameters().size() >= getPlaceHolders().size(), 
						"Insufficient positional parameters: {} for ParsedSql parameters: {}", parameters, this);
				return parameters.getParameters();
			}
			else {
				throw check.stateInvalid("Programming error parsed: {}, parameters: {}", this, parameters);
			}
			
			
		}
		
		public Optional<PlaceHolderType> allOfType() {
			if (placeHolders.isEmpty()) return Optional.absent();
			Iterator<PlaceHolder> it = placeHolders.iterator();
			PlaceHolderType pt = it.next().getType();
			while (it.hasNext()) {
				if (pt != it.next().getType())
					return Optional.absent();
			}
			return Optional.of(pt);
		}
		
	}
	
	public abstract static class PlaceHolder {
		private final int position;
		private final PlaceHolderType type;
		private PlaceHolder(int position, PlaceHolderType type) {
			super();
			this.position = position;
			this.type = type;
		}
		public int getPosition() {
			return position;
		}
		public PlaceHolderType getType() {
			return type;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + position;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			PlaceHolder other = (PlaceHolder) obj;
			if (position != other.position)
				return false;
			if (type != other.type)
				return false;
			return true;
		}
		
		public abstract NamePlaceHolder asName();
		
		public abstract PositionPlaceHolder asPosition();
		
	}
	public static class NamePlaceHolder extends PlaceHolder {
		private final String name;
		private final int namePosition;
		private NamePlaceHolder(int position, String name, int namePosition) {
			super(position, PlaceHolderType.NAME);
			this.name = name;
			this.namePosition = namePosition;
		}
		public String getName() {
			return name;
		}
		public int getNamePosition() {
			return namePosition;
		}
		@Override
		public String toString() {
			return "NamePlaceHolder{name=" + name + "}";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + namePosition;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			NamePlaceHolder other = (NamePlaceHolder) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			}
			else if (!name.equals(other.name))
				return false;
			if (namePosition != other.namePosition)
				return false;
			return true;
		}
		@Override
		public NamePlaceHolder asName() {
			return this;
		}
		@Override
		public PositionPlaceHolder asPosition() {
			throw new UnsupportedOperationException("asPosition is not yet supported");
		}
		
		
	}
	
	public static class PositionPlaceHolder extends PlaceHolder {
		private final int parameterPosition;
		private PositionPlaceHolder(int position, int parameterPosition) {
			super(position, PlaceHolderType.POSITION);
			this.parameterPosition = parameterPosition;
		}
		public int getParameterPosition() {
			return parameterPosition;
		}
		@Override
		public String toString() {
			return "PositionPlaceHolder{parameterPosition=" + parameterPosition + "}";
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + parameterPosition;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			PositionPlaceHolder other = (PositionPlaceHolder) obj;
			if (parameterPosition != other.parameterPosition)
				return false;
			return true;
		}
		@Override
		public NamePlaceHolder asName() {
			throw new UnsupportedOperationException("asName is not yet supported");
		}
		@Override
		public PositionPlaceHolder asPosition() {
			return this;
		}
	}
	
	
	public enum PlaceHolderType {
		NAME, POSITION;
	}
	
	private static class CacheKey {
		private final String sql;
		private final SqlPlaceholderParserConfig config;
		
		private CacheKey(SqlPlaceholderParserConfig config, String sql) {
			super();
			this.config = config;
			this.sql = sql;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((config == null) ? 0 : config.hashCode());
			result = prime * result + ((sql == null) ? 0 : sql.hashCode());
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
			CacheKey other = (CacheKey) obj;
			if (config == null) {
				if (other.config != null)
					return false;
			}
			else if (!config.equals(other.config))
				return false;
			if (sql == null) {
				if (other.sql != null)
					return false;
			}
			else if (!sql.equals(other.sql))
				return false;
			return true;
		}
		
	}

}
