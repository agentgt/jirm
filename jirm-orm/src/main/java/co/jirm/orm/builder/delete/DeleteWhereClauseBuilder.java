package co.jirm.orm.builder.delete;

import java.util.List;

import co.jirm.orm.builder.AbstractWhereClauseBuilder;
import co.jirm.orm.builder.ImmutableCondition;

import com.google.common.collect.Lists;


public class DeleteWhereClauseBuilder<I> extends AbstractWhereClauseBuilder<DeleteWhereClauseBuilder<I>, I> implements DeleteClause<I> {

	protected final List<DeleteClause<I>> children = Lists.newArrayList();
	private final DeleteClause<I> parent;
	
	private DeleteWhereClauseBuilder(DeleteClause<I> parent, ImmutableCondition condition) {
		super(condition);
		this.parent = parent;
	}
	
	static <I> DeleteWhereClauseBuilder<I> newInstance(DeleteClause<I> parent) {
		return new DeleteWhereClauseBuilder<I>(parent, ImmutableCondition.where());
	}

	@Override
	protected DeleteWhereClauseBuilder<I> getSelf() {
		return this;
	}
	
	
	public DeleteClauseType getType() {
		return DeleteClauseType.WHERE;
	}
	
	protected <K extends DeleteClause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}
	
	@Override
	public I execute() {
		return parent.execute();
	}

	@Override
	public <C extends DeleteClauseVisitor> C accept(C visitor) {
		visitor.visit(getSelf());
		for (DeleteClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}


}
