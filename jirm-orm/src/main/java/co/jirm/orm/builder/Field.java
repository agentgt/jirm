package co.jirm.orm.builder;

import co.jirm.orm.builder.Condition.CombineType;


public class Field<T extends Condition<T>> extends AbstractField<T> {
	
	private Field(String propertyPath, T condition, CombineType combineType) {
		super(propertyPath, condition, combineType);
	}
	
	public static <T extends Condition<T>> Field<T> newField(String propertyPath, T condition, CombineType combineType) {
		return new Field<T>(propertyPath, condition, combineType);
	}

	@Override
	protected String sqlPath() {
		return propertyPath;
	}


}
