package co.jirm.orm.builder.select;

public interface SelectVisitorAcceptor {
	public <C extends SelectClauseVisitor> C accept(C visitor);
}