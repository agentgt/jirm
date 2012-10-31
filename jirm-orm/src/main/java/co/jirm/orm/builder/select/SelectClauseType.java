package co.jirm.orm.builder.select;


public enum SelectClauseType {
	
	ROOT(null),
	CUSTOM(""),
	WHERE("WHERE"),
	ORDERBY("ORDER BY"),
	LIMIT("LIMIT"),
	OFFSET("OFFSET");
	
	private final String sql;
	
	private SelectClauseType(String sql) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}
}
