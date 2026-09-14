package io.openliberty.sample.jakarta.persistence.context;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

// Valid: @WebListener is a container-managed servlet component.
// @PersistenceContext injection is valid here.
// See: https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791
@WebListener
public class PersistenceContextInWebListenerValid implements ServletContextListener {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void contextInitialized(ServletContextEvent sce) {}

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
