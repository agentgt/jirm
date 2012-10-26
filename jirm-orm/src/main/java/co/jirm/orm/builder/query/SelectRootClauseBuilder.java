package co.jirm.orm.builder.query;

import java.util.List;

import co.jirm.orm.builder.ImmutableCondition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


public class SelectRootClauseBuilder<I> implements SelectClause<I> {
	
	private final SelectClauseTransform<SelectRootClauseBuilder<I>, I> factory;
	protected final List<SelectClause<I>> children = Lists.newArrayList();
	
	
	private SelectRootClauseBuilder(SelectClauseTransform<SelectRootClauseBuilder<I>, I> factory) {
		super();
		this.factory = factory;
	}
	@Override
	public ImmutableList<Object> getParameters() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public ImmutableMap<String, Object> getNameParameters() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public ImmutableList<Object> mergedParameters() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public SelectClauseType getType() {
		throw new UnsupportedOperationException("Cannot do that yet");
	}
	@Override
	public boolean isNoOp() {
		return true;
	}
	@Override
	public I query() {
		return factory.transform(this);
	}
	
	public WhereClauseBuilder<I> where() {
		return addClause(WhereClauseBuilder.newWhereClauseBuilder(this, ImmutableCondition.where()));
	}
	
	public WhereClauseBuilder<I> where(String sql) {
		return addClause(WhereClauseBuilder.newWhereClauseBuilder(this, ImmutableCondition.where(sql)));
	}
	
	public SelectRootClauseBuilder<I> header(String sql) {
		addFirstClause(CustomClauseBuilder.newCustomClause(this, sql));
		return this;
	}

	public static <I> SelectRootClauseBuilder<I> using(SelectClauseTransform<SelectRootClauseBuilder<I>, I> factory) {
		return new SelectRootClauseBuilder<I>(factory);
	}
	
	public CustomClauseBuilder<I> sql(String sql) {
		return addClause(CustomClauseBuilder.newCustomClause(this, sql));
	}
	
	public CustomClauseBuilder<I> sqlFromResource(Class<?> k, String resource) {
		return addClause(CustomClauseBuilder.newCustomClause(this, "").useResource(k, resource));
	}
	
	protected <K extends SelectClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	protected <K extends SelectClause<I>> K addFirstClause(K k) {
		children.add(0, k);
		return k;
	}
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		for (SelectClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

}