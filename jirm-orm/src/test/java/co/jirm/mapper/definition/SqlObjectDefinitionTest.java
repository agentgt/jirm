package co.jirm.mapper.definition;

import static org.junit.Assert.*;

import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Before;
import org.junit.Test;

import co.jirm.mapper.SqlObjectConfig;


public class SqlObjectDefinitionTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testFromClass() {
		SqlObjectDefinition<ProtectedConsTest> d = 
				SqlObjectDefinition.fromClass(ProtectedConsTest.class, SqlObjectConfig.DEFAULT);
		assertTrue(d.getIdParameters().containsKey("id"));
		
	}
	
	public static class ProtectedConsTest {
		@Id
		private String id;

		@JsonCreator
		protected ProtectedConsTest(@JsonProperty("id") String id) {
			super();
			this.id = id;
		}
		
		public String getId() {
			return id;
		}
		
	}

}
