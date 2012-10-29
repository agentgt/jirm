package co.jirm.orm.builder.update;



public abstract class UpdateClauseVisitor {
	
	public abstract void visit(UpdateCustomClauseBuilder<?> custom);
	public abstract void visit(UpdateWhereClauseBuilder<?> where);
	public abstract void visit(SetClauseBuilder<?> set);
	
	public UpdateClauseVisitor startOn(UpdateVisitorAcceptor klause) {
		klause.accept(this);
		return this;
	}
	
}
