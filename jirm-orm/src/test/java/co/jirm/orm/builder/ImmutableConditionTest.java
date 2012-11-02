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

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.orm.builder.ConditionVisitors.ParametersConditionVisitor;


public class ImmutableConditionTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testWhereString() {
		ImmutableCondition c = ImmutableCondition.where("kyle=?")
				.and("stuff=?").with("crap")
				.or("bingo=?").with("blah")
				.or("blah=?").with("kingo")
				.and("blah=?").with("kingo");
		final StringBuilder sb = new StringBuilder();
		
		final MutableParameters mp = new MutableParameters();
		
		c.accept(new ParametersConditionVisitor() {
			@Override
			public void doParameters(Parameters ps) {
				mp.addAll(ps);
			}
		});
		
		ConditionVisitor v = ConditionVisitors.conditionVisitor(sb);
		c.accept(v);
		System.out.println(sb.toString());
		System.out.println(mp.getParameters());
		
		
	}

}
