package io.openliberty.sample.jakarta.cdi;

import jakarta.enterprise.context.NormalScope;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom normal scope annotation for testing.
 * This is annotated with @NormalScope, making it a normal scope.
 */
@NormalScope
@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD})
public @interface CustomNormalScope {
}
