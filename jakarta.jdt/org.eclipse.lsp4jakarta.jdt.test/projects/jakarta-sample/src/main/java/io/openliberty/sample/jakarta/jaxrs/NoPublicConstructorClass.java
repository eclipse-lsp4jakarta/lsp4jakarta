package io.openliberty.sample.jakarta.jaxrs;

import jakarta.ws.rs.Path;

@Path("/somewhere")
public class NoPublicConstructorClass {

    private NoPublicConstructorClass() {

    }

    protected NoPublicConstructorClass(int arg1) {

    }

}