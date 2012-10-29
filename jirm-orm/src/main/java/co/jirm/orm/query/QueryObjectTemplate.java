package co.jirm.orm.query;

import java.util.List;

import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;

import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.core.execute.SqlQueryExecutor;
import co.jirm.core.sql.MutableParameterizedSql;
import co.jirm.core.sql.ParametersSql;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.orm.builder.query.SelectClauseVisitor;
import co.jirm.orm.builder.query.CustomClauseBuilder;
import co.jirm.orm.builder.query.SelectRootClauseBuilder;
import co.jirm.orm.builder.query.SelectClauseVisitors;
import co.jirm.orm.builder.query.SelectClause.SelectClauseTransform;
import co.jirm.orm.writer.SqlWriterStrategy;

import com.google.common.base.Optional;


public class QueryObjectTemplate<T> {
	
	private final SqlQueryExecutor queryExecutor;
	private final SqlObjectDefinition<T> definition;
	private final SqlExecutorRowMapper<T> objectRowMapper;
	private final SqlWriterStrategy writerStrategy;
	
	private QueryObjectTemplate(
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
	
	public SelectRootClauseBuilder<SelectBuilder<T>> select() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		definition.selectParameters(sb);
		sb.append(" FROM ").append(definition.getSqlName());
		definition.innerJoin(sb);
		return SelectRootClauseBuilder.using(new RootClauseHandoff<SelectBuilder<T>>(sb.toString()) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(QueryObjectTemplate.this, sql);
			}
			
		});
	}
	
	public CustomClauseBuilder<SelectBuilder<T>> sql(String sql) {
		SelectRootClauseBuilder<SelectBuilder<T>> root = SelectRootClauseBuilder.using(new RootClauseHandoff<SelectBuilder<T>>(null) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(QueryObjectTemplate.this, sql);
			}
		});
		return root.sql(sql);
	}
	
	public CustomClauseBuilder<SelectBuilder<T>> sqlFromResource(String resource) {
		SelectRootClauseBuilder<SelectBuilder<T>> root = SelectRootClauseBuilder.using(new RootClauseHandoff<SelectBuilder<T>>(null) {
			@Override
			protected SelectBuilder<T> createBuilder(String sql) {
				return new SelectBuilder<T>(QueryObjectTemplate.this, sql);
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
				return new CountBuilder<T>(QueryObjectTemplate.this, sql);
			}
			
			@Override
			public CountBuilder<T> transform(SelectRootClauseBuilder<CountBuilder<T>> clause) {
				clause.where().limit(1);
				return super.transform(clause);
			}
			
		});
	}
	

	
	public static class SelectBuilder<T> extends MutableParameterizedSql<SelectBuilder<T>> {
		private final QueryObjectTemplate<T> queryTemplate;

		private SelectBuilder(QueryObjectTemplate<T> queryTemplate, String sql) {
			super(sql);
			this.queryTemplate = queryTemplate;
		}

		@Override
		protected SelectBuilder<T> getSelf() {
			return this;
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
	
	public static class CountBuilder<T> extends MutableParameterizedSql<CountBuilder<T>> {
		
		private final QueryObjectTemplate<T> queryTemplate;
		
		private CountBuilder(QueryObjectTemplate<T> queryTemplate, String sql) {
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
			b.addAll(SelectClauseVisitor.getParameters(clause));
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
