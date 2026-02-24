package io.openliberty.sample.jakarta.persistence;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityPropertyIdDateMissingTemporal {

	private Date pk;

	@Id
	public Date getPk() {
		return pk;
	}

	public void setPk(Date pk) {
		this.pk = pk;
	}
	
	
}
