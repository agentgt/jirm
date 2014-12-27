/**
 * Copyright (C) 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.jirm.mapper.definition;

import static org.junit.Assert.*;

import java.beans.ConstructorProperties;

import javax.persistence.Column;
import javax.persistence.Id;

import co.jirm.core.JirmIllegalArgumentException;
import co.jirm.mapper.SqlObjectConfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.junit.Before;
import org.junit.Test;


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
	
	@Test
	public void testConstructorProperties() {
	  SqlObjectDefinition<ConstructorPropertiesTest> d = SqlObjectDefinition.fromClass(ConstructorPropertiesTest.class, SqlObjectConfig.DEFAULT);
	  
	  assertTrue(d.getIdParameters().containsKey("id"));
	  assertEquals("name", d.getParameters().get("name").sqlName());
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
	
	public static class ConstructorPropertiesTest {
	  @Id
	  private final Long id;
    private final String name;
	  
	  @ConstructorProperties({ "id", "name" })
	  public ConstructorPropertiesTest(Long id, String name) {
      this.id = id;
      this.name = name;
	  }
	}

}
