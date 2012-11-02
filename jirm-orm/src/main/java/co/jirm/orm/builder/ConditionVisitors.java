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

import co.jirm.core.sql.Parameters;
import co.jirm.core.util.SafeAppendable;

import com.google.common.collect.ImmutableList;


public class ConditionVisitors {
	public static ConditionVisitor conditionVisitor(final Appendable appendable) {
		
		final SafeAppendable sb = new SafeAppendable(appendable);
		
		ConditionVisitor v = new ConditionVisitor() {
			private int depth = 0;
			
			@Override
			public void visitAnd(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
				depth++;
				doCondition(" AND ", conditions, parameters);
				
			}
			@Override
			public void visitOr(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
				depth++;
				doCondition(" OR ", conditions, parameters);
			}
			
			private void doCondition(String op, ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
				if (conditions.size() == 1) {
					sb.append(op);
					conditions.get(0).accept(this);
					
				}
				else {
					boolean top = depth == 1;
					if (! top)
						sb.append("( ");
					boolean first = true;
					for (ImmutableCondition c : conditions) {
						if (first) { 
							first = false; 
							c.accept(this);
						}
						else {
							sb.append(op);
							c.accept(this);
						}
						
					}
					if (! top)
						sb.append(" )");
				}
			}

			@Override
			public void visitSql(String sql, Parameters parameters) {
				sb.append(sql);
			}
		};
		return v;
	}
	
	public static abstract class ParametersConditionVisitor extends ConditionVisitor {

		@Override
		public void visitAnd(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
			super.visitAnd(conditions, parameters);
			doParameters(parameters);
		}

		@Override
		public void visitOr(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
			super.visitOr(conditions, parameters);
			doParameters(parameters);
		}

		@Override
		public void visitSql(String sql, Parameters parameters) {
			doParameters(parameters);
		}
		
		public abstract void doParameters(Parameters p);
		
	}
}
