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
import static org.junit.Assert.assertTrue;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.util.ResourceUtils;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;


public class PlainSqlTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testBasic() throws Exception {
	
		PlainSql sql = PlainSql.fromResource(getClass(), "select-test-bean.sql")
				.bind("name", "Adam")
				.bind("limit", 1);
		System.out.println(sql.toString());
		assertEquals(ImmutableList.<Object>of("Adam", 1), sql.mergedParameters());
		assertEquals(
				"SELECT * from test_bean\n" + 
				"WHERE stringProp like ? \n" + 
				"LIMIT ? ", sql.getSql());
	}
	
	@Test
	public void testNameParameters() throws Exception {
		PlainSql sql = PlainSql.parse(ResourceUtils.getClasspathResourceAsString(getClass(), "search-recruiting-name.sql"));
		sql.with("1", "2", "3", "4");
		assertEquals(ImmutableList.<Object>of("1","2","3", "4"), sql.mergedParameters());
		sql = PlainSql.parse(ResourceUtils.getClasspathResourceAsString(getClass(), "search-recruiting-name.sql"));
		sql
			.bind("now", "1")
			.bind("limit", "10")
			.bind("offset", "100");
		assertEquals(ImmutableList.<Object>of("1","1","10", "100"), sql.mergedParameters());
	}
	
	@Test
	public void testPerformance() throws Exception {
		Stopwatch sw = new Stopwatch().start();
		String sql = ResourceUtils.getClasspathResourceAsString(getClass(), "search-recruiting-name.sql");
		for (int i = 0; i < 300000; i++) {
			PlainSql.parse(sql);
		}
		
		assertTrue("Should be faster",sw.stop().elapsedMillis() < 5000);
	}
	
	@Test
	public void testPerformanceFromClasspath() throws Exception {
		Stopwatch sw = new Stopwatch().start();
		for (int i = 0; i < 300000; i++) {
			PlainSql.fromResource(getClass(), "search-recruiting-name.sql");
		}
		
		assertTrue("Should be faster",sw.stop().elapsedMillis() < 5000);
	}
	
	@Test
	public void testApos() throws Exception {
		PlainSql sql = PlainSql.fromResource(getClass(), "select-test-bean.sql");
		String result = sql
				.bind("name", "Adam")
				.bind("limit", 1).getSql();
		assertEquals(
				"SELECT * from test_bean\n" + 
				"WHERE stringProp like ? \n" + 
				"LIMIT ? ", result);
	}
	
	@Test
	public void testPartial() throws Exception {
		PlainSql sql = PlainSql.fromResource(getClass(), "partial-test.sql#other")
				.with(Calendar.getInstance())
				.with(1)
				.with(2);
		
		String actual = sql.getSql();
		assertEquals("SELECT\n" + 
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
				"ORDER BY c.createts ASC, c.id, g.latitude, g.longitude\n" + 
				"LIMIT ? \n" + 
				"OFFSET ? ", actual);
	}
}
