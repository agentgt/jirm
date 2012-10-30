package co.jirm.orm.dao;

public class TestBeanBuilder {
	private java.lang.String stringProp;
	private long longProp;
	private java.util.Calendar timeTS;

	public TestBeanBuilder() {}

	public TestBeanBuilder(TestBean o) {
		this.stringProp = o.getStringProp();
		this.longProp = o.getLongProp();
		this.timeTS = o.getTimeTS();
	}
	public TestBeanBuilder stringProp(java.lang.String p) {
		this.stringProp = p;
		return this;
	}
	public TestBeanBuilder longProp(long p) {
		this.longProp = p;
		return this;
	}
	public TestBeanBuilder timeTS(java.util.Calendar p) {
		this.timeTS = p;
		return this;
	}
	public TestBean build() {
		return new TestBean(stringProp, longProp, timeTS);
	}
}