package io.openliberty.sample.jakarta.persistence.context;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Valid: Jakarta Servlet. The container manages the lifecycle.
// @PersistenceContext injection is valid in servlets.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@WebServlet("/example")
public class PersistenceContextInServletValid extends HttpServlet {

    @PersistenceContext
    private EntityManager em;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // use em safely
    }
}
