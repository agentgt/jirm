package co.jirm.mapper.definition;


public class SqlParameterObjectDefinition {
	
	private final SqlObjectDefinition<?> objectDefintion;
	private final int maximumLoadDepth;
	
	public SqlParameterObjectDefinition(SqlObjectDefinition<?> objectDefintion, int maximumLoadDepth) {
		super();
		this.objectDefintion = objectDefintion;
		this.maximumLoadDepth = maximumLoadDepth;
	}
	
	
	public SqlObjectDefinition<?> getObjectDefintion() {
		return objectDefintion;
	}
	
	public int getMaximumLoadDepth() {
		return maximumLoadDepth;
	}
	
}
