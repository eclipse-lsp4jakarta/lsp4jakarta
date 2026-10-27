package io.openliberty.sample.jakarta.persistence.entitygraph;

import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class NamedEntityGraphSuperclass {

    private String superField;

    public String getSuperField() {
        return superField;
    }

    public void setSuperField(String superField) {
        this.superField = superField;
    }

    public String getSuperGetterProperty() {
        return "superGetter";
    }

    public boolean isSuperActive() {
        return true;
    }
}
