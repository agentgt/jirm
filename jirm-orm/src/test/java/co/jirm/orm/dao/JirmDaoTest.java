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
package co.jirm.orm.dao;

import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlMultiValueRowMapper;
import co.jirm.orm.OrmConfig;
import co.jirm.orm.dao.EnumBean.MyEnum;
public class JirmDaoTest {

	private JirmDao<TestBean> dao;
	private SqlExecutor mock;
	private SqlMultiValueRowMapper<TestBean> rowMapper;
	
	@Before
	public void setUp() throws Exception {
		mock = mock(SqlExecutor.class);
		dao = JirmDao.newInstance(TestBean.class, OrmConfig.newInstance(mock));
		rowMapper = dao.getSelectBuilderFactory().getObjectRowMapper();
	}

	@Test
	public void testQueryForObjectByFilterStringObjectStringObject() {
		
		dao.select().where()
			.property("longProp").eq(1L)
			.property("stringProp").eq("blah")
			.query().forObject();
		
		verify(mock).queryForObject("SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets FROM test_bean WHERE test_bean.long_prop = ? AND test_bean.string_prop = ?"
				, rowMapper, new Object[] {1L, "blah"});
	}

	@Test
	public void testInsert() {
		String travis = System.getenv("TRAVIS");
		assumeTrue(travis == null || ! travis.equals("true"));
		if (travis == null) {
			Calendar c = Calendar.getInstance();
			TestBean t = new TestBean("blah", 1L, c);
			dao.insert(t);
			verify(mock).update("INSERT INTO test_bean (string_prop, long_prop, timets) VALUES (?, ?, ?)", new Object[] {"blah", 1L, c});
		}
	}
	
	@Test
	public void testReload() {
		Calendar c = Calendar.getInstance();
		TestBean t = new TestBean("blah", 1L, c);
		t = dao.reload(t);
		verify(mock).queryForObject("SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets FROM test_bean WHERE test_bean.string_prop = ?", 
				rowMapper,
				new Object[]{"blah"} 
				);
	}
	
	@Test
	public void testQueryForList() {
		
		dao.select().where()
			.property("longProp", 1L)
			.property("stringProp").eq("blah")
			.limit(100)
			.offset(10)
			.query()
			.forList();
		verify(mock).queryForList(
				"SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets FROM test_bean " +
				"WHERE test_bean.long_prop = ? AND test_bean.string_prop = ? LIMIT ? OFFSET ?", 
				rowMapper, 
				new Object[] {1L, "blah", 100L, 10L} );
	}
	
	@Test
	public void testParentForList() {
		JirmDao<ParentBean> parentDao = JirmDao.newInstance(ParentBean.class, OrmConfig.newInstance(mock));
		parentDao.select().where()
			.property("id", 1L)
			.limit(100)
			.offset(10)
			.query()
			.forList();
		verify(mock).queryForList("SELECT parent_bean.id, _test.string_prop AS \"test.stringProp\", _test.long_prop AS \"test.longProp\", _test.timets AS \"test.timeTS\" " +
				"FROM parent_bean INNER JOIN test_bean _test ON _test.string_prop = parent_bean.test WHERE parent_bean.id = ? LIMIT ? OFFSET ?"
				,parentDao.getSelectBuilderFactory().getObjectRowMapper(),new Object[] {1L, 100L, 10L});
		

		

	}
	
	@Test
	public void testPropertyPath() throws Exception {
		JirmDao<ParentBean> parentDao = JirmDao.newInstance(ParentBean.class, OrmConfig.newInstance(mock));
		
		parentDao.select().where()
			.property("test.stringProp").eq("Hello")
			.orProperty("test.longProp").eq(1L)
			.query()
			.forList();

		verify(mock).queryForList(
				"SELECT parent_bean.id, " +
				"_test.string_prop AS \"test.stringProp\", " +
				"_test.long_prop AS \"test.longProp\", " +
				"_test.timets AS \"test.timeTS\" " +
				"FROM parent_bean " +
				"INNER JOIN test_bean _test ON _test.string_prop = parent_bean.test " +
				"WHERE _test.string_prop = ? OR _test.long_prop = ?",
				parentDao.getSelectBuilderFactory().getObjectRowMapper(), 
				new Object[] {"Hello", 1L});
	}
	
