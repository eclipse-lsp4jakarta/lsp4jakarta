package io.openliberty.sample.jakarta.servlet.declareroles;

import jakarta.annotation.security.DeclareRoles;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@DeclareRoles("Administrator")
public class DeclareRolesWithServletInterface implements Servlet {
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        // Implementation
    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException {
        // Implementation
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {
        // Implementation
    }
}
