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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that detects wildcard types in bean types.
 *
 * According to CDI specification section 2.2.1:
 * "A parameterized type that contains a wildcard type parameter is not a legal bean type."
 *
 * This applies to:
 * - Injection points (@Inject fields and method parameters)
 * - Producer methods (@Produces methods)
 * - Producer fields (@Produces fields)
 * - Arrays whose component type contains wildcards
 */
public class CdiWildcardDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(CdiWildcardDiagnosticsParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            // Check fields with @Inject and @Produces annotations
            for (IField field : type.getFields()) {
                String[] annotationNames = DiagnosticUtils.getAnnotationNames(field);

                // Use if-else since @Inject and @Produces don't appear on the same field
                if (hasAnnotation(type, annotationNames, Constants.INJECT_FQ_NAME)) {
                    String typeSignature = field.getTypeSignature();
                    if (containsWildcard(typeSignature)) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("InvalidWildcardTypeInInjectField"),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidWildcardTypeInInjectField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (hasAnnotation(type, annotationNames, Constants.PRODUCES_FQ_NAME)) {
                    String typeSignature = field.getTypeSignature();
                    if (containsWildcard(typeSignature)) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("InvalidWildcardTypeInProducerField"),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidWildcardTypeInProducerField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
            }

            // Check methods with @Inject and @Produces annotations
            for (IMethod method : type.getMethods()) {
                String[] annotationNames = DiagnosticUtils.getAnnotationNames(method);

                // Use if-else since @Inject and @Produces don't appear on the same method
                if (hasAnnotation(type, annotationNames, Constants.INJECT_FQ_NAME)) {
                    // Check method parameters for wildcard types
                    String[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (containsWildcard(parameterTypes[i])) {
                            Range range = PositionUtils.toNameRange(method.getParameters()[i], context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("InvalidWildcardTypeInInjectMethod"),
                                                                     range,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     null,
                                                                     ErrorCode.InvalidWildcardTypeInInjectField,
                                                                     DiagnosticSeverity.Error));
                        }
                    }
                } else if (hasAnnotation(type, annotationNames, Constants.PRODUCES_FQ_NAME)) {
                    // Check return type for wildcard types
                    String returnTypeSignature = method.getReturnType();
                    if (containsWildcard(returnTypeSignature)) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("InvalidWildcardTypeInProducerMethod"),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidWildcardTypeInProducerMethod,
                                                                 DiagnosticSeverity.Error));
                    }
                }
            }
        }

        return diagnostics;
    }

    /**
     * Checks if an annotation array contains a specific annotation.
     *
     * @param type the type containing the annotations
     * @param annotationNames array of annotation names to check
     * @param annotationFQName the fully qualified name of the annotation to match
     * @return true if the annotation is found, false otherwise
     */
    private boolean hasAnnotation(IType type, String[] annotationNames, String annotationFQName) {
        return DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                          new String[] { annotationFQName }).size() > 0;
    }

    /**
     * Checks if a type signature contains a wildcard type parameter.
     *
     * This method recursively checks for wildcards in:
     * - Direct wildcard types (?, ? extends T, ? super T)
     * - Parameterized types with wildcard arguments (List<?>, Map<String, ?>)
     * - Array types with wildcard component types (List<?>[], List<?>[][])
     * - Nested generic types (Map<String, List<?>>)
     *
     * Wildcards in Java type signatures are represented as:
     * - '*' for unbounded wildcard (?)
     * - '+' for upper bounded wildcard (? extends)
     * - '-' for lower bounded wildcard (? super)
     *
     * @param typeSignature the type signature to check
     * @return true if the signature contains a wildcard, false otherwise
     */
    private boolean containsWildcard(String typeSignature) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }

        // Check for array types - need to check the component type
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.ARRAY_TYPE_SIGNATURE) {
            String elementType = Signature.getElementType(typeSignature);
            return containsWildcard(elementType);
        }

        // Check for parameterized types
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.CLASS_TYPE_SIGNATURE) {
            String[] typeArguments = Signature.getTypeArguments(typeSignature);
            for (String typeArg : typeArguments) {
                // Check if this type argument is a wildcard
                if (isWildcardSignature(typeArg)) {
                    return true;
                }
                // Recursively check nested type arguments
                if (containsWildcard(typeArg)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if a type signature represents a wildcard type.
     *
     * @param typeSignature the type signature to check
     * @return true if the signature is a wildcard, false otherwise
     */
    private boolean isWildcardSignature(String typeSignature) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }

        // Wildcard signatures start with '*', '+', or '-'
        char firstChar = typeSignature.charAt(0);
        return firstChar == Signature.C_STAR ||
               firstChar == Signature.C_EXTENDS ||
               firstChar == Signature.C_SUPER;
    }
}