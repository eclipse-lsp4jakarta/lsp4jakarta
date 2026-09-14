package io.openliberty.sample.jakarta.cdi;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A plain custom annotation that is NOT meta-annotated with @NormalScope.
 * Placing this on a class does NOT make it a CDI bean.
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD})
public @interface NotAScope {
}
