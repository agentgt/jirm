package co.jirm.orm.builder;


public interface SqlClause<I> extends Clause<I> {
	public String getSql();
}
