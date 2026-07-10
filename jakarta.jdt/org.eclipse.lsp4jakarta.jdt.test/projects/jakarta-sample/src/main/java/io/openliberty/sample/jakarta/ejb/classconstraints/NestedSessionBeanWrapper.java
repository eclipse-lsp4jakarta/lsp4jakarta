package io.openliberty.sample.jakarta.ejb.classconstraints;

import jakarta.ejb.Stateful;

/**
 * Test resource for the top-level class constraint.
 * The inner class NestedStatefulBean is annotated with @Stateful but is not
 * a top-level class, which violates Jakarta Enterprise Beans 4.0 spec section 4.1.
 */
public class NestedSessionBeanWrapper {

    @Stateful
    public class NestedStatefulBean {
        public NestedStatefulBean() {
        }
    }
}
