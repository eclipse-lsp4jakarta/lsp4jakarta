package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateful;

// Valid: public, non-final, non-abstract, top-level — no constructor (default applies).
@Stateful
public class ValidStatefulBean {
}
