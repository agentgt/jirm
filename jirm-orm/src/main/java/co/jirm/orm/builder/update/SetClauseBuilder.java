package co.jirm.orm.builder.update;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.Map;
import java.util.Map.Entry;


public class SetClauseBuilder<I> extends AbstractSqlParameterizedUpdateClause<SetClauseBuilder<I>, I> {
	
	private SetClauseBuilder(UpdateClause<I> parent, String sql) {
		super(parent, UpdateClauseType.SET, sql);
	}
	
	static <I> SetClauseBuilder<I> newInstance(UpdateClause<I> parent) {
		return new SetClauseBuilder<I>(parent, "");
	}
	
	public SetClauseBuilder<I> set(String property, Object value) {
		return setProperty(property, value);
	}
	
	public SetClauseBuilder<I> setAll(Map<String,Object> m) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for(Entry<String,Object> e : m.entrySet()) {
			if (first) { 
				first = false;
				String sql = getSql();
				if (! nullToEmpty(sql).isEmpty() ) {
					sb.append(getSql()).append(", ");
				}
			}
			else {
				sb.append(", ");
			}
			_writeProperty(sb, e.getKey(), e.getValue());
		}
		setSql(sb.toString());
		return getSelf();
	}
	
	public SetClauseBuilder<I> setProperty(String property, Object value) {
		StringBuilder sb = new StringBuilder();
		if (! nullToEmpty(getSql()).isEmpty()) {
			sb.append(getSql()).append(", ");
		}
		_writeProperty(sb, property, value);
		setSql(sb.toString());
		return getSelf();
	}
	
	private static void _writeProperty(StringBuilder sb, String property, Object value) {
		sb.append("{{").append(property).append("}} = ").append("?");
	}
	
	public SetClauseBuilder<I> setField(String field, Object value) {
		StringBuilder sb = new StringBuilder();
		if (! nullToEmpty(getSql()).isEmpty()) {
			sb.append(getSql()).append(", ");
		}
		sb.append(field).append(" = ").append("?");
		setSql(sb.toString());
		return getSelf();
	}
	
	public UpdateWhereClauseBuilder<I> where() {
		return UpdateWhereClauseBuilder.newInstance(getSelf());
	}

	@Override
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		for (UpdateClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

	@Override
	protected SetClauseBuilder<I> getSelf() {
		return this;
	}

}
