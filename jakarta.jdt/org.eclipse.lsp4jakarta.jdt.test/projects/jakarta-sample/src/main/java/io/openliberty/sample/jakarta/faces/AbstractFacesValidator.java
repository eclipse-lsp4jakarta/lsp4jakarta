package io.openliberty.sample.jakarta.faces;

import jakarta.faces.validator.FacesValidator;

/**
 * An abstract class annotated with @FacesValidator but missing the Validator
 * interface implementation. Should produce a diagnostic.
 */
@FacesValidator("abstractValidator")
public abstract class AbstractFacesValidator {
    // Missing Validator interface implementation
}
