package co.jirm.core.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public interface Parameters {
	
	public ImmutableList<Object> getParameters();
	
	public ImmutableMap<String, Object> getNameParameters();
	
	public ImmutableList<Object> mergedParameters();

}
