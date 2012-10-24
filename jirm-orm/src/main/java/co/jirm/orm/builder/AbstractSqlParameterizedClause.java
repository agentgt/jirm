package co.jirm.orm.builder;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.List;

import co.jirm.core.sql.MutableParameterizedSql;

import com.google.common.collect.Lists;


public abstract class AbstractSqlParameterizedClause<T, I> extends MutableParameterizedSql<T> implements SqlClause<I>{

	protected final List<Clause<I>> children = Lists.newArrayList();
	private final Clause<I> parent;
	private final ClauseType type;

	protected AbstractSqlParameterizedClause(Clause<I> parent, ClauseType type, String sql) {
		super(sql);
		this.parent = parent;
		this.type = type;
	}
	
	public ClauseType getType() {
		return type;
	}
	
	@Override
	public boolean isNoOp() {
		return (nullToEmpty(getSql()).trim().isEmpty());
	}
	
	protected <K extends Clause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I query() {
		return parent.query();
	}

}
