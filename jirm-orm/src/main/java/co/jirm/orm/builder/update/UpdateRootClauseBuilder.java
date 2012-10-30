package co.jirm.orm.builder.update;

import java.util.List;
import java.util.Map;

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
	
	public static <I> UpdateRootClauseBuilder<I> newInstance(UpdateClauseTransform<UpdateRootClauseBuilder<I>, I> factory) {
		return new UpdateRootClauseBuilder<I>(factory);
	}
	
	public SetClauseBuilder<I> set(String property, Object value) {
		return addClause(SetClauseBuilder.newInstance(this).set(property, value));
	}
	
	public SetClauseBuilder<I> setAll(Map<String,Object> m) {
		return addClause(SetClauseBuilder.newInstance(this).setAll(m));
	}
	
	public SetClauseBuilder<I> plus(String property, Number value) {
		return addClause(SetClauseBuilder.newInstance(this).plus(property, value));
	}
	
	public UpdateRootClauseBuilder<I> header(String sql) {
		addFirstClause(UpdateCustomClauseBuilder.newInstance(this, sql));
		return this;
	}
	
	protected <K extends UpdateClause<I>> K addFirstClause(K k) {
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
	public UpdateClauseType getType() {
		return UpdateClauseType.ROOT;
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
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		for (UpdateClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	protected <K extends UpdateClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}

}
