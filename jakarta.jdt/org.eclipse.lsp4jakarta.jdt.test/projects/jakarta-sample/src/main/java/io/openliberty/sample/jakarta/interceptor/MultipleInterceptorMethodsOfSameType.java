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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Interceptor
public class MultipleInterceptorMethodsOfSameType {

    // Multiple @AroundInvoke methods - INVALID
    @AroundInvoke
    public Object log1(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundInvoke
    public Object log2(InvocationContext ctx) throws Exception {
        return ctx.proceed(); // ERROR: duplicate @AroundInvoke
    }

    @AroundInvoke
    public Object log3(InvocationContext ctx) throws Exception {
        return ctx.proceed(); // ERROR: duplicate @AroundInvoke
    }

    // Multiple @AroundTimeout methods - INVALID
    @AroundTimeout
    public Object timeout1(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    @AroundTimeout
    public Object timeout2(InvocationContext ctx) throws Exception {
        return ctx.proceed(); // ERROR: duplicate @AroundTimeout
    }

    // Multiple @PostConstruct methods - INVALID
    @PostConstruct
    public void init1(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    @PostConstruct
    public void init2(InvocationContext ctx) throws Exception {
        ctx.proceed(); // ERROR: duplicate @PostConstruct
    }

    // Multiple @PreDestroy methods - INVALID
    @PreDestroy
    public void destroy1(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    @PreDestroy
    public void destroy2(InvocationContext ctx) throws Exception {
        ctx.proceed(); // ERROR: duplicate @PreDestroy
    }

    // Multiple @AroundConstruct methods - INVALID
    @AroundConstruct
    public void construct1(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    @AroundConstruct
    public void construct2(InvocationContext ctx) throws Exception {
        ctx.proceed(); // ERROR: duplicate @AroundConstruct
    }

    // Nested class with duplicates
    @Interceptor
    public static class NestedInterceptor {
        @AroundInvoke
        public Object nested1(InvocationContext ctx) throws Exception {
            return ctx.proceed();
        }

        @AroundInvoke
        public Object nested2(InvocationContext ctx) throws Exception {
            return ctx.proceed(); // ERROR: duplicate @AroundInvoke in nested class
        }
    }
}
