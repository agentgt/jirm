package co.jirm.core.sql;


public class PlainSql extends MutableParameterizedSql<PlainSql>{

	public PlainSql(String sql) {
		super(sql);
	}

	@Override
	protected PlainSql getSelf() {
		return this;
	}

	@Override
	public String toString() {
		return "PlainSql [getSql()=" + getSql() + ", getParameters()=" + getParameters() + ", getNameParameters()="
				+ getNameParameters() + "]";
	}
	
}
