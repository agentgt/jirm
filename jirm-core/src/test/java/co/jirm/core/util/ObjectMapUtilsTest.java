package co.jirm.core.util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class ObjectMapUtilsTest {

	@Before
	public void setUp() throws Exception {}

	@Test
	public void testEnumFromOrdinal() {
		Enum<?> e = ObjectMapUtils.enumFrom(BlahEnum.class, 0);
		System.out.println(e);
		assertEquals(BlahEnum.ONE, e);
	}
	
	@Test
	public void testEnumFromName() {
		Enum<?> e = ObjectMapUtils.enumFrom(BlahEnum.class, "TWO");
		System.out.println(e);
		assertEquals(BlahEnum.TWO, e);
	}
	
	public static enum BlahEnum {
		ONE,
		TWO
	}

}
