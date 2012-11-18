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
package co.jirm.orm.builder.update;

import static com.google.common.base.Strings.nullToEmpty;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Optional;


public class SetClauseBuilder<I> extends AbstractSqlParameterizedUpdateClause<SetClauseBuilder<I>, I> {
	
	private SetClauseBuilder(UpdateClause<I> parent, String sql) {
		super(parent, UpdateClauseType.SET, sql);
	}
	
	static <I> SetClauseBuilder<I> newInstance(UpdateClause<I> parent) {
		return new SetClauseBuilder<I>(parent, "");
	}
	
	public SetClauseBuilder<I> set(String property, Object value) {
		return setProperty(property, value);
	}
	
	public SetClauseBuilder<I> plus(String property, Number value) {
		StringBuilder sb = new StringBuilder();
		if (! nullToEmpty(getSql()).isEmpty()) {
			sb.append(getSql()).append(", ");
		}
		sb.append("{{").append(property).append("}} = ( {{").append(property).append("}} + ").append("?").append(" )");
		with(value);
		setSql(sb.toString());
		return getSelf();
	}
	
	public SetClauseBuilder<I> setAll(Map<String,Object> m) {
		boolean first = true;
		StringBuilder sb = new StringBuilder();
		/*
		 * TODO Clean this up as its nasty.
		 */
		for(Entry<String,Object> e : m.entrySet()) {
			if (first) { 
				first = false;
				String sql = getSql();
				if (! nullToEmpty(sql).isEmpty() ) {
					sb.append(getSql()).append(", ");
				}
			}
			else {
				sb.append(", ");
			}
			_writeProperty(sb, e.getKey());
			Object o = e.getValue();
			if (o == null) {
				o = Optional.absent();
			}
			with(o);
		}
		setSql(sb.toString());
		return getSelf();
	}
	
	public SetClauseBuilder<I> setProperty(String property, Object value) {
		StringBuilder sb = new StringBuilder();
		if (! nullToEmpty(getSql()).isEmpty()) {
			sb.append(getSql()).append(", ");
		}
		_writeProperty(sb, property);
		with(value);
		setSql(sb.toString());
		return getSelf();
	}
	
	private static void _writeProperty(StringBuilder sb, String property) {
		sb.append("{{").append(property).append("}} = ").append("?");
	}
	
	public SetClauseBuilder<I> setField(String field, Object value) {
		StringBuilder sb = new StringBuilder();
		if (! nullToEmpty(getSql()).isEmpty()) {
			sb.append(getSql()).append(", ");
		}
		sb.append(field).append(" = ").append("?");
		setSql(sb.toString());
		return getSelf();
	}
	
	public UpdateWhereClauseBuilder<I> where() {
		return addClause(UpdateWhereClauseBuilder.newInstance(getSelf()));
	}

	@Override
	public <C extends UpdateClauseVisitor> C accept(C visitor) {
		visitor.visit(getSelf());
		for (UpdateClause<I> k : children) {
			k.accept(visitor);
		}
		return visitor;
	}

	@Override
	protected SetClauseBuilder<I> getSelf() {
		return this;
	}

}
