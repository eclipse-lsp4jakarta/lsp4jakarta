package io.openliberty.sample.jakarta.persistence;

import java.util.Date;

import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

public class EntityIdDate {

	@Id
	@Temporal(value = TemporalType.DATE)
	private Date pk;

	public Date getPk() {
		return pk;
	}

	public void setPk(Date pk) {
		this.pk = pk;
	}
	
	
}
