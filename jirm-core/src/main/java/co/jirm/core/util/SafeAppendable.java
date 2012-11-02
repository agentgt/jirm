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

import java.io.IOException;


public class SafeAppendable {
	
	private final Appendable a;
	private boolean empty = true;

	public SafeAppendable(Appendable a) {
		super();
		this.a = a;
	}
	
	public SafeAppendable append(CharSequence s) {
		try {
			if (empty && s.length() > 0) empty = false;
			a.append(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}
	
	public boolean isEmpty() {
		return empty;
	}
	
	
	public Appendable getAppendable() {
		return a;
	}

}
