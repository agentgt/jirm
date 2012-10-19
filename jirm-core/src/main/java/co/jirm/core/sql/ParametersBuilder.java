package co.jirm.core.sql;



public interface ParametersBuilder<T> extends Parameters {
	
	T set(String key, Object value);
	T with(Object ... value);
	
}
