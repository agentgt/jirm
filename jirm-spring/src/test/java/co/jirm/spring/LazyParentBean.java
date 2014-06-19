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
package co.jirm.spring;

import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
