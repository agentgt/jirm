package co.jirm.orm.builder.update;


public enum UpdateClauseType {
	
	ROOT(null),
	CUSTOM(""),
	SET("SET"),
	WHERE("WHERE");
	
	private final String sql;
	
	private UpdateClauseType(String sql) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}
}
