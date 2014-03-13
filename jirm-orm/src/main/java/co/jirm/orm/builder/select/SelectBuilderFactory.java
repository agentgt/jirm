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
package co.jirm.orm.builder.select;

import java.util.List;

import co.jirm.core.builder.QueryForNumber;
import co.jirm.core.builder.TypedQueryFor;
import co.jirm.core.execute.SqlMultiValueRowMapper;
import co.jirm.core.execute.SqlQueryExecutor;
import co.jirm.core.execute.SqlSingleValueRowMapper;
import co.jirm.core.sql.MutableParameterizedSql;
import co.jirm.core.sql.ParametersSql;
import co.jirm.mapper.SqlObjectExecutorRowMapper;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.orm.OrmConfig;
import co.jirm.orm.builder.select.SelectClause.SelectClauseTransform;
import co.jirm.orm.writer.SqlWriterStrategy;

import com.google.common.base.Optional;


public class SelectBuilderFactory<T> {
	
	private final SqlQueryExecutor queryExecutor;
	private final SqlObjectDefinition<T> definition;
	private final SqlMultiValueRowMapper<T> objectRowMapper;
	private final SqlSingleValueRowMapper singleValueRowMapper;
	private final SqlWriterStrategy writerStrategy;
	 
	private SelectBuilderFactory(
			SqlQueryExecutor queryExecutor, 
			SqlObjectDefinition<T> definition,
			SqlMultiValueRowMapper<T> objectRowMapper,
			SqlWriterStrategy writerStrategy) {
		super();
		this.queryExecutor = queryExecutor;
		this.definition = definition;
		this.objectRowMapper = objectRowMapper;
		this.writerStrategy = writerStrategy;
		this.singleValueRowMapper = SqlSingleValueRowMapper.DEFAULT;
	}
	
	public static <T> SelectBuilderFactory<T> newInstance(SqlObjectDefinition<T> definition, OrmConfig ormConfig) {
		SqlMultiValueRowMapper<T> objectRowMapper = 
				SqlObjectExecutorRowMapper.newInstance(definition, ormConfig.getSqlObjectConfig().getObjectMapper());
		SelectBuilderFactory<T> bf = new SelectBuilderFactory<T>(ormConfig.getSqlExecutor(), definition, objectRowMapper, ormConfig.getSqlWriterStrategy());
		return bf;
	}
	

	public Long queryForLong(ParametersSql sql) {
		return queryExecutor.queryForLong(sql.getSql(), sql.mergedParameters().toArray());
	}
	
	public List<T> queryForList(ParametersSql sql) {
		return queryExecutor.queryForList(sql.getSql(), getObjectRowMapper(), sql.mergedParameters().toArray());
	}
	
	public T queryForObject(ParametersSql sql) {
		return queryExecutor.queryForObject(sql.getSql(), getObjectRowMapper(), sql.mergedParameters().toArray());
	}
	
	public Optional<T> queryForOptional(ParametersSql sql) {
		return queryExecutor.queryForOptional(sql.getSql(), getObjectRowMapper(), sql.mergedParameters().toArray());
	}
	
	public <O> List<O> queryForListOf(ParametersSql sql, Class<O> type) {
		return queryExecutor.queryForList(sql.getSql(), singleValueRowMapper, type, sql.mergedParameters().toArray());
	}
	
	public <O> O queryForObjectOf(ParametersSql sql, Class<O> type) {
		return queryExecutor.queryForObject(sql.getSql(), singleValueRowMapper, type, sql.mergedParameters().toArray());
	}
	
	public <O> Optional<O> queryForOptionalOf(ParametersSql sql, Class<O> type) {
		return queryExecutor.queryForOptional(sql.getSql(), singleValueRowMapper, type, sql.mergedParameters().toArray());
	}
	
