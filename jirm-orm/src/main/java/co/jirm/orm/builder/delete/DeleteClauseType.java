package co.jirm.orm.builder.delete;


public enum DeleteClauseType {
	
	ROOT(null),
	CUSTOM(""),
	WHERE("WHERE");
	
	private final String sql;
	
	private DeleteClauseType(String sql) {
		this.sql = sql;
	}
	
	public String getSql() {
		return sql;
	}
}
