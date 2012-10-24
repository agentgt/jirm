package co.jirm.orm.builder;

import co.jirm.core.sql.ParametersBuilder;

public interface Condition<T> extends ParametersBuilder<T> {
	
	
	//public T and(Condition<T> other);
	
	public T and(String sql);
	
    //public T or(Condition<T> other);
    
    public T or(String sql);
    
    public T not();
    
    public boolean isNoOp();

    public enum CombineType {
    	AND, OR;
    }
    
}
