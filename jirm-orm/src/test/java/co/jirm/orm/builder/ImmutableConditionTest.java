package co.jirm.orm.builder;

import org.junit.Before;
import org.junit.Test;

import co.jirm.core.sql.MutableParameters;
import co.jirm.core.sql.Parameters;
import co.jirm.orm.builder.ConditionVisitors.ParametersConditionVisitor;


public class ImmutableConditionTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testWhereString() {
		ImmutableCondition c = ImmutableCondition.where("kyle=?")
				.and("stuff=?").with("crap")
				.or("bingo=?").with("blah")
				.or("blah=?").with("kingo")
				.and("blah=?").with("kingo");
		final StringBuilder sb = new StringBuilder();
		
		final MutableParameters mp = new MutableParameters();
		
		c.accept(new ParametersConditionVisitor() {
			@Override
			public void doParameters(Parameters ps) {
				mp.addAll(ps);
			}
		});
		
		ConditionVisitor v = ConditionVisitors.conditionVisitor(sb);
		c.accept(v);
		System.out.println(sb.toString());
		System.out.println(mp.getParameters());
		
		
	}

}
