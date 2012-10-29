package co.jirm.orm.builder;

import co.jirm.core.sql.Parameters;

import com.google.common.collect.ImmutableList;

public abstract class ConditionVisitor {
	public void visitNoOp() {
		return;
	}
	public void visitAnd(ImmutableList<ImmutableCondition> conditions, 
			Parameters parameters) {
		for (ImmutableCondition c : conditions) {
			c.accept(this);
		}
	}
	public void visitOr(ImmutableList<ImmutableCondition> conditions, Parameters parameters) {
		for (ImmutableCondition c : conditions) {
			c.accept(this);
		}
	}
	public abstract void visitSql(String sql, Parameters parameters);
	
	public void visitNot(ImmutableCondition condition) {
		condition.accept(this);
	}
	
	
}