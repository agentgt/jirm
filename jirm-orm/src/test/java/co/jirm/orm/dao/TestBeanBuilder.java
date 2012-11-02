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
package co.jirm.orm.dao;

public class TestBeanBuilder {
	private java.lang.String stringProp;
	private long longProp;
	private java.util.Calendar timeTS;

	public TestBeanBuilder() {}

	public TestBeanBuilder(TestBean o) {
		this.stringProp = o.getStringProp();
		this.longProp = o.getLongProp();
		this.timeTS = o.getTimeTS();
	}
	public TestBeanBuilder stringProp(java.lang.String p) {
		this.stringProp = p;
		return this;
	}
	public TestBeanBuilder longProp(long p) {
		this.longProp = p;
		return this;
	}
	public TestBeanBuilder timeTS(java.util.Calendar p) {
		this.timeTS = p;
		return this;
	}
	public TestBean build() {
		return new TestBean(stringProp, longProp, timeTS);
	}
}