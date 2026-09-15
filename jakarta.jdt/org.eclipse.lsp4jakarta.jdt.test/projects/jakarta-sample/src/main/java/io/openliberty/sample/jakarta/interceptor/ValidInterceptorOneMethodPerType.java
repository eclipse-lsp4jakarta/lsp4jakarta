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

@Monitored
@Interceptor
public class ValidInterceptorOneMethodPerType {

    // Valid: Only one @AroundInvoke method
    @AroundInvoke
    public Object log(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    // Valid: Only one @AroundTimeout method
    @AroundTimeout
    public Object timeout(InvocationContext ctx) throws Exception {
        return ctx.proceed();
    }

    // Valid: Only one @PostConstruct method
    @PostConstruct
    public void init(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    // Valid: Only one @PreDestroy method
    @PreDestroy
    public void destroy(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }

    // Valid: Only one @AroundConstruct method
    @AroundConstruct
    public void construct(InvocationContext ctx) throws Exception {
        ctx.proceed();
    }
}
