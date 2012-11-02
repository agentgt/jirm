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
package co.jirm.orm.builder;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;



public abstract class AbstractWhereClauseBuilder<B extends Condition<B>, I> implements MutableCondition<B>  {
	protected ImmutableCondition condition;
	
	protected AbstractWhereClauseBuilder(ImmutableCondition condition) {
		this.condition = condition;
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
	public B and(String sql) {
		this.condition = this.condition.and(sql);
		return getSelf();
	}
	
	public B where(String sql) {
		this.condition = this.condition.and(sql);
		return getSelf();
	}

	@Override
	public B or(String sql) {
		this.condition = this.condition.or(sql);
		return getSelf();
	}
	
	public PropertyPath<B> property(String property) {
		return andProperty(property);
	}
	
	public B property(String property, Object o) {
		return andProperty(property).eq(o);
	}
	
	public B propertyAll(Map<String, Object> m) {
		for(Entry<String,Object> e : m.entrySet()) {
			andProperty(e.getKey()).eq(e.getValue());
		}
		return getSelf();
	}
	
	public PropertyPath<B> andProperty(String property) {
		return PropertyPath.newPropertyPath(property, getSelf(), CombineType.AND);
	}

	public PropertyPath<B> orProperty(String property) {
		return PropertyPath.newPropertyPath(property, getSelf(), CombineType.OR);

	}
	
	public Field<B> andField(String field) {
		return Field.newField(field, getSelf(), CombineType.AND);
	}

	public Field<B> orField(String field) {
		return Field.newField(field, getSelf(), CombineType.OR);

	}
	
	@Override
	public B not() {
		this.condition = this.condition.not();
		return getSelf();
	}

	@Override
	public boolean isNoOp() {
		return this.condition.isNoOp();
	}
	
	
	@Override
	public B bind(String key, Object value) {
		this.condition = this.condition.bind(key, value);
		return getSelf();
	}
	
	@Override
	public B with(Object ... value) {
		this.condition = this.condition.with(value);
		return getSelf();
	}
	
	protected abstract B getSelf();
}
