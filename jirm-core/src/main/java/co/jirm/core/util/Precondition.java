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

import javax.annotation.Nullable;

import org.slf4j.helpers.MessageFormatter;

import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;

public abstract class Precondition<ARG extends IllegalArgumentException, STATE extends IllegalStateException> {

	public final <T> T notNull(T o, @Nullable Object errorMessage) {
		argument(o != null, errorMessage);
		return o;
	}
	
	public final <T> T notNull(T o, @Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		argument(o != null, errorMessageTemplate, errorMessageArgs);
		return o;
	}

	public final void argument(boolean expression, @Nullable Object errorMessage) {
		if (!expression) {
			throw argumentException(String.valueOf(errorMessage));
		}
	}

	public final void argument(boolean expression, @Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		if (!expression) {
			throw argumentException(format(errorMessageTemplate, errorMessageArgs));
		}
	}

	public final void state(boolean expression, @Nullable Object errorMessage) {
		if (!expression) {
			throw stateException(String.valueOf(errorMessage));
		}
	}

	public final void state(boolean expression, @Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		if (!expression) {
			throw stateException(format(errorMessageTemplate, errorMessageArgs));
		}
	}
	
	public final STATE stateInvalid(@Nullable Object errorMessage) {
		throw stateException(String.valueOf(errorMessage));
	}

	public final STATE stateInvalid(@Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		throw stateException(format(errorMessageTemplate, errorMessageArgs));
	}
	
	public final ARG argumentInvalid(@Nullable Object errorMessage) {
		throw argumentException(String.valueOf(errorMessage));
	}

	public final ARG argumentInvalid(@Nullable String errorMessageTemplate,
			@Nullable Object... errorMessageArgs) {
		throw argumentException(format(errorMessageTemplate, errorMessageArgs));
	}

	public String format(String message, Object... args) {
		return MessageFormatter.arrayFormat(message, args).getMessage();
	}

	protected abstract ARG argumentException(String message);
	
	protected abstract STATE stateException(String message);
	
}
