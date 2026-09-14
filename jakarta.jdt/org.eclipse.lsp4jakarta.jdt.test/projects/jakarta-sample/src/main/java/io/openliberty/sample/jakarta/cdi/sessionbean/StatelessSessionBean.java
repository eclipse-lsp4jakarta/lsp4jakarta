package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.RequestScoped;

// Invalid: Stateless with RequestScoped
@Stateless
@RequestScoped
public class StatelessSessionBean {
}
