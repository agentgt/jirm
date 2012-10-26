package co.jirm.orm.builder.update;

public interface UpdateVisitorAcceptor {
	public <C extends UpdateClauseVisitor> C accept(C visitor);
}