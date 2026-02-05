package io.openliberty.sample.jakarta.jaxrs;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

@Path("/somewhere")
public class ConstraintOnSetter {

	String name;

	public String getName() {
		return name;
	}

	public void setName(@NotNull String name) {
		this.name = name;
	}
	
	
}
