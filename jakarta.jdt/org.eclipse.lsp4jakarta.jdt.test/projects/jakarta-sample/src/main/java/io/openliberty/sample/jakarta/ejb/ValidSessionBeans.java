package io.openliberty.sample.jakarta.ejb;

import jakarta.ejb.Stateless;
import jakarta.ejb.Stateful;
import jakarta.ejb.Singleton;

/**
 * Valid session bean classes — public, non-final, non-abstract, top-level.
 * No diagnostics should be produced for any class in this file.
 */

// Valid: public, non-final, non-abstract, top-level — explicit no-arg constructor
@Stateless
public class ValidStatelessBeanExplicit {
    public ValidStatelessBeanExplicit() {
    }
}

// Valid: public, non-final, non-abstract, top-level — no constructor (default applies)
@Stateful
public class ValidStatefulBeanNoConstructor {
}

// Valid: public Singleton with explicit constructor
@Singleton
public class ValidSingletonBeanExplicit {
    public ValidSingletonBeanExplicit() {
    }
}
