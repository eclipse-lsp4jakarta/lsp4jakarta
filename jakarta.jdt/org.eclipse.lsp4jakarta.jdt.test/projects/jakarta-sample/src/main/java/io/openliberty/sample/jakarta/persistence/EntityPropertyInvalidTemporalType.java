package io.openliberty.sample.jakarta.persistence;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class EntityPropertyInvalidTemporalType {

	private Date pk;

	@Id
	@Temporal(TemporalType.TIME)
	public Date getPk() {
		return pk;
	}

	public void setPk(Date pk) {
		this.pk = pk;
	}
}
