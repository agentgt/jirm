package co.jirm.core.util;

import co.jirm.core.JirmIllegalArgumentException;
import co.jirm.core.JirmIllegalStateException;


public final class JirmPrecondition extends Precondition<JirmIllegalArgumentException, JirmIllegalStateException> {

	
	public static final JirmPrecondition check = new JirmPrecondition();
	

	private JirmPrecondition() {
		super();
	}


	@Override
	protected JirmIllegalArgumentException argumentException(String message) {
		throw new JirmIllegalArgumentException(message);
	}

	@Override
	protected JirmIllegalStateException stateException(String message) {
		throw new JirmIllegalStateException(message);
	}
	
}
