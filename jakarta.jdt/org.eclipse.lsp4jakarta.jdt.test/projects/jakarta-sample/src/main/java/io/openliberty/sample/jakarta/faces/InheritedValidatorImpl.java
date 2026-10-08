package io.openliberty.sample.jakarta.faces;

import jakarta.faces.validator.FacesValidator;

/**
 * A @FacesValidator class that inherits the Validator interface from its
 * superclass. Should NOT produce a diagnostic (inherited implementation is valid).
 */
@FacesValidator("inheritedValidator")
public class InheritedValidatorImpl extends ValidatorBase {
    // Validator interface is satisfied through the superclass
}
