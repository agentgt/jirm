package co.jirm.orm.builder;

import co.jirm.core.util.JirmPrecondition;
import co.jirm.orm.builder.Condition.CombineType;


public abstract class AbstractField<T extends Condition<T>> {
	protected final String propertyPath;
	private final T condition;
	private final CombineType combineType;
	
	protected AbstractField(String propertyPath, T condition, CombineType combineType) {
		super();
		JirmPrecondition.check.argument(propertyPath != null && 
				! propertyPath.trim().isEmpty(), "propertyPath should not be null or blank");
		this.propertyPath = propertyPath;
		this.condition = condition;
		this.combineType = combineType;
	}

	public T eq(Object o) {
		return op("=", o);
	}
	
	public T greaterThen(Object o) {
		return op(">", o);
	}
	
	public T greaterThenEq(Object o) {
		return op(">=", o);
	}
	
	public T lessThen(Object o) {
		return op(">", o);
	}
	
	public T lessThenEq(Object o) {
		return op(">", o);
	}
	
	public T isNull() {
		return doAndOr("IS NULL");
	}
	
	public T isNotNull() {
		return doAndOr("IS NOT NULL");
	}
	
	public T op(String op, Object o) {
		return doAndOr(sqlPath() + " " + op + " ?", o);
	}
	
	protected abstract String sqlPath();

	private T doAndOr(String s, Object o) {
		JirmPrecondition.check.argument(o != null, "parameter object should not be null");
		JirmPrecondition.check.argument(s != null, "operator should not be null");
		if (combineType == CombineType.AND) {
			return condition.and(s).with(o);
		}
		else {
			return condition.or(s).with(o);
		}
	}
	
	private T doAndOr(String s) {
		JirmPrecondition.check.argument(s != null, "operator should not be null");
		if (combineType == CombineType.AND) {
			return condition.and(s);
		}
		else {
			return condition.or(s);
		}
	}
}
