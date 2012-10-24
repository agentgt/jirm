package co.jirm.orm.builder;


public enum ClauseType {
	
	ROOT(null),
	CUSTOM(""),
	WHERE("WHERE"),
	ORDERBY("ORDER BY"),
	LIMIT("LIMIT"),
	OFFSET("OFFSET");
	
	private final String sql;
	
	private ClauseType(String sql) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}
}
