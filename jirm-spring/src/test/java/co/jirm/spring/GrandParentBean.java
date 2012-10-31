package co.jirm.spring;

import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


public class GrandParentBean {

	@Id
	private final String id;
	@ManyToOne(targetEntity=ParentBean.class)
	private final ParentBean parent;
	
	@JsonCreator
	public GrandParentBean(
			@JsonProperty("id") String id, 
			@JsonProperty("parent") ParentBean parent) {
		super();
		this.id = id;
		this.parent = parent;
	}
	
	
	public String getId() {
		return id;
	}
	
	public ParentBean getParent() {
		return parent;
	}
	
}
