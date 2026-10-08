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
 * Valid dataset for issue #693:
 * A component class that declares or inherits a class-level interceptor binding
 * (@Monitored is meta-annotated with @InterceptorBinding) must not be declared
 * final, or have any non-static, non-private, final methods.
 */

// VALID: no final modifier on class or non-private/non-static methods - no diagnostic expected
@Monitored
public class ValidInterceptorBindingClassModifiers {

    @AroundInvoke
    public Object intercept(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    // Valid: public non-final method
    public void publicHelper() {
    }

    // Valid: public static non-final method
    public static void publicStaticNonFinalHelper() {
    }
}
