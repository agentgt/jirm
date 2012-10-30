package co.jirm.orm.builder.update;

import java.util.List;

import co.jirm.orm.builder.AbstractWhereClauseBuilder;
import co.jirm.orm.builder.ImmutableCondition;

import com.google.common.collect.Lists;


public class UpdateWhereClauseBuilder<I> extends AbstractWhereClauseBuilder<UpdateWhereClauseBuilder<I>, I> implements UpdateClause<I> {

	protected final List<UpdateClause<I>> children = Lists.newArrayList();
	private final UpdateClause<I> parent;
	
	private UpdateWhereClauseBuilder(UpdateClause<I> parent, ImmutableCondition condition) {
		super(condition);
		this.parent = parent;
	}
	
	static <I> UpdateWhereClauseBuilder<I> newInstance(UpdateClause<I> parent) {
		return new UpdateWhereClauseBuilder<I>(parent, ImmutableCondition.where());
	}

	@Override
	protected UpdateWhereClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	public UpdateClauseType getType() {
		return UpdateClauseType.WHERE;
	}
	
	protected <K extends UpdateClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I execute() {
		return parent.execute();
	}

	@Override
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		visitor.visit(getSelf());
		for (UpdateClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}


}
