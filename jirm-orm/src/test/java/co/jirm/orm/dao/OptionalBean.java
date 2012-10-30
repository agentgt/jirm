package co.jirm.orm.dao;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Id;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Optional;


public class OptionalBean {
	
	@Id
	private final String stringProp;
	private final Optional<Long> longProp;
	@Column(name="timets")
	private final Calendar timeTS;
	
	@JsonCreator
	public OptionalBean(
			@JsonProperty("stringProp") String stringProp, 
			@JsonProperty("longProp") Optional<Long> longProp,
			@JsonProperty("timeTS") Calendar timeTS ) {
		super();
		this.stringProp = stringProp;
		this.longProp = longProp;
		this.timeTS = timeTS;
	}
	
	public String getStringProp() {
		return stringProp;
	}
	public Optional<Long> getLongProp() {
		return longProp;
	}
	
	public Calendar getTimeTS() {
		return timeTS;
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((longProp == null) ? 0 : longProp.hashCode());
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
		OptionalBean other = (OptionalBean) obj;
		if (longProp == null) {
			if (other.longProp != null)
				return false;
		}
		else if (!longProp.equals(other.longProp))
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
		return "OptionalBean [stringProp=" + stringProp + ", longProp=" + longProp + ", timeTS=" + timeTS + "]";
	}
	
	

}
