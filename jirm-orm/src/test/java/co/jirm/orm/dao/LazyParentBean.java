package co.jirm.orm.dao;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Optional;


@Table(name = "parent_bean")
public class LazyParentBean {

	@Id
	private final String id;
	@ManyToOne(targetEntity=TestBean.class, fetch=FetchType.LAZY)
	private final Optional<TestBean> test;
	
	@JsonCreator
	public LazyParentBean(
			@JsonProperty("id") String id, 
			@JsonProperty("test") Optional<TestBean> test) {
		super();
		this.id = id;
		this.test = test;
	}
	
	
	public String getId() {
		return id;
	}
	public Optional<TestBean> getTest() {
		return test;
	}
	
	
}
