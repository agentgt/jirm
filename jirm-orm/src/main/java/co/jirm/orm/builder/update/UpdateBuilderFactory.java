package co.jirm.orm.builder.update;

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
			String result = writerStrategy.replacePropertyPaths(definition, sb.toString());
			return result;
		}
	}
}
