package co.jirm.orm.builder.delete;



public abstract class DeleteClauseVisitor {
	
	public abstract void visit(DeleteCustomClauseBuilder<?> custom);
	public abstract void visit(DeleteWhereClauseBuilder<?> where);
	
	public DeleteClauseVisitor startOn(DeleteVisitorAcceptor klause) {
		klause.accept(this);
		return this;
	}
	
}
