package co.jirm.spring;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Version;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;


public class LockBean {
	
	@Id
	private final String id;
	private final long longProp;
	@Column(name="timets")
	private final Calendar timeTS;
	@Version
	private final Integer version;
	
	
	@JsonCreator
	public LockBean(
			@JsonProperty("id") String id, 
			@JsonProperty("longProp") long longProp,
			@JsonProperty("timeTS") Calendar timeTS,
			@JsonProperty("version") Integer version) {
		super();
		this.id = id;
		this.longProp = longProp;
		this.timeTS = timeTS;
		this.version = version;
	}
	
	public String getId() {
		return id;
	}
	public long getLongProp() {
		return longProp;
	}
	
	public Calendar getTimeTS() {
		return timeTS;
	}
	public Integer getVersion() {
		return version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (longProp ^ (longProp >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		LockBean other = (LockBean) obj;
		if (longProp != other.longProp)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		}
		else if (!id.equals(other.id))
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
		return "LockBean [id=" + id + ", longProp=" + longProp + ", timeTS=" + timeTS + "]";
	}

}
