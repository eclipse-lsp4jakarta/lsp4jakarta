package io.openliberty.sample.jakarta.cdi;

import jakarta.ejb.Stateless;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.Dependent;

// Invalid: Stateless with RequestScoped
@Stateless
@RequestScoped
public class StatelessSessionBean {
}

// Invalid: Stateless with SessionScoped
@Stateless
@SessionScoped
public class StatelessWithSessionScoped {
}

// Invalid: Stateless with multiple scopes including Dependent
@Stateless
@Dependent
@RequestScoped
public class StatelessWithMultipleScopes {
}

// Valid: Stateless with no explicit scope (defaults to @Dependent)
@Stateless
public class StatelessWithNoScope {
}

// Valid: Stateless with only Dependent
@Stateless
@Dependent
public class StatelessWithDependent {
}
            
