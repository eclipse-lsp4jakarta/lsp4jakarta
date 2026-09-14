package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EntityMissingConstructor {

	@Id
	private int id;
	
    private EntityMissingConstructor(int x) {}

}