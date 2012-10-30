package co.jirm.orm.builder.delete;

public interface DeleteVisitorAcceptor {
	public <C extends DeleteClauseVisitor> C accept(C visitor);
}