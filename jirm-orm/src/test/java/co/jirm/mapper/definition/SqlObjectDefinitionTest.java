package co.jirm.mapper.definition;

import static org.junit.Assert.*;

import javax.persistence.Column;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Before;
import org.junit.Test;

import co.jirm.core.JirmIllegalArgumentException;
import co.jirm.mapper.SqlObjectConfig;


public class SqlObjectDefinitionTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testFromClass() {
		SqlObjectDefinition<ProtectedConsTest> d = 
				SqlObjectDefinition.fromClass(ProtectedConsTest.class, SqlObjectConfig.DEFAULT);
		assertTrue(d.getIdParameters().containsKey("id"));
		assertEquals("no_colum_name", d.getParameters().get("noColumName").sqlName());
	}
	
	@Test(expected=JirmIllegalArgumentException.class)
	public void testNoDefinitionFailure() {
		SqlObjectDefinition.fromClass(NoDef.class, SqlObjectConfig.DEFAULT);
	}
	
	public static class ProtectedConsTest {
		@Id
		@Column(length = 8192)
		private final String id;
		
		@Column(length = 8192)
		private final String noColumName;
		

		@JsonCreator
		protected ProtectedConsTest(@JsonProperty("id") String id, @JsonProperty("noColumName") String noColumName) {
			super();
			this.id = id;
			this.noColumName = noColumName;
		}
		
		public String getId() {
			return id;
		}
		
		public String getNoColumName() {
			return noColumName;
		}
		
	}
	
	public static class NoDef {}

}
