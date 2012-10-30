package co.jirm.orm.dao;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


@Table(name="grand_parent_bean")
public class LazyGrandParentBean {

	@Id
	private final String id;
	
	@ManyToOne(targetEntity=LazyParentBean.class, fetch=FetchType.LAZY)
	private final LazyParentBean parent;
	
	@JsonCreator
	public LazyGrandParentBean(
			@JsonProperty("id") String id, 
			@JsonProperty("parent") LazyParentBean parent) {
		super();
		this.id = id;
		this.parent = parent;
	}
	
	
	public String getId() {
		return id;
	}
	
	public LazyParentBean getParent() {
		return parent;
	}
	
}
