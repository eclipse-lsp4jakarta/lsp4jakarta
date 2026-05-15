package io.openliberty.sample.jakarta.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public final class FinalModifiers {

	@Id
	private int id;
	
    final int x = 1;
    final String y = "hello", z = "world";
    
    public final int methody() {
        final int ret = 100;
        return 100 + ret;
    }
}