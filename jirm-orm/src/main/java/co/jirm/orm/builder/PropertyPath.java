package co.jirm.orm.builder;

import co.jirm.orm.builder.Condition.CombineType;


public class PropertyPath<T extends Condition<T>> extends AbstractField<T> {
	
	private PropertyPath(String propertyPath, T condition, CombineType combineType) {
		super(propertyPath, condition, combineType);
	}
	
	public static <T extends Condition<T>> PropertyPath<T> newPropertyPath(
			String propertyPath, 
			T condition, 
			CombineType combineType) {
		return new PropertyPath<T>(propertyPath, condition, combineType);
	}

	@Override
	protected String sqlPath() {
		return "{{" + propertyPath + "}}";
	}
}
