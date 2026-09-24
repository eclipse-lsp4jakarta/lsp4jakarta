package org.example.faces;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Sample JSF Validator using:
 *  - Jakarta Faces 4.0 (EE 10)
 *
 * Replaces jakarta.ejb-api:4.0.1 and jakarta.mail-api:2.1.3 which both share
 * the same version in EE 11 and are therefore not version-distinct for EE 10.
 * Jakarta Faces: EE 9=3.0.0, EE 10=4.0.1, EE 11=4.1.0 — uniquely identifies EE 10.
 */
@FacesValidator("productNameValidator")
public class ProductNameValidator implements Validator<String> {

    private static final int MAX_LENGTH = 100;

    @Override
    public void validate(FacesContext context, UIComponent component, String value)
            throws ValidatorException {
        if (value == null || value.isBlank()) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Name is required", null));
        }
        if (value.length() > MAX_LENGTH) {
            throw new ValidatorException(
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Name must not exceed " + MAX_LENGTH + " characters", null));
        }
    }
}
