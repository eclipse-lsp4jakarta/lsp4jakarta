package io.openliberty.sample.jakarta.servlet.declareroles;

import jakarta.annotation.security.DeclareRoles;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@DeclareRoles("Administrator")
public class DeclareRolesWithHttpServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Implementation
    }
}
