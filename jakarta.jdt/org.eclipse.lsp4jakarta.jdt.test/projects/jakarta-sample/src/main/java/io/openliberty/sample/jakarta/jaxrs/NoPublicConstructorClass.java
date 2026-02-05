package io.openliberty.sample.jakarta.jaxrs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Path;

@Path("/somewhere")
public class NoPublicConstructorClass {

    String name;
    

    public NoPublicConstructorClass(String name,JB jb) {
	}

	protected NoPublicConstructorClass(int arg1) {

    }
	
	public void setname(@BeanParam JB jb) {
		
	}

}
