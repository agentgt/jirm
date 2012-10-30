package co.jirm.orm.builder.delete;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;



public class DeleteRootClauseBuilder<I> implements DeleteClause<I> {
	
	private final DeleteClauseTransform<DeleteRootClauseBuilder<I>, I> handoff;
	protected final List<DeleteClause<I>> children = Lists.newArrayList();
	
	private DeleteRootClauseBuilder(DeleteClauseTransform<DeleteRootClauseBuilder<I>, I> handoff) {
		super();
		this.handoff = handoff;
	}
	
	public static <I> DeleteRootClauseBuilder<I> newInstance(DeleteClauseTransform<DeleteRootClauseBuilder<I>, I> factory) {
		return new DeleteRootClauseBuilder<I>(factory);
	}
	
	
	public DeleteRootClauseBuilder<I> header(String sql) {
		addFirstClause(DeleteCustomClauseBuilder.newInstance(this, sql));
		return this;
	}
	
	public DeleteWhereClauseBuilder<I> where() {
		return addClause(DeleteWhereClauseBuilder.newInstance(this));
	}
	
	protected <K extends DeleteClause<I>> K addFirstClause(K k) {
		children.add(0, k);
		return k;
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
	public DeleteClauseType getType() {
		return DeleteClauseType.ROOT;
	}
	
	@Override
	public boolean isNoOp() {
		return true;
	}
	@Override
	public I execute() {
		return handoff.transform(this);
	}

	@Override
	public <C extends DeleteClauseVisitor> C accept(C visitor) {
		for (DeleteClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	protected <K extends DeleteClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}

}
