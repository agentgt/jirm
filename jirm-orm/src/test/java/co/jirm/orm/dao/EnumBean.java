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
package co.jirm.orm.dao;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class EnumBean {
	
	@Id
	private final String id;
	@Enumerated(EnumType.ORDINAL)
	private final MyEnum myEnum;
	
	@JsonCreator
	public EnumBean(
			@JsonProperty("id") String stringProp, 
			@JsonProperty("myEnum") MyEnum myEnum ) {
		super();
		this.id = stringProp;
		this.myEnum = myEnum;
	}
	
	
	public String getId() {
		return id;
	}

	public MyEnum getMyEnum() {
		return myEnum;
	}
	
	public static enum MyEnum {
		FOO,
		BAR
	}

	
	
	
	

}