	@Test
	public void testPropertyManual() throws Exception {
		JirmDao<ParentBean> parentDao = JirmDao.newInstance(ParentBean.class, OrmConfig.newInstance(mock));
		
		parentDao.select()
		.where("{{test.stringProp}} = ? OR {{test.longProp}} = ?")
				.with("Hello").with(1L)
		.query()
		.forList();
	
		verify(mock).queryForList("SELECT parent_bean.id, " +
				"_test.string_prop AS \"test.stringProp\", " +
				"_test.long_prop AS \"test.longProp\", " +
				"_test.timets AS \"test.timeTS\" " +
				"FROM parent_bean " +
				"INNER JOIN test_bean _test ON _test.string_prop = parent_bean.test " +
				"WHERE _test.string_prop = ? OR _test.long_prop = ?"
				, parentDao.getSelectBuilderFactory().getObjectRowMapper(),
				new Object[] {"Hello", 1L});	
	}
	
	@Test
	public void testGrandParentForList() {
		JirmDao<GrandParentBean> parentDao = JirmDao.newInstance(GrandParentBean.class, OrmConfig.newInstance(mock));
				//new SqlObjectDao<GrandParentBean>(mock, GrandParentBean.class);
		parentDao.select().where()
			.property("id", 1L)
			.limit(100)
			.offset(10)
			.query()
			.forList();
		
		verify(mock).queryForList("SELECT grand_parent_bean.id, _parent.id AS \"parent.id\", " +
				"_parent_test.string_prop AS \"parent.test.stringProp\", " +
				"_parent_test.long_prop AS \"parent.test.longProp\", " +
				"_parent_test.timets AS \"parent.test.timeTS\" " +
				"FROM grand_parent_bean " +
				"INNER JOIN parent_bean _parent ON _parent.id = grand_parent_bean.parent " +
				"INNER JOIN test_bean _parent_test ON _parent_test.string_prop = _parent.test " +
				"WHERE grand_parent_bean.id = ? LIMIT ? OFFSET ?"
				, parentDao.getSelectBuilderFactory().getObjectRowMapper(),
				 new Object[] {1L, 100L, 10L});
	}
	
	@Test
	public void testLazyGrandParentForList() {
		JirmDao<LazyGrandParentBean> parentDao = JirmDao.newInstance(LazyGrandParentBean.class, OrmConfig.newInstance(mock));
		parentDao.select().where()
			.property("id", 1L)
			.limit(100)
			.offset(10)
			.query()
			.forList();
		
		verify(mock).queryForList("SELECT grand_parent_bean.id, _parent.id AS \"parent.id\" " +
				"FROM grand_parent_bean " +
				"INNER JOIN parent_bean _parent ON _parent.id = grand_parent_bean.parent WHERE grand_parent_bean.id = ? LIMIT ? OFFSET ?",
				parentDao.getSelectBuilderFactory().getObjectRowMapper()
				, new Object[] {1L, 100L, 10L});
	}
	
	
	@Test
	public void testUpdate() {
		dao.update()
			.set("stringProp", "crap")
			.set("longProp", 10L)
			.where()
			.property("stringProp").eq("stuff")
			.execute();
		verify(mock).update("UPDATE test_bean SET string_prop = ?, long_prop = ? WHERE string_prop = ?"
				, new Object[] {"crap", 10L, "stuff"});
	}
	
	@Test
	public void testUpdateExclude() {
		JirmDao<LockBean> dao = JirmDao.newInstance(LockBean.class, OrmConfig.newInstance(mock));
		LockBean lb = new LockBean("MYID", 20, Calendar.getInstance(), 0);
		
		when(mock.update("UPDATE lock_bean SET long_prop = ?, version = ? WHERE id = ? AND version = ?"
				, new Object[] {20L, 1, "MYID", 0}))
				.thenReturn(1);
		
		dao.update(lb)
			.exclude("timeTS")
			.execute();
	}
	
	@Test
	public void testUpdateInclude() {
		JirmDao<LockBean> dao = JirmDao.newInstance(LockBean.class, OrmConfig.newInstance(mock));
		LockBean lb = new LockBean("MYID", 20, Calendar.getInstance(), 0);
		
		when(mock.update("UPDATE lock_bean SET long_prop = ?, version = ? WHERE id = ? AND version = ?"
				, new Object[] {20L, 1, "MYID", 0}))
				.thenReturn(1);
		
		dao.update(lb)
			.include("longProp")
			.execute();
	}
	
	@Test
	public void testAdder() {
		
		dao.update()
			.plus("longProp", 20)
			.where().property("stringProp").eq("crap")
			.execute();
		verify(mock).update("UPDATE test_bean SET long_prop = ( long_prop + ? ) WHERE string_prop = ?"
				, new Object[] { 20, "crap"});
	}
	
	@Test
	public void testCount() {
		dao.count().query().forLong();
		verify(mock).queryForLong("SELECT count(*) FROM test_bean LIMIT ?", new Object[] {1L});
	}
	
