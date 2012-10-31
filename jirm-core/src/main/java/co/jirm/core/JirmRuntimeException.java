package co.jirm.core;

import org.slf4j.helpers.MessageFormatter;


public abstract class JirmRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected JirmRuntimeException(String message, Object ... args) {
		super(MessageFormatter.arrayFormat(message, args).getMessage());
	}
	
}
