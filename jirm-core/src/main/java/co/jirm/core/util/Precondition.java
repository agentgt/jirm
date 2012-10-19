package co.jirm.core.util;

import javax.annotation.Nullable;

import org.slf4j.helpers.MessageFormatter;

import java.lang.IllegalArgumentException;
import java.lang.IllegalStateException;

public abstract class Precondition<ARG extends IllegalArgumentException, STATE extends IllegalStateException> {

	public static Precondition<IllegalArgumentException, IllegalStateException> check = 
			new Precondition<IllegalArgumentException, IllegalStateException>() {
		protected IllegalArgumentException argumentException(String message) {
			return new IllegalArgumentException(message);
		}
		
		protected IllegalStateException stateException(String message) {
			return new IllegalStateException(message);
		}
	};
	
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
