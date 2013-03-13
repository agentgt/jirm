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
package co.jirm.core.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class ObjectMapUtilsTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testEnumFromOrdinal() {
		Enum<?> e = ObjectMapUtils.enumFrom(BlahEnum.class, 0);
		System.out.println(e);
		assertEquals(BlahEnum.ONE, e);
	}
	
	@Test
	public void testEnumFromName() {
		Enum<?> e = ObjectMapUtils.enumFrom(BlahEnum.class, "TWO");
		System.out.println(e);
		assertEquals(BlahEnum.TWO, e);
	}
	
	public static enum BlahEnum {
		ONE,
		TWO
	}

}
