package co.jirm.spring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import co.jirm.orm.JirmFactory;
import co.jirm.orm.dao.JirmDao;

import com.google.common.collect.Lists;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:/spring/test-applicationContext.xml")
public class SqlObjectDaoIntegrationTest {


	@Autowired
	private JirmFactory jirmFactory;
	private JirmDao<TestBean> dao;
	
	@Before
	public void setUp() throws Exception {
		dao = jirmFactory.daoFor(TestBean.class);
	}

	@Test
	public void testAll() throws Exception {
		TestBean t = new TestBean(randomId(), 2L, Calendar.getInstance());
		dao.insert(t);
		List<TestBean> list = //dao.queryForListByFilter("stringProp", "hello");
				dao.select().where()
					.property("stringProp").eq("hello")
					.query()
					.forList();
		long count = dao.count().query().forLong();
		assertTrue(count > 0);
		
		TestBean newT = new TestBean(t.getStringProp(), 50, Calendar.getInstance());
		dao.update(newT);
		TestBean reloaded = dao.reload(newT);
		assertTrue(reloaded != newT);
		assertTrue(reloaded.getStringProp().equals(newT.getStringProp()));
		assertNotNull(list);
		
	}
	
	@Test
	public void testAdding() throws Exception {
		String id = randomId();
		TestBean t = new TestBean(id, 2L, Calendar.getInstance());
		dao.insert(t);
		int i = dao.update().plus("longProp", 100).where().property("stringProp", id).execute();
		assertTrue(i > 0);
		
	}
	
	@Test
	public void testManyToOne() throws Exception {
		TestBean t = new TestBean(randomId(), 2L, Calendar.getInstance());
		dao.insert(t);
		ParentBean pb = new ParentBean(randomId(), t);
		jirmFactory.daoFor(ParentBean.class).insert(pb);
		jirmFactory.daoFor(GrandParentBean.class).insert(new GrandParentBean(randomId(), pb));
		
		pb = jirmFactory.daoFor(ParentBean.class).reload(pb);
		LazyGrandParentBean lazy = jirmFactory.daoFor(LazyGrandParentBean.class)
				.select()
					.where("{{parent.id}} = ?").with(pb.getId())
					.query()
					.forObject();
		assertTrue( ! lazy.getParent().getTest().isPresent());
		
		assertNotNull(pb.getTest());
		
	}
	
	@Test
	public void testBatch() throws Exception {
		List<TestBean> batchBeans = Lists.newArrayListWithExpectedSize(300);
		
		for (int i = 0; i < 300; i++) {
			TestBean t = new TestBean(randomId(), 5000L, Calendar.getInstance());
			batchBeans.add(t);
		}
		
		dao.insert(batchBeans.iterator(), 50);
		dao.delete()
			.where()
			.property("longProp", 5000L)
			.execute();
	}
	
	@Ignore
	@Test
	public void testNonBatch() throws Exception {
		List<TestBean> batchBeans = Lists.newArrayListWithExpectedSize(300);
		
		for (int i = 0; i < 300; i++) {
			TestBean t = new TestBean(randomId(), 5000L, Calendar.getInstance());
			batchBeans.add(t);
		}
		
		for (TestBean t: batchBeans) {
			dao.insert(t);
		}
	}
	
	
	@Ignore
	@Test
	public void testJooq() throws Exception {
		crap();
	}

	@Transactional
	public void crap() {
//		Settings s = new Settings();
//		s.getExecuteListeners().add("com.snaphop.jooq.SpringExceptionTranslationExecuteListener");
//		PublicFactory p = new PublicFactory(ds, s);
//		
//		JobRecord jr = p.newRecord(JOB);
//		jr.setBatchId("FAIL");
//		jr.setCreatets(new Timestamp(new LocalDateTime().toDateTime().getMillis()));
//		jr.setEndts(new Timestamp(new LocalDateTime().toDateTime().getMillis()));
//		jr.setId("FAIL");
//		jr.setObjectId("FAIL");
//		jr.setTriggerts(new Timestamp(new LocalDateTime().toDateTime().getMillis()));
//		jr.setUpdatets(new Timestamp(new LocalDateTime().toDateTime().getMillis()));
//		jr.setStatus("STARTED");
//		jr.setType("CRAP");
//		jr.store();
//		
//		//PublicFactory a = new PublicFactory(ds, s);
//
//		p.select(JOB.BATCH_ID, JOB.CREATETS).from(JOB).where("s = 'shit'").fetch();
	}
	
	public static class Crap {
		
		private final String name;

		@JsonCreator
		public Crap(@JsonProperty ("name") String name) {
			super();
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
		
	
	}
	
	public static String randomId() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
}
