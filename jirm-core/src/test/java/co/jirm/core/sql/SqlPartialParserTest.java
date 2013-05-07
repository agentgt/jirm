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
package co.jirm.core.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.JirmIllegalArgumentException;
import co.jirm.core.JirmIllegalStateException;
import co.jirm.core.sql.SqlPartialParser.ExpandedSql;
import co.jirm.core.sql.SqlPartialParser.FileDeclarationSql;
import co.jirm.core.sql.SqlPartialParser.Parser;
import co.jirm.core.sql.SqlPartialParser.Path;
import co.jirm.core.util.ResourceUtils;


public class SqlPartialParserTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testProcessFile() throws Exception {
		String f = ResourceUtils.getClasspathResourceAsString(getClass(), "partial-test.sql");
		FileDeclarationSql sql = SqlPartialParser.processFile("partial-test.sql", f);
		assertEquals(3, sql.getHashDeclarations().size());
		assertEquals(3, sql.getReferences().size());
		assertEquals("c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"", 
				sql.getHashDeclarations().get("stuff").join());
		assertEquals("c.type = 'JOBPAGE' AND c.createts < now() -- {}", 
				sql.getReferences().get(0).join());
		assertNotNull(sql);
	}
	
	@Test(expected=JirmIllegalStateException.class)
	public void testProcessFileHashFailure() throws Exception {
		String f = ResourceUtils.getClasspathResourceAsString(getClass(), "partial-test-hash-duplicate.sql");
		SqlPartialParser.processFile("partial-test-hash-duplicate.sql", f);
	}
	
	@Test
	public void testPath() throws Exception {
		Path p = Path.create("/blah/stuff");
		Path h = Path.create("#crap");
		assertEquals("/blah/stuff#crap", p.fromRelative(h).getFullPath());
	}
	@Test
	public void testExpand() throws Exception {
		Parser p = SqlPartialParser.Parser.create();
		ExpandedSql e = p.expand("/co/jirm/core/sql/partial-test.sql#other");
		String actual = e.join();
		assertEquals("SELECT\n" + 
				"c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
				"FROM campaign c\n" + 
				"LEFT OUTER JOIN \n" + 
				"	(SELECT DISTINCT cg.campaign, geo.latitude, geo.longitude from campaign_geo cg\n" + 
				"	INNER JOIN geo geo on geo.id = cg.geo \n" + 
				"	WHERE geo.latitude IS NOT NULL AND geo.longitude IS NOT NULL AND cg.createts < now() -- {}\n" + 
				"	) g on g.campaign = c.id\n" + 
				"ORDER BY c.createts ASC, c.id, g.latitude, g.longitude\n" + 
				"LIMIT 100 -- {}\n" + 
				"OFFSET 1 -- {}", actual);
		
	}
	
	@Test(expected=JirmIllegalStateException.class)
	public void testValidateInvalid() throws Exception {
		Parser p = SqlPartialParser.Parser.create();
		p.expand("/co/jirm/core/sql/partial-test-validate.sql#other");
	}
	
	@Test
	public void testIssue26ValidateValid() throws Exception {
		SqlPartialParser.parseFromPath("/co/jirm/core/sql/issue26-partial-test-validate.sql#other");
	}
	
	@Test
	public void testIssue25ValidateErrorMessage() throws Exception {
		try {
			SqlPartialParser.parseFromPath("/co/jirm/core/sql/partial-test-validate.sql#other");
			fail("Should have thrown an exception");
		}
		catch (JirmIllegalStateException e) {
			assertEquals("Reference '> #stuff' in /co/jirm/core/sql/partial-test-validate.sql at line: 44 does" +
					"\nNOT MATCH declaration /co/jirm/core/sql/partial-test-validate.sql#stuff at line: 1\n" + 
					"REFERENCE:\n" + 
					"c.id, c.name, c.tags, c.category, c.description\n" + 
					"DECLARATION:\n" + 
					"c.id, c.name, c.tags, c.category, c.description, \n" + 
					"c.division, c.experience_level as \"experienceLevel\", \n" + 
					"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
					"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
					"", e.getMessage());
		}
		catch (Exception e) {
			fail("should have thrown the right exception");
		}
	}
	
	@Test
	public void testReference() throws Exception {
		Parser p = SqlPartialParser.Parser.create();
		ExpandedSql e = p.expand("/co/jirm/core/sql/partial-test-ref.sql#ref");
		String actual = e.join();
		assertEquals("SELECT\n" + 
				"c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
				"FROM campaign c\n" + 
				"LEFT OUTER JOIN \n" + 
				"	(SELECT DISTINCT cg.campaign, geo.latitude, geo.longitude from campaign_geo cg\n" + 
				"	INNER JOIN geo geo on geo.id = cg.geo \n" + 
				"	WHERE geo.latitude IS NOT NULL AND geo.longitude IS NOT NULL AND cg.createts < now() -- {}\n" + 
				"	) g on g.campaign = c.id\n" + 
				"ORDER BY c.createts ASC, c.id, g.latitude, g.longitude\n" + 
				"LIMIT 100 -- {}\n" + 
				"OFFSET 1 -- {}", actual);
		
	}
	
	@Test(expected=JirmIllegalArgumentException.class)
	public void testReferenceFail() throws Exception {
		Parser p = SqlPartialParser.Parser.create();
		p.expand("/co/jirm/core/sql/partial-test-ref.sql#other");
		
	}
	
	
	@Test
	public void testIssue24HashDeclarationRemoval() throws Exception {
		Parser p = SqlPartialParser.Parser.create();
		ExpandedSql e = p.expand("/co/jirm/core/sql/issue24-hash-issue.sql");
		String actual = e.join();
		assertEquals("SELECT\n" + 
				"c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
				"FROM campaign c\n" + 
				"WHERE \n" + 
				"c.type = 'JOBPAGE' AND c.createts < now() -- {}\n" + 
				"ORDER BY c.createts\n" + 
				"LIMIT 100 -- {}\n" + 
				"OFFSET 1 -- {}", actual);
	}
	
	@Test
	public void testIssue24HashWithRefRemoval() throws Exception {
		Parser p = SqlPartialParser.Parser.create();
		ExpandedSql e = p.expand("/co/jirm/core/sql/issue24-hash-issue-with-ref.sql");
		String actual = e.join();
		assertEquals("SELECT\n" + 
				"c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
				"FROM campaign c\n" + 
				"WHERE \n" + 
				"c.type = 'JOBPAGE' AND c.createts < now() -- {}\n" + 
				"ORDER BY c.createts\n" + 
				"LIMIT 100 -- {}\n" + 
				"OFFSET 1 -- {}", actual);
	}

}
