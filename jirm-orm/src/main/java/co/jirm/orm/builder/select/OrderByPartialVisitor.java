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

import co.jirm.orm.builder.select.OrderByPartial.OrderByMode;


public abstract class OrderByPartialVisitor {
	
	public void visit(ImmutableOrderByPartial partial) {
		final boolean first;
		if (partial.getParent().isPresent()) {
			visit(partial.getParent().get());
			first = false;
		}
		else {
			first = true;
		}
		visit(partial, first);
	}
	
	protected void visit(OrderByPartial<?> partial, boolean first) {
		visit(partial.getSql(), partial.getOrderMode(), first);
	}
	
	protected abstract void visit(String field, OrderByMode mode, boolean first);

}
