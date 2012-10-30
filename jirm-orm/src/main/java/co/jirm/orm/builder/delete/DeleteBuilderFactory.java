package co.jirm.orm.builder.delete;

import co.jirm.core.execute.SqlUpdateExecutor;
import co.jirm.core.sql.Parameters;
import co.jirm.mapper.definition.SqlObjectDefinition;
import co.jirm.orm.builder.delete.DeleteClause.DeleteClauseTransform;
import co.jirm.orm.writer.SqlWriterStrategy;


public class DeleteBuilderFactory<T> {
	private final SqlUpdateExecutor updateExecutor;
	private final SqlObjectDefinition<T> definition;
	private final SqlWriterStrategy writerStrategy;
	
	
	
	private DeleteBuilderFactory(SqlUpdateExecutor updateExecutor, SqlObjectDefinition<T> definition,
			SqlWriterStrategy writerStrategy) {
		super();
		this.updateExecutor = updateExecutor;
		this.definition = definition;
		this.writerStrategy = writerStrategy;
	}
	public DeleteRootClauseBuilder<Integer> delete() {
		StringBuilder sb = new StringBuilder();
		sb.append("DELETE ").append(definition.getSqlName()).append(" ");
		return DeleteRootClauseBuilder.newInstance(new RootClauseHandoff(sb.toString()) {
			@Override
			protected Integer execute(String sql, Object[] values) {
				return updateExecutor.update(sql, values);
			}
		});
	}
	private abstract class RootClauseHandoff implements DeleteClauseTransform<DeleteRootClauseBuilder<Integer>, Integer> {
		private final String startSql;
		
		private RootClauseHandoff(String startSql) {
			super();
			this.startSql = startSql;
		}

		@Override
		public Integer transform(DeleteRootClauseBuilder<Integer> clause) {
			
			if (startSql != null) {
				clause.header(startSql);
			}
			StringBuilder sb = new StringBuilder();
			
			DeleteClauseVisitors.clauseVisitor(sb).startOn(clause);
			
			String result = replace(sb);
			Parameters parameters = DeleteClauseVisitors.getParameters(clause);
			return execute(result, parameters.mergedParameters().toArray());
		}
		
		protected abstract Integer execute(String sql, Object[] values);

		private String replace(StringBuilder sb) {
			String result = writerStrategy.replacePropertyPaths(definition, sb.toString());
			return result;
		}
	}
}
