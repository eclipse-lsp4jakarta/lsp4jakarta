package io.openliberty.sample.jakarta.faces;

import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * A @FacesValidator class implementing the raw Validator interface (without a
 * generic type parameter). Should NOT produce a diagnostic.
 */
@FacesValidator("rawValidator")
@SuppressWarnings("rawtypes")
public class RawValidatorImpl implements Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // validation logic
    }
}
