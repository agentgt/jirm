package co.jirm.orm.builder.query;

public interface SelectVisitorAcceptor {
	public <C extends SelectClauseVisitor> C accept(C visitor);
}