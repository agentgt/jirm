package co.jirm.orm.builder.query;



public interface SqlSelectClause<I> extends SelectClause<I> {
	public String getSql();
}
