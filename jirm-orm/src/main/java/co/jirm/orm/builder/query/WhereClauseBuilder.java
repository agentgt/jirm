package co.jirm.orm.builder.query;

import java.util.List;

import co.jirm.orm.builder.Clause;
import co.jirm.orm.builder.ClauseType;
import co.jirm.orm.builder.ClauseVisitor;
import co.jirm.orm.builder.Field;
import co.jirm.orm.builder.ImmutableCondition;
import co.jirm.orm.builder.MutableCondition;
import co.jirm.orm.builder.PropertyPath;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;


public class WhereClauseBuilder<I> implements MutableCondition<WhereClauseBuilder<I>>, Clause<I> {
	
	private ImmutableCondition condition;
	protected final List<Clause<I>> children = Lists.newArrayList();
	private final Clause<I> parent;

	private WhereClauseBuilder(Clause<I> parent, ImmutableCondition condition) {
		this.condition = condition;
		this.parent = parent;
	}
	
	static <I> WhereClauseBuilder<I> newWhereClauseBuilder(Clause<I> parent, ImmutableCondition condition) {
		return new WhereClauseBuilder<I>(parent, condition);
	}
	
	public OrderByClauseBuilder<I> orderBy(String sql) {
		return addClause(OrderByClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(String sql) {
		return addClause(LimitClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(Number i) {
		return addClause(LimitClauseBuilder.newInstanceWithLimit(this, i));
	}
	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(this, sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(this, i));
	}
	
	protected <K extends Clause<I>> K addClause(K k) {
		children.add(k);
		return k;
	}

	@Override
	public WhereClauseBuilder<I> and(String sql) {
		this.condition = this.condition.and(sql);
		return this;
	}
	
	public WhereClauseBuilder<I> where(String sql) {
		this.condition = this.condition.and(sql);
		return this;
	}

	@Override
	public WhereClauseBuilder<I> or(String sql) {
		this.condition = this.condition.or(sql);
		return this;
	}
	
	public PropertyPath<WhereClauseBuilder<I>> property(String property) {
		return andProperty(property);
	}
	
	public WhereClauseBuilder<I> property(String property, Object o) {
		return andProperty(property).eq(o);
	}
	
	public PropertyPath<WhereClauseBuilder<I>> andProperty(String property) {
		return PropertyPath.newPropertyPath(property, this, CombineType.AND);
	}

	public PropertyPath<WhereClauseBuilder<I>> orProperty(String property) {
		return PropertyPath.newPropertyPath(property, this, CombineType.OR);

	}
	
	public Field<WhereClauseBuilder<I>> andField(String field) {
		return Field.newField(field, this, CombineType.AND);
	}

	public Field<WhereClauseBuilder<I>> orField(String field) {
		return Field.newField(field, this, CombineType.OR);

	}
	
	@Override
	public WhereClauseBuilder<I> not() {
		this.condition = this.condition.not();
		return this;
	}

	@Override
	public boolean isNoOp() {
		return this.condition.isNoOp();
	}
	
	
	@Override
	public WhereClauseBuilder<I> set(String key, Object value) {
		this.condition = this.condition.set(key, value);
		return this;
	}
	
	@Override
	public WhereClauseBuilder<I> with(Object ... value) {
		this.condition = this.condition.with(value);
		return this;
	}
	
	@Override
	public <C extends ClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		for (Clause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

	public ImmutableCondition getCondition() {
		return condition;
	}

	@Override
	public ImmutableList<Object> getParameters() {
		return this.condition.getParameters();
	}

	@Override
	public ImmutableMap<String, Object> getNameParameters() {
		return this.condition.getNameParameters();
	}
	
	@Override
	public ImmutableList<Object> mergedParameters() {
		return this.condition.mergedParameters();
	}

	@Override
	public ClauseType getType() {
		return ClauseType.WHERE;
	}

	@Override
	public I query() {
		return parent.query();
	}

}
