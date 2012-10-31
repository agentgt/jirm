package co.jirm.orm.builder.select;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.List;

import co.jirm.core.sql.MutableParameterizedSql;

import com.google.common.collect.Lists;


public abstract class AbstractSqlParameterizedSelectClause<T, I> extends MutableParameterizedSql<T> implements SqlSelectClause<I>{

	protected final List<SelectClause<I>> children = Lists.newArrayList();
	private final SelectClause<I> parent;
	private final SelectClauseType type;

	protected AbstractSqlParameterizedSelectClause(SelectClause<I> parent, SelectClauseType type, String sql) {
		super(sql);
		this.parent = parent;
		this.type = type;
	}
	
	public SelectClauseType getType() {
		return type;
	}
	
	@Override
	public boolean isNoOp() {
		return (nullToEmpty(getSql()).trim().isEmpty());
	}
	
	protected <K extends SelectClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I query() {
		return parent.query();
	}

}
