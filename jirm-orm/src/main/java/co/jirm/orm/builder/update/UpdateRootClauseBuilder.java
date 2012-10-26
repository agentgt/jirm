package co.jirm.orm.builder.update;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;



public class UpdateRootClauseBuilder<I> implements UpdateClause<I> {
	
	private final UpdateClauseTransform<UpdateRootClauseBuilder<I>, I> handoff;
	protected final List<UpdateClause<I>> children = Lists.newArrayList();
	
	private UpdateRootClauseBuilder(UpdateClauseTransform<UpdateRootClauseBuilder<I>, I> handoff) {
		super();
		this.handoff = handoff;
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
	public UpdateClauseType getType() {
		return UpdateClauseType.ROOT;
	}
	
	@Override
	public boolean isNoOp() {
		return true;
	}
	@Override
	public I query() {
		return handoff.transform(this);
	}

	@Override
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		for (UpdateClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

}
