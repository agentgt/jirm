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
package co.jirm.orm.builder.update;

import java.util.LinkedHashMap;

import co.jirm.core.execute.SqlUpdateExecutor;
import co.jirm.core.sql.Parameters;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.orm.OrmConfig;
import co.jirm.orm.builder.update.UpdateClause.UpdateClauseTransform;
import co.jirm.orm.writer.SqlWriterStrategy;


public class UpdateBuilderFactory<T> {
	private final SqlUpdateExecutor updateExecutor;
	private final SqlObjectDefinition<T> definition;
	private final SqlWriterStrategy writerStrategy;
	
	
	
	private UpdateBuilderFactory(
			SqlUpdateExecutor updateExecutor, 
			SqlObjectDefinition<T> definition,
			SqlWriterStrategy writerStrategy) {
		super();
		this.updateExecutor = updateExecutor;
		this.definition = definition;
		this.writerStrategy = writerStrategy;
	}
	
	public static <T> UpdateBuilderFactory<T> newInstance(SqlObjectDefinition<T> definition, OrmConfig ormConfig) {
		return new UpdateBuilderFactory<T>(ormConfig.getSqlExecutor(), definition, ormConfig.getSqlWriterStrategy());
	}
	
	public UpdateRootClauseBuilder<Integer> update() {
		StringBuilder sb = new StringBuilder();
		writerStrategy.updateStatementBeforeSet(sb, definition);
		return UpdateRootClauseBuilder.newInstance(new RootClauseHandoff(sb.toString()) {
			@Override
			protected Integer execute(String sql, Object[] values) {
				return updateExecutor.update(sql, values);
			}
		});
	}
	public UpdateCustomClauseBuilder<Integer> sql(final String sql) {
		return UpdateRootClauseBuilder.newInstance(new RootClauseHandoff(null) {
			@Override
			protected Integer execute(String sql, Object[] values) {
				return updateExecutor.update(sql, values);
			}
		}).sql(sql);
	}
	
	public UpdateCustomClauseBuilder<Integer> sqlFromResource(String resource) {
		return UpdateRootClauseBuilder.newInstance(new RootClauseHandoff(null) {
			@Override
			protected Integer execute(String sql, Object[] values) {
				return updateExecutor.update(sql, values);
			}
		}).sqlFromResource(definition.getObjectType(), resource);
	}
	
	public UpdateObjectBuilder<T> update(LinkedHashMap<String, Object> object) {
		return UpdateObjectBuilder.newInstance(this, object);
	}
	
	private abstract class RootClauseHandoff implements UpdateClauseTransform<UpdateRootClauseBuilder<Integer>, Integer> {
		private final String startSql;
		
		private RootClauseHandoff(String startSql) {
			super();
			this.startSql = startSql;
		}

		@Override
		public Integer transform(UpdateRootClauseBuilder<Integer> clause) {
			
			if (startSql != null) {
				clause.header(startSql);
			}
			StringBuilder sb = new StringBuilder();
			
			UpdateClauseVisitors.clauseVisitor(sb).startOn(clause);
			
			String result = replace(sb);
			Parameters parameters = UpdateClauseVisitors.getParameters(clause);
			return execute(result, parameters.mergedParameters().toArray());
		}
		
		protected abstract Integer execute(String sql, Object[] values);

		private String replace(StringBuilder sb) {
			String result = writerStrategy.replaceProperties(definition, sb.toString());
			return result;
		}
	}
	
	public SqlObjectDefinition<T> getDefinition() {
		return definition;
	}
}
