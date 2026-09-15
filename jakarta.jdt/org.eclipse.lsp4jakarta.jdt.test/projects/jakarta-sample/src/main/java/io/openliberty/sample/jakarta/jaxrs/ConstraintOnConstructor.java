package io.openliberty.sample.jakarta.jaxrs;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Path;

@Path("/somewhere")
public class ConstraintOnConstructor {

	String name;
	
	public ConstraintOnConstructor(@NotNull String name) {
		
	}
	
}
