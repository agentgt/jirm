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

import co.jirm.core.sql.SqlSupplier;

import com.google.common.base.Optional;


public class ImmutableOrderByPartial implements SqlSupplier, OrderByPartial<ImmutableOrderByPartial> {
	
	private final String sql;
	private final OrderByMode mode;
	private final Optional<ImmutableOrderByPartial> parent;

	private ImmutableOrderByPartial(String sql, OrderByMode mode, Optional<ImmutableOrderByPartial> parent) {
		super();
		this.sql = sql;
		this.mode = mode;
		this.parent = parent;
	}
	
	public static ImmutableOrderByPartial newInstance(String sql) {
		return new ImmutableOrderByPartial(sql, OrderByMode.UNSPECIFIED, Optional.<ImmutableOrderByPartial>absent());
	}
	
	public ImmutableOrderByPartial asc() {
		return new ImmutableOrderByPartial(this.sql, OrderByMode.ASC, this.parent);
	}
	
	public ImmutableOrderByPartial desc() {
		return new ImmutableOrderByPartial(this.sql, OrderByMode.DESC, this.parent);
	}
	
	public ImmutableOrderByPartial orderBy(String sql) {
		return new ImmutableOrderByPartial(sql, OrderByMode.UNSPECIFIED, Optional.of(this));
	}
	
	public String getSql() {
		return sql;
	}
	public OrderByMode getOrderMode() {
		return mode;
	}
	public Optional<ImmutableOrderByPartial> getParent() {
		return parent;
	}
	
}
