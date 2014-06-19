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

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class TestBean {
	
	@Id
	private final String stringProp;
	private final long longProp;
	@Column(name="timets")
	private final Calendar timeTS;
	
	@JsonCreator
	public TestBean(
			@JsonProperty("stringProp") String stringProp, 
			@JsonProperty("longProp") long longProp,
			@JsonProperty("timeTS") Calendar timeTS ) {
		super();
		this.stringProp = stringProp;
		this.longProp = longProp;
		this.timeTS = timeTS;
	}
	
	public String getStringProp() {
		return stringProp;
	}
	public long getLongProp() {
		return longProp;
	}
	
	public Calendar getTimeTS() {
		return timeTS;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (longProp ^ (longProp >>> 32));
		result = prime * result + ((stringProp == null) ? 0 : stringProp.hashCode());
		result = prime * result + ((timeTS == null) ? 0 : timeTS.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestBean other = (TestBean) obj;
		if (longProp != other.longProp)
			return false;
		if (stringProp == null) {
			if (other.stringProp != null)
				return false;
		}
		else if (!stringProp.equals(other.stringProp))
			return false;
		if (timeTS == null) {
			if (other.timeTS != null)
				return false;
		}
		else if (!timeTS.equals(other.timeTS))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TestBean [stringProp=" + stringProp + ", longProp=" + longProp + ", timeTS=" + timeTS + "]";
	}
	
	
	
	

}
