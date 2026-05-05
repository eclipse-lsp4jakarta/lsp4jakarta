/*******************************************************************************
* Copyright (c) 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.commons.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.interceptor.Constants;

/**
 * Utilities for working with cross module common functionalities of annotations, fields, methods etc.
 */
public class InterModuleCommonUtils {

    private static final Logger LOGGER = Logger.getLogger(InterModuleCommonUtils.class.getName());

    /**
     * Checks if type is of Interceptor type or uses interceptor-related features.
     * Returns true if:
     * The type has @Interceptor annotation
     * The type or its methods use interceptor-specific annotations (AroundInvoke, AroundConstruct, AroundTimeout)
     * The type or its methods use @Interceptors annotation
     * Any method uses InvocationContext parameter (indicating it's an interceptor method)
     *
     * Note: This excludes PostConstruct and PreDestroy as they belong to the annotations module.
     *
     * @param type the type to check
     * @param unit the compilation unit
     * @return true if the type is an interceptor type or uses interceptor-related features
     * @throws JavaModelException if there's an error accessing the Java model
     */
    public static boolean isInterceptorReferencedType(IType type, ICompilationUnit unit) throws JavaModelException {
        if (type != null) {
            // Check if the type has @Interceptor annotation
            if (isInterceptorType(type)) {
                return true;
            }
            // Check if the type or methods use interceptor-specific annotations or features
            if (hasInterceptorMethodAnnotations(type)) {
                return true;
            }
            // Check if the type uses @Interceptors annotation (class-level or method-level)
            if (hasInterceptorsAnnotation(type)) {
                return true;
            }
            // Check if any method uses InvocationContext parameter
            if (hasInvocationContextParameter(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the type has any methods annotated with interceptor-specific annotations.
     * Checks for: @AroundInvoke, @AroundConstruct, @AroundTimeout
     *
     * @param type the type to check
     * @return true if any method uses interceptor-specific annotations
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private static boolean hasInterceptorMethodAnnotations(IType type) throws JavaModelException {
        // Convert Set to array for getMatchedJavaElementName
        String[] interceptorReferences = Constants.INTERCEPTOR_REFERENCES.toArray(String[]::new);
        // Check all methods for interceptor-specific annotations
        for (var method : type.getMethods()) {
            for (var annotation : method.getAnnotations()) {
                String annotationName = annotation.getElementName();
                // Use DiagnosticUtils.getMatchedJavaElementName to check if this annotation
                // matches any of the interceptor-specific annotations (excludes PostConstruct/PreDestroy)
                String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type, annotationName,
                                                                                     interceptorReferences);
                if (matchedAnnotation != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the type or its methods use @Interceptors annotation.
     * The @Interceptors annotation is used to bind interceptors to a class or method.
     *
     * @param type the type to check
     * @return true if @Interceptors annotation is found
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private static boolean hasInterceptorsAnnotation(IType type) throws JavaModelException {
        // Check class-level @Interceptors annotation
        boolean hasClassLevelAnnotation = Arrays.stream(type.getAnnotations()).anyMatch(annotation -> {
            try {
                return DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                            Constants.INTERCEPTORS_FQ_NAME);
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to match class annotation", e.getMessage());
                return false;
            }
        });

        if (hasClassLevelAnnotation) {
            return true;
        }
        // Check method-level @Interceptors annotation using for loops (nested iteration)
        for (var method : type.getMethods()) {
            for (var annotation : method.getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                         Constants.INTERCEPTORS_FQ_NAME)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if any method in the type uses InvocationContext as a parameter.
     * Methods with InvocationContext parameters are interceptor methods.
     *
     * @param type the type to check
     * @return true if any method has InvocationContext parameter
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private static boolean hasInvocationContextParameter(IType type) throws JavaModelException {
        for (var method : type.getMethods()) {
            String[] parameterTypes = method.getParameterTypes();
            for (String paramType : parameterTypes) {
                // Get the resolved type name
                String typeName = DiagnosticUtils.getDataTypeName(paramType);
                // Check if it matches InvocationContext
                if (DiagnosticUtils.isMatchedJavaElement(type, typeName, Constants.JAKARTA_INTERCEPTOR_INVOCATION_CONTEXT)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Method checks if the type is interceptor
     *
     * @param type
     * @return
     * @throws JavaModelException
     */
    public static boolean isInterceptorType(IType type) throws JavaModelException {
        return Arrays.stream(type.getAnnotations()).filter(Objects::nonNull).anyMatch(annotation -> {
            try {
                return DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                            Constants.INTERCEPTOR_FQ_NAME);
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to find matching annotation", e.getMessage());
                return false;
            }
        });
    }
}