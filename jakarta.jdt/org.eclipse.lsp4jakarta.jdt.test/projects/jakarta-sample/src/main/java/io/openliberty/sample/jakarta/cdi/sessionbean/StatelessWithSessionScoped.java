package io.openliberty.sample.jakarta.cdi.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.SessionScoped;

// Invalid: Stateless with SessionScoped
@Stateless
@SessionScoped
public class StatelessWithSessionScoped {
}
