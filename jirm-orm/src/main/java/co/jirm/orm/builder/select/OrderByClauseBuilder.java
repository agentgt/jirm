/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.jirm.orm.builder.select;


public class OrderByClauseBuilder<I> extends AbstractSqlParameterizedSelectClause<OrderByClauseBuilder<I>, I> 
	implements OrderByPartial<OrderByClauseBuilder<I>> {
	
	private ImmutableOrderByPartial orderByPartial;
	
	private OrderByClauseBuilder(SelectClause<I> parent, String sql) {
		super(parent, SelectClauseType.ORDERBY, sql);
	}
	
	public static <I> OrderByClauseBuilder<I> newInstanceForCustom(SelectClause<I> parent, String sql) {
		OrderByClauseBuilder<I> b = new OrderByClauseBuilder<I>(parent, sql);
		return b;
	}
	
	public static <I> OrderByClauseBuilder<I> newInstanceForProperty(SelectClause<I> parent, String sql) {
		OrderByClauseBuilder<I> b = new OrderByClauseBuilder<I>(parent, "");
		b.orderByPartial = ImmutableOrderByPartial.newInstance("{{" + sql + "}}");
		return b;
	}
	
	public static <I> OrderByClauseBuilder<I> newInstanceForField(SelectClause<I> parent, String sql) {
		OrderByClauseBuilder<I> b = new OrderByClauseBuilder<I>(parent, "");
		b.orderByPartial = ImmutableOrderByPartial.newInstance(sql);
		return b;
	}

	@Override
	public boolean isNoOp() {
		return super.isNoOp() && orderByPartial == null;
	}

	@Override
	protected OrderByClauseBuilder<I> getSelf() {
		return this;
	}
	
	@Override
	public <C extends SelectClauseVisitor> C accept(C visitor) {
		visitor.visit(this);
		for (SelectClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}
	
	public LimitClauseBuilder<I> limit(String sql) {
		return addClause(LimitClauseBuilder.newInstance(this, sql));
	}
	public LimitClauseBuilder<I> limit(Number i) {
		return addClause(LimitClauseBuilder.newInstanceWithLimit(this, i));
	}
	public OffsetClauseBuilder<I> offset(String sql) {
		return addClause(OffsetClauseBuilder.newInstance(getSelf(), sql));
	}
	public OffsetClauseBuilder<I> offset(Number i) {
		return addClause(OffsetClauseBuilder.newInstanceWithOffset(getSelf(), i));
	}
	public ForUpdateClauseBuilder<I> forUpdate() {
		return addClause(ForUpdateClauseBuilder.newInstance(this));
	}
	public ForShareClauseBuilder<I> forShare() {
		return addClause(ForShareClauseBuilder.newInstance(this));
	}

	//@Override
	public OrderByClauseBuilder<I> asc() {
		this.orderByPartial = this.orderByPartial.asc();
		return getSelf();
	}

	@Override
	public OrderByClauseBuilder<I> desc() {
		this.orderByPartial = this.orderByPartial.desc();
		return getSelf();
	}

	public OrderByClauseBuilder<I> property(String sql) {
		this.orderByPartial = this.orderByPartial.orderBy("{{" + sql + "}}");
		return getSelf();
	}
	
	public OrderByClauseBuilder<I> field(String sql) {
		this.orderByPartial = this.orderByPartial.orderBy(sql);
		return getSelf();
	}
	
	@Override
	public OrderByMode getOrderMode() {
		return orderByPartial.getOrderMode();
	}
	
	public ImmutableOrderByPartial getOrderByPartial() {
		return orderByPartial;
	}
	
	@Override
	public OrderByClauseBuilder<I> orderBy(String sql) {
		return property(sql);
	}
	
}
