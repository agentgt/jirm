package co.jirm.orm.builder.query;

import java.util.List;

import co.jirm.core.builder.QueryFor;
import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.execute.SqlQueryExecutor;
import co.jirm.core.sql.MutableParameterizedSql;
import co.jirm.core.sql.ParametersSql;
import co.jirm.mapper.SqlObjectExecutorRowMapper;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.orm.OrmConfig;
import co.jirm.orm.builder.query.SelectClause.SelectClauseTransform;
import co.jirm.orm.writer.SqlWriterStrategy;

import com.google.common.base.Optional;


public class SelectBuilderFactory<T> {
	
	private final SqlQueryExecutor queryExecutor;
	private final SqlObjectDefinition<T> definition;
	private final SqlExecutorRowMapper<T> objectRowMapper;
	private final SqlWriterStrategy writerStrategy;
	
	private SelectBuilderFactory(
			SqlQueryExecutor queryExecutor, 
			SqlObjectDefinition<T> definition,
			SqlExecutorRowMapper<T> objectRowMapper,
			SqlWriterStrategy writerStrategy) {
		super();
		this.queryExecutor = queryExecutor;
		this.definition = definition;
		this.objectRowMapper = objectRowMapper;
		this.writerStrategy = writerStrategy;
	}
	
	public static <T> SelectBuilderFactory<T> newInstance(SqlObjectDefinition<T> definition, OrmConfig ormConfig) {
		SqlExecutorRowMapper<T> objectRowMapper = 
				SqlObjectExecutorRowMapper.newInstance(definition, ormConfig.getSqlObjectConfig().getObjectMapper());
		SelectBuilderFactory<T> bf = new SelectBuilderFactory<T>(ormConfig.getSqlExecutor(), definition, objectRowMapper, ormConfig.getSqlWriterStrategy());
		return bf;
	}

	public Long queryForLong(ParametersSql sql) {
		return queryExecutor.queryForLong(sql.getSql(), sql.getParameters().toArray());
	}
	
	public List<T> queryForList(ParametersSql sql) {
		return queryExecutor.queryForList(sql.getSql(), getObjectRowMapper(), sql.getParameters().toArray());
	}
	
	public T queryForObject(ParametersSql sql) {
		return queryExecutor.queryForObject(sql.getSql(), getObjectRowMapper(), sql.getParameters().toArray());
	}
	
	public Optional<T> queryForOptional(ParametersSql sql) {
		return queryExecutor.queryForOptional(sql.getSql(), getObjectRowMapper(), sql.getParameters().toArray());
	}
	
	public SelectRootClauseBuilder<SelectObjectBuilder<T>> select() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		definition.selectParameters(sb);
		sb.append(" FROM ").append(definition.getSqlName());
		definition.innerJoin(sb);
		return SelectRootClauseBuilder.using(new RootClauseHandoff<SelectObjectBuilder<T>>(sb.toString()) {
			@Override
			protected SelectObjectBuilder<T> createBuilder(String sql) {
				return new SelectObjectBuilder<T>(SelectBuilderFactory.this, sql);
			}
			
		});
	}
	
	public CustomClauseBuilder<SelectBuilder<T>> sql(String sql) {
		SelectRootClauseBuilder<SelectBuilder<T>> root = SelectRootClauseBuilder.using(new RootClauseHandoff<SelectBuilder<T>>(null) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(SelectBuilderFactory.this, sql);
			}
		});
		return root.sql(sql);
	}
	
	public CustomClauseBuilder<SelectBuilder<T>> sqlFromResource(String resource) {
		SelectRootClauseBuilder<SelectBuilder<T>> root = SelectRootClauseBuilder.using(new RootClauseHandoff<SelectBuilder<T>>(null) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(SelectBuilderFactory.this, sql);
			}
		});
		return root.sqlFromResource(definition.getObjectType(), resource);
	}
	
	public SelectRootClauseBuilder<CountBuilder<T>> count() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT count(*)");
		sb.append(" FROM ").append(definition.getSqlName());
		definition.innerJoin(sb);
		return SelectRootClauseBuilder.using(new RootClauseHandoff<CountBuilder<T>>(sb.toString()) {
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
	
	public final static class SelectObjectBuilder<T> extends AbstractSelectObjectBuilder<SelectObjectBuilder<T>, T> {

		private SelectObjectBuilder(SelectBuilderFactory<T> queryTemplate, String sql) {
			super(queryTemplate, sql);
		}
		@Override
		protected SelectObjectBuilder<T> getSelf() {
			return this;
		}
	}
	
	public final static class SelectBuilder<T> extends AbstractSelectObjectBuilder<SelectBuilder<T>, T> implements QueryFor {
		
		private SelectBuilder(SelectBuilderFactory<T> queryTemplate, String sql) {
			super(queryTemplate, sql);
		}
		@Override
		protected SelectBuilder<T> getSelf() {
			return this;
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
	
	public static class CountBuilder<T> extends MutableParameterizedSql<CountBuilder<T>> {
		
		private final SelectBuilderFactory<T> queryTemplate;
		
		private CountBuilder(SelectBuilderFactory<T> queryTemplate, String sql) {
			super(sql);
			this.queryTemplate = queryTemplate;
		}

		@Override
		protected CountBuilder<T> getSelf() {
			return this;
		}
		
		public Long forLong() { 
			return queryTemplate.queryForLong(this);
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
	
	
	public SqlExecutorRowMapper<T> getObjectRowMapper() {
		return objectRowMapper;
	}

}
