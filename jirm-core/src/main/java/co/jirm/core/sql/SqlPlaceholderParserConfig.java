package co.jirm.core.sql;


public class SqlPlaceholderParserConfig {
	private final boolean stripNewLines;
	
	public final static SqlPlaceholderParserConfig DEFAULT = new SqlPlaceholderParserConfig(false);
	
	public SqlPlaceholderParserConfig(boolean stripNewLines) {
		super();
		this.stripNewLines = stripNewLines;
	}

	public boolean isStripNewLines() {
		return stripNewLines;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (stripNewLines ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SqlPlaceholderParserConfig other = (SqlPlaceholderParserConfig) obj;
		if (stripNewLines != other.stripNewLines)
			return false;
		return true;
	}

}