	public SelectRootClauseBuilder<? extends TypedQueryFor<T>> select() {
		StringBuilder sb = new StringBuilder();
		writerStrategy.selectStatementBeforeWhere(sb, definition);
		return SelectRootClauseBuilder.newInstance(new RootClauseHandoff<SelectBuilder<T>>(sb.toString()) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(SelectBuilderFactory.this, sql);
			}
		});
	}
	
	public SelectCustomClauseBuilder<? extends TypedQueryFor<T>> sql(String sql) {
		SelectRootClauseBuilder<SelectBuilder<T>> root = SelectRootClauseBuilder.newInstance(new RootClauseHandoff<SelectBuilder<T>>(null) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(SelectBuilderFactory.this, sql);
			}
		});
		return root.sql(sql);
	}
	
	public SelectCustomClauseBuilder<? extends TypedQueryFor<T>> sqlFromResource(String resource) {
		SelectRootClauseBuilder<SelectBuilder<T>> root = SelectRootClauseBuilder.newInstance(new RootClauseHandoff<SelectBuilder<T>>(null) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(SelectBuilderFactory.this, sql);
			}
		});
		return root.sqlFromResource(definition.getObjectType(), resource);
	}
	
	//FIXME This may not work.
	public SelectRootClauseBuilder<? extends QueryForNumber> count() {
		//TODO its hard to know whether or not to innerjoin to get an accurate count here.
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(*)");
		sb.append(" FROM ").append(definition.getSqlName());
		//definition.innerJoin(sb);
		SelectRootClauseBuilder<? extends QueryForNumber> r = SelectRootClauseBuilder.newInstance(new RootClauseHandoff<CountBuilder<T>>(sb.toString()) {
			@Override
			protected CountBuilder<T> createBuilder(String sql) {
				return new CountBuilder<T>(SelectBuilderFactory.this, sql);
			}
			
			@Override
			public CountBuilder<T> transform(SelectRootClauseBuilder<CountBuilder<T>> clause) {
				clause.where().limit(1);
				return super.transform(clause);
			}
			
		});
		return r;
	}
	

	
	static abstract class AbstractSelectObjectBuilder<B, T> extends MutableParameterizedSql<B> {
		protected final SelectBuilderFactory<T> queryTemplate;

		protected AbstractSelectObjectBuilder(SelectBuilderFactory<T> queryTemplate, String sql) {
			super(sql);
			this.queryTemplate = queryTemplate;
		}
		
		public List<T> forList() { 
			return queryTemplate.queryForList(this);
		}
		
		public T forObject() {
			return queryTemplate.queryForObject(this);
		}
		
		public Optional<T> forOptional() {
			return queryTemplate.queryForOptional(this);
		}
		
	}
	
	public final static class SelectBuilder<T> extends AbstractSelectObjectBuilder<SelectBuilder<T>, T> implements TypedQueryFor<T> {
		
		private SelectBuilder(SelectBuilderFactory<T> queryTemplate, String sql) {
			super(queryTemplate, sql);
		}
		@Override
		protected SelectBuilder<T> getSelf() {
			return this;
		}
		
		public <O> List<O> forListOf(Class<O> k) { 
			return queryTemplate.queryForListOf(this, k);
		}
		
		public <O> O forObjectOf(Class<O> k) {
			return queryTemplate.queryForObjectOf(this, k);
		}
		
		public <O> Optional<O> forOptionalOf(Class<O> k) {
			return queryTemplate.queryForOptionalOf(this, k);
		}
		
		@Override
		public int forInt() {
			throw new UnsupportedOperationException("forInt is not yet supported");
		}
		@Override
		public long forLong() {
			return queryTemplate.queryForLong(getSelf());
		}
		
	}
	
	public static class CountBuilder<T> extends MutableParameterizedSql<CountBuilder<T>> implements QueryForNumber {
		
		private final SelectBuilderFactory<T> queryTemplate;
		
		private CountBuilder(SelectBuilderFactory<T> queryTemplate, String sql) {
			super(sql);
			this.queryTemplate = queryTemplate;
		}

		@Override
		protected CountBuilder<T> getSelf() {
			return this;
		}
		
		public long forLong() { 
			return queryTemplate.queryForLong(this);
		}

		@Override
		public int forInt() {
			throw new UnsupportedOperationException("forInt is not yet supported");
		}
		
	}
	
	private abstract class RootClauseHandoff<B extends MutableParameterizedSql<B>> implements SelectClauseTransform<SelectRootClauseBuilder<B>, B> {
		private final String startSql;
		
		private RootClauseHandoff(String startSql) {
			super();
			this.startSql = startSql;
		}

		@Override
		public B transform(SelectRootClauseBuilder<B> clause) {
			
			if (startSql != null) {
				clause.header(startSql);
			}
			StringBuilder sb = new StringBuilder();
			
			SelectClauseVisitors.clauseVisitor(sb).startOn(clause);
			
			String result = replace(sb);
			
			B b = createBuilder(result);
			b.addAll(SelectClauseVisitors.getParameters(clause));
			return b;
		}
		
		protected abstract B createBuilder(String sql);

		private String replace(StringBuilder sb) {
			String result = writerStrategy.replacePropertyPaths(definition, sb.toString());
			return result;
		}
	}
	
	
	public SqlMultiValueRowMapper<T> getObjectRowMapper() {
		return objectRowMapper;
	}

}
