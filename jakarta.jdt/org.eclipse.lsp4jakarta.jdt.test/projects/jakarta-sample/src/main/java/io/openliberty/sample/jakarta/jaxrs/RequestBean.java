package io.openliberty.sample.jakarta.jaxrs;

import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;

public class RequestBean {
    @PathParam("userId")
    private String userId;

    @QueryParam("sort")
    private String sort;

}

