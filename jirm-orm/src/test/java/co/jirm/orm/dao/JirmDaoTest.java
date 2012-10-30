package co.jirm.orm.dao;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.execute.SqlExecutor;
import co.jirm.core.execute.SqlExecutorRowMapper;
import co.jirm.orm.OrmConfig;
public class JirmDaoTest {

	private JirmDao<TestBean> dao;
	private SqlExecutor mock;
	private SqlExecutorRowMapper<TestBean> rowMapper;
	
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
	public void testInsertT() {
		Calendar c = Calendar.getInstance();
		TestBean t = new TestBean("blah", 1L, c);
		dao.insert(t);
		verify(mock).update("INSERT INTO test_bean (string_prop, long_prop, timets) VALUES (?, ?, ?)", new Object[] {"blah", 1L, c});
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


}
