package co.jirm.core.sql;

import com.google.common.collect.ImmutableList;


public class MutableParameters extends MutableParameterized<MutableParameters> {

	@Override
	protected MutableParameters getSelf() {
		return this;
	}

	@Override
	public ImmutableList<Object> mergedParameters() {
		return getParameters();
	}
	
}
