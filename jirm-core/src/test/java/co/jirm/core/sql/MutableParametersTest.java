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
package co.jirm.core.sql;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.JirmIllegalArgumentException;


public class MutableParametersTest {

	@Before
	public void setUp() throws Exception {}

	@Test(expected=JirmIllegalArgumentException.class)
	public void testBindNull() {
		MutableParameters mp = new MutableParameters();
		mp.bind("stuff", null);
	}

	@Test(expected=JirmIllegalArgumentException.class)
	public void testWithNull() {
		MutableParameters mp = new MutableParameters();
		mp.with((Object)null);
	}

}
