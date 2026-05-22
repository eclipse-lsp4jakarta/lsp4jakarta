package io.openliberty.sample.jakarta.servlet.declareroles;

import jakarta.annotation.security.DeclareRoles;
import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@DeclareRoles("Administrator")
public class DeclareRolesWithGenericServlet extends GenericServlet {
    
    @Override
    public void service(ServletRequest req, ServletResponse res) {
        // Implementation
    }
}
