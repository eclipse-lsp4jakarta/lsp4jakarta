package io.openliberty.sample.jakarta.faces;

import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * Superclass that implements Validator — used to test that @FacesValidator
 * classes with inherited Validator implementations are not flagged.
 */
public abstract class ValidatorBase implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // base validation logic
    }
}