	@Test
	public void testCrap() {
		dao.select().where()
			.property("longProp").eq(20)
			.and("{{stringProp}} = ?").with("crap")
			.query()
			.forObject();
	}
	
	@Test
	public void testCustomQueryTemplate() {
		dao.getSelectBuilderFactory()
			.sqlFromResource("select-test-bean.sql")
			.bind("name", "Adam")
			.bind("limit", 1)
			.query()
			.forObject();
		
		verify(mock).queryForObject(
				"SELECT * from test_bean\n" + 
				"WHERE stringProp like ? \n" + 
				"LIMIT ? ",  
				dao.getSelectBuilderFactory().getObjectRowMapper(),
				new Object[] {"Adam", 1});		
	}
	
	@Test
	public void testCustomUpdateTemplate() throws Exception {
		Calendar now = Calendar.getInstance();
		dao.getUpdateBuilderFactory()
			.sqlFromResource("insert-test-bean.sql")
			.bind("timeTS", now)
			.bind("stringProp", "BLAH")
			.bind("longProp", 200L)
			.execute();
		verify(mock).update("INSERT INTO test_bean \n" + 
				"(string_prop, long_prop, timets)\n" + 
				"VALUES (\n" + 
				"? \n" + 
				", ? \n" + 
				", ? \n" + 
				")", new Object[] {"BLAH", 200L, now});
	}
	
	@Test
	public void testOrderBy() {
		
		dao.select().where()
			.property("longProp").eq(1L)
			.orderBy("longProp").asc()
			.orderBy("stringProp").desc()
			.query().forObject();
		
		verify(mock).queryForObject("SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets " +
				"FROM test_bean WHERE test_bean.long_prop = ? " +
				"ORDER BY test_bean.long_prop ASC, test_bean.string_prop DESC"
				, rowMapper, new Object[] {1L});
	}
	
	@Test
	public void testLessThanIssue7() {
		//Test for issue #7 lessThan
		dao.select().where()
			.property("longProp").lessThen(7)
			.query().forObject();
		
		verify(mock).queryForObject("SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets " +
				"FROM test_bean WHERE test_bean.long_prop < ?"
				, rowMapper, new Object[] {7});
	}
	
	@Test
	public void testLessThanEqIssue7() {
		//Test for issue #7 lessThan
		dao.select().where()
			.property("longProp").lessThenEq(7)
			.query().forObject();
		
		verify(mock).queryForObject("SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets " +
				"FROM test_bean WHERE test_bean.long_prop <= ?"
				, rowMapper, new Object[] {7});
	}
	
	@Test
	public void testCustomSqlIssue12() throws Exception {
		dao.getUpdateBuilderFactory().sql("UPDATE stuff set a = '1' where b = '2'").execute();
		verify(mock).update("UPDATE stuff set a = '1' where b = '2'", new Object[] {}); 
	}
	
	@Test
	public void testEnumIssue4() throws Exception {
		
		JirmDao<EnumBean> edao = JirmDao.newInstance(EnumBean.class, OrmConfig.newInstance(mock));
		EnumBean b = new EnumBean("blah", MyEnum.BAR);
		edao.insert(b);
		
		verify(mock).update("INSERT INTO enum_bean (id, my_enum) VALUES (?, ?)", 
				new Object[] {"blah", 1});
		
	}
	
	
	@Test
	public void testNotEqIssue5() {
		dao.select().where()
			.property("longProp").notEq(7)
			.query().forObject();
		
		verify(mock).queryForObject("SELECT test_bean.string_prop, test_bean.long_prop, test_bean.timets " +
				"FROM test_bean WHERE test_bean.long_prop != ?"
				, rowMapper, new Object[] {7});
	}

	
	@Test
	public void testForUpdateIssue10() {
		dao.select().where()
			.property("longProp").notEq(7)
			.forUpdate()
			.query().forObject();
		
		verify(mock).queryForObject("SELECT " +
				"test_bean.string_prop, " +
				"test_bean.long_prop, test_bean.timets " +
				"FROM test_bean WHERE test_bean.long_prop != ? FOR UPDATE"
				, rowMapper, new Object[] {7});
	}
	
	@Test
	public void testForShareIssue14() {
		dao.select().where()
			.property("longProp").notEq(7)
			.forShare()
			.query().forObject();
		
		verify(mock).queryForObject("SELECT " +
				"test_bean.string_prop, " +
				"test_bean.long_prop, test_bean.timets " +
				"FROM test_bean WHERE test_bean.long_prop != ? FOR SHARE"
				, rowMapper, new Object[] {7});
	}
}
