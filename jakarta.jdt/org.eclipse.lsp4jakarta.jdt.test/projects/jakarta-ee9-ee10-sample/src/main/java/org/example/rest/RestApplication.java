package org.example.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application config using:
 *  - Jakarta REST (JAX-RS) 3.1 (EE 10)
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
