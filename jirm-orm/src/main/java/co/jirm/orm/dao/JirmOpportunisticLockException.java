package co.jirm.orm.dao;

import co.jirm.core.JirmRuntimeException;


public class JirmOpportunisticLockException extends JirmRuntimeException {

	private static final long serialVersionUID = 1L;

	public JirmOpportunisticLockException(String message, Object ... args) {
		super(message, args);
	}

}
