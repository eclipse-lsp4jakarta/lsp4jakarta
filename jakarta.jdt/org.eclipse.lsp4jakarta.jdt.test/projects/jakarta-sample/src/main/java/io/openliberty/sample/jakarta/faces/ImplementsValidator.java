package io.openliberty.sample.jakarta.faces;

import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

@FacesValidator("customValidator")
public class ImplementsValidator implements Validator<Object> {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        // validation logic
    }
}
