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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.sql.SqlPlaceholderParser.ParsedSql;
import co.jirm.core.sql.SqlPlaceholderParser.PlaceHolderType;
import co.jirm.core.util.ResourceUtils;


public class SqlPlaceholderParserTest {

	@Before
	public void setUp() throws Exception {}
	
	@Test
	public void test() throws Exception {
		String sql = ResourceUtils.getClasspathResourceAsString(getClass(), "search-recruiting.sql");
		ParsedSql psql = SqlPlaceholderParser.parseSql(sql);
		assertEquals("SELECT \n" + 
				"c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
				"FROM campaign c\n" + 
				"LEFT OUTER JOIN \n" + 
				"	(SELECT DISTINCT cg.campaign, geo.latitude, geo.longitude from campaign_geo cg\n" + 
				"	INNER JOIN geo geo on geo.id = cg.geo \n" + 
				"	WHERE geo.latitude IS NOT NULL AND geo.longitude IS NOT NULL AND cg.createts < ? \n" + 
				"	) g on g.campaign = c.id\n" + 
				"WHERE c.type = 'JOBPAGE' AND c.createts < ? \n" + 
				"ORDER BY c.createts ASC, c.id, g.latitude, g.longitude\n" + 
				"LIMIT ? \n" + 
				"OFFSET ? \n" + 
				"", psql.getResultSql());
		assertEquals(4, psql.getPlaceHolders().size());
		assertEquals(PlaceHolderType.POSITION, psql.getPlaceHolders().get(0).getType());
	}
	
	@Test
	public void testName() throws Exception {
		String sql = ResourceUtils.getClasspathResourceAsString(getClass(), "search-recruiting-name.sql");
		ParsedSql psql = SqlPlaceholderParser.parseSql(sql);
		assertEquals("SELECT \n" + 
				"c.id, c.name, c.tags, c.category, c.description, \n" + 
				"c.division, c.experience_level as \"experienceLevel\", \n" + 
				"c.locations, c.type, c.parent_id as \"parentId\", \n" + 
				"g.latitude as \"latitude\", g.longitude as \"longitude\"\n" + 
				"FROM campaign c\n" + 
				"LEFT OUTER JOIN \n" + 
				"	(SELECT DISTINCT cg.campaign, geo.latitude, geo.longitude from campaign_geo cg\n" + 
				"	INNER JOIN geo geo on geo.id = cg.geo \n" + 
				"	WHERE geo.latitude IS NOT NULL AND geo.longitude IS NOT NULL AND cg.createts < ? \n" + 
				"	) g on g.campaign = c.id\n" + 
				"WHERE c.type = 'JOBPAGE' AND c.createts < ? \n" + 
				"ORDER BY c.createts ASC, c.id, g.latitude, g.longitude\n" + 
				"LIMIT ? \n" + 
				"OFFSET ? \n" + 
				"", psql.getResultSql());
		assertEquals(4, psql.getPlaceHolders().size());
		assertEquals(PlaceHolderType.NAME, psql.getPlaceHolders().get(0).getType());
		assertEquals("now", psql.getPlaceHolders().get(0).asName().getName());
		
	}
	

	
	@Test
	public void testMultiApos() throws Exception {
		int i[] = SqlPlaceholderParser.parseForReplacement("blah = ''''");
		assertArrayEquals(new int[] {7,11}, i);
		String replace = "blah = ' Hey butt hole '' '  ";
		i = SqlPlaceholderParser.parseForReplacement(replace);
		assertArrayEquals(new int[] {7,27}, i);
		assertEquals("' Hey butt hole '' '", replace.substring(i[0], i[1]));
	}

}
