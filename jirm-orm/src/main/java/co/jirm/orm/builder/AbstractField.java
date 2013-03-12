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

import static co.jirm.core.util.JirmPrecondition.check;
import co.jirm.orm.builder.Condition.CombineType;


public abstract class AbstractField<T extends Condition<T>> {
	protected final String propertyPath;
	private final T condition;
	private final CombineType combineType;
	
	protected AbstractField(String propertyPath, T condition, CombineType combineType) {
		super();
		check.argument(propertyPath != null && 
				! propertyPath.trim().isEmpty(), "propertyPath should not be null or blank");
		this.propertyPath = propertyPath;
		this.condition = condition;
		this.combineType = combineType;
	}

	public T eq(Object o) {
		return op("=", o);
	}
	
	public T notEq(Object o) {
		return op("!=", o);
	}
	
	public T greaterThen(Object o) {
		return op(">", o);
	}
	
	public T greaterThenEq(Object o) {
		return op(">=", o);
	}
	
	public T lessThen(Object o) {
		return op("<", o);
	}
	
	public T lessThenEq(Object o) {
		return op("<=", o);
	}
	
	public T isNull() {
		return doAndOr("IS NULL");
	}
	
	public T isNotNull() {
		return doAndOr("IS NOT NULL");
	}
	
	public T op(String op, Object o) {
		return doAndOr(sqlPath() + " " + op + " ?", o);
	}
	
	protected abstract String sqlPath();

	private T doAndOr(String s, Object o) {
		check.argument(o != null, "parameter object should not be null");
		check.argument(s != null, "operator should not be null");
		if (combineType == CombineType.AND) {
			return condition.and(s).with(o);
		}
		else {
			return condition.or(s).with(o);
		}
	}
	
	private T doAndOr(String s) {
		check.argument(s != null, "operator should not be null");
		if (combineType == CombineType.AND) {
			return condition.and(s);
		}
		else {
			return condition.or(s);
		}
	}
}
