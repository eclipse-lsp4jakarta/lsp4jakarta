package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom interceptor binding annotation for testing.
 * This annotation is marked with @InterceptorBinding meta-annotation.
 */
@InterceptorBinding
@Target(TYPE)
@Retention(RUNTIME)
public @interface Monitored {
}