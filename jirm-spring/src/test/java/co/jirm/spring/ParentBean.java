package co.jirm.spring;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


public class ParentBean {

	@Id
	private final String id;
	@ManyToOne(targetEntity=TestBean.class, fetch=FetchType.EAGER)
	private final TestBean test;
	
	@JsonCreator
	public ParentBean(
			@JsonProperty("id") String id, 
			@JsonProperty("test") TestBean test) {
		super();
		this.id = id;
		this.test = test;
	}
	
	
	public String getId() {
		return id;
	}
	public TestBean getTest() {
		return test;
	}
	
	
}
