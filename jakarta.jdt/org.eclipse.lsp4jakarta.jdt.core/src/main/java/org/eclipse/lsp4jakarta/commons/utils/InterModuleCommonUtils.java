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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
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
            // Check if the type has @Interceptor annotation (no method iteration needed)
            if (isInterceptorType(type, unit)) {
                return true;
            }
            IMethod[] methods = type.getMethods();
            // Check if the type or methods use interceptor-specific annotations or features
            if (hasInterceptorMethodAnnotations(type, methods)) {
                return true;
            }
            /**
             * Check if any method uses InvocationContext parameter
             * Commented out due to below SPEC conflicts. @PreDestory and @PostConstruct have different validations depending on modules for the same functionality.
             * Can be uncommented for future use
             * Interceptor Spec: https://jakarta.ee/specifications/interceptors/2.0/interceptors-spec-2.0#:~:text=Interceptor%20methods%
             * 20must%20always%20call%20the%20InvocationContext.proceed%20method%20or%20no%20subsequent%20interceptor%20methods%2C%20target
             * %20class%20method%2C%20or%20lifecycle%20callback%20methods%20will%20be%20invoked%2C%20or%E2%80%94in%20the%20case%20of%20around
             * %2Dconstruct%20interceptor%20methods%E2%80%94the%20target%20instance%20will%20not%20be%20created
             * Annotation Spec: https://jakarta.ee/specifications/annotations/2.0/annotations-spec-2.0#jakarta-annotation-postconstruct
             */
//            if (hasInvocationContextParameter(type, methods)) {
//                return true;
//            }
        }
        return false;
    }

    /**
     * Checks if the type has any methods annotated with interceptor-specific annotations.
     * Checks for: @AroundInvoke, @AroundConstruct, @AroundTimeout
     *
     * @param type the type to check
     * @param methods the methods array (pre-fetched to avoid redundant calls)
     * @return true if any method uses interceptor-specific annotations
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private static boolean hasInterceptorMethodAnnotations(IType type, IMethod[] methods) throws JavaModelException {
        String[] interceptorReferences = Constants.INTERCEPTOR_REFERENCES.toArray(String[]::new);
        return Arrays.stream(methods).flatMap(method -> {
            try {
                return Arrays.stream(method.getAnnotations());
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to get method annotations", e);
                return Arrays.stream(new IAnnotation[0]);
            }
        }).anyMatch(annotation -> {
            try {
                String annotationName = annotation.getElementName();
                return DiagnosticUtils.getMatchedJavaElementName(type, annotationName,
                                                                 interceptorReferences) != null;
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to check method annotation", e);
                return false;
            }
        });
    }

    /**
     * Checks if any method in the type uses InvocationContext as a parameter.
     * Methods with InvocationContext parameters are interceptor methods.
     *
     * @param type the type to check
     * @param methods the methods array (pre-fetched to avoid redundant calls)
     * @return true if any method has InvocationContext parameter
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private static boolean hasInvocationContextParameter(IType type, IMethod[] methods) throws JavaModelException {
        return Arrays.stream(methods).flatMap(method -> {
            return Arrays.stream(method.getParameterTypes());
        }).anyMatch(paramType -> {
            try {
                String typeName = DiagnosticUtils.getDataTypeName(paramType);
                return DiagnosticUtils.isMatchedJavaElement(type, typeName,
                                                            Constants.JAKARTA_INTERCEPTOR_INVOCATION_CONTEXT);
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to check parameter type", e);
                return false;
            }
        });
    }

    /**
     * Checks if the type is an interceptor type (has @Interceptor annotation).
     *
     * @param type the type to check
     * @param unit the compilation unit
     * @return true if the type has @Interceptor annotation
     * @throws JavaModelException if there's an error accessing the Java model
     */
    public static boolean isInterceptorType(IType type, ICompilationUnit unit) throws JavaModelException {
        return Arrays.stream(type.getAnnotations()).anyMatch(annotation -> {
            try {
                return DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.INTERCEPTOR_FQ_NAME);
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to find matching annotation", e);
                return false;
            }
        });
    }
}