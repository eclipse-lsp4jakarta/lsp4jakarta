package org.example.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InterceptorBinding;
import jakarta.interceptor.InvocationContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sample using Jakarta Interceptor 2.1 (EE 10).
 * EE 9=2.0.0, EE 10=2.1.0, EE 11=2.2.0 — version-distinct per tier.
 */
@Interceptor
@LoggingInterceptorEE10.LoggedEE10
public class LoggingInterceptorEE10 {

    @AroundInvoke
    public Object logInvocation(InvocationContext context) throws Exception {
        System.out.println("[EE10 Interceptor 2.1] Before: " + context.getMethod().getName());
        try {
            return context.proceed();
        } finally {
            System.out.println("[EE10 Interceptor 2.1] After: " + context.getMethod().getName());
        }
    }

    @InterceptorBinding
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.METHOD})
    public @interface LoggedEE10 {}
}
