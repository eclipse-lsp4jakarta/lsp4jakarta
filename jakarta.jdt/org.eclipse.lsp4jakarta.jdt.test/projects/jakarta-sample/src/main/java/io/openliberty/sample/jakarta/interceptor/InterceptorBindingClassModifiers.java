/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package io.openliberty.sample.jakarta.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;

/**
 * Dataset for issue #693:
 * A component class that declares or inherits a class-level interceptor binding
 * (@Monitored is meta-annotated with @InterceptorBinding) must not be declared
 * final, or have any non-static, non-private, final methods.
 */

// INVALID: final class - triggers InvalidFinalInterceptorBindingClass
@Monitored
final class InvalidFinalInterceptorBindingClass {

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }
}

// INVALID: non-static, non-private final methods - triggers InvalidMethodOnInterceptorBindingClass
@Monitored
class InvalidMethodsOnInterceptorBindingClass {

    // ERROR: public final method (non-static, non-private)
    public final Object intercept(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    // ERROR: protected final method (non-static, non-private)
    protected final void protectedHelper() {
    }

    // ERROR: package-private (default) final method (non-static, non-private)
    final void packagePrivateHelper() {
    }

    // Valid: private final method is permitted
    private final void privateHelper() {
    }

    // Valid: public static final method is permitted
    public static final void publicStaticHelper() {
    }

    // Valid: protected static final method is permitted
    protected static final void protectedStaticHelper() {
    }

    // Valid: package-private static final method is permitted
    static final void packageStaticHelper() {
    }

    // Valid: private static final method is permitted
    private static final void privateStaticHelper() {
    }
}

