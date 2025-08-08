package io.openliberty.sample.jakarta.validation;

import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;

public class MethodConstraintValidation {

    // valid cases
    @AssertFalse
    private boolean falseMethod() {
        return false;
    }

    @AssertTrue
    public boolean trueMethod() {
        return true;
    }

    // invalid cases
    @AssertTrue
    public static boolean anotherTruth() {  // static
        return true;
    }

    @AssertTrue
    public String notBoolean() {            // invalid type
        return "aha!";
    }

    @AssertFalse
    private static int notBoolTwo(int x) {  // invalid type, static
        return x;
    }
   
}