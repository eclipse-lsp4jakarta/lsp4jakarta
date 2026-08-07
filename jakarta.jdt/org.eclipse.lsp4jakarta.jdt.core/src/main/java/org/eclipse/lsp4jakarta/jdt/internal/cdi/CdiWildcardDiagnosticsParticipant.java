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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
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
 * CDI diagnostics participant that detects illegal bean types related to wildcards and
 * type variables in producer methods, producer fields, and injection points.
 *
 * <p>Rules enforced (CDI 3.0 spec sections 2.2.1, 3.2, and 3.3):
 * <ul>
 * <li>A parameterized type containing a wildcard is not a legal bean type.</li>
 * <li>A producer method/field whose type is a bare type variable (e.g. {@code T}) or
 * an array of one (e.g. {@code T[]}) is always a definition error.</li>
 * <li>A producer method/field whose type is a parameterized type with a type variable
 * (e.g. {@code List<T>}) must declare scope {@code @Dependent}.</li>
 * </ul>
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

        String[] scopeFQNames = Constants.SCOPE_FQ_NAMES.toArray(String[]::new);

        for (IType type : unit.getAllTypes()) {
            // Hoisted once per type — used by both field and method branches.
            Set<String> typeParamNames = getTypeParameterNames(type);

            for (IField field : type.getFields()) {
                String[] annotationNames = DiagnosticUtils.getAnnotationNames(field);

                if (DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), Constants.INJECT_FQ_NAME)) {
                    if (containsWildcard(field.getTypeSignature())) {
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage(ErrorCode.InvalidWildcardTypeInInjectField.name()),
                                                                 PositionUtils.toNameRange(field, context.getUtils()),
                                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.InvalidWildcardTypeInInjectField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), Constants.PRODUCES_FQ_NAME)) {
                    checkProducerMember(context, uri, diagnostics, type, annotationNames,
                                        field.getTypeSignature(), typeParamNames, scopeFQNames,
                                        PositionUtils.toNameRange(field, context.getUtils()),
                                        new ErrorCode[] { ErrorCode.InvalidWildcardTypeInProducerField,
                                                          ErrorCode.InvalidProducerFieldWithBareTypeVariableType,
                                                          ErrorCode.InvalidProducerFieldWithTypeVariableAndNonDependentScope });
                }
            }

            for (IMethod method : type.getMethods()) {
                String[] annotationNames = DiagnosticUtils.getAnnotationNames(method);

                if (DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.INJECT_FQ_NAME)) {
                    // Check method parameters for wildcard types
                    String[] parameterTypes = method.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (containsWildcard(parameterTypes[i])) {
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage(ErrorCode.InvalidWildcardTypeInInjectMethod.name()),
                                                                     PositionUtils.toNameRange(method.getParameters()[i], context.getUtils()),
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidWildcardTypeInInjectMethod,
                                                                     DiagnosticSeverity.Error));
                        }
                    }
                } else if (DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.PRODUCES_FQ_NAME)) {
                    checkProducerMember(context, uri, diagnostics, type, annotationNames,
                                        method.getReturnType(), typeParamNames, scopeFQNames,
                                        PositionUtils.toNameRange(method, context.getUtils()),
                                        new ErrorCode[] { ErrorCode.InvalidWildcardTypeInProducerMethod,
                                                          ErrorCode.InvalidProducerMethodWithBareTypeVariableReturnType,
                                                          ErrorCode.InvalidProducerMethodWithTypeVariableAndNonDependentScope });
                }
            }
        }

        return diagnostics;
    }

    /**
     * Applies the three CDI type-variable rules for a single {@code @Produces} member
     * (field or method) and appends any violations to {@code diagnostics}.
     *
     * <p>{@code errorCodes[0]} — wildcard in type (always invalid)<br>
     * {@code errorCodes[1]} — bare type variable or array of one (always invalid)<br>
     * {@code errorCodes[2]} — parameterized type with type variable and non-{@code @Dependent} scope
     *
     * @param context the diagnostics context
     * @param uri the compilation unit URI
     * @param diagnostics list to append diagnostics to
     * @param type the enclosing type (used for annotation resolution)
     * @param annotationNames the annotation names on the member
     * @param typeSignature the JDT type signature of the field type / method return type
     * @param typeParamNames class-level type parameter names (e.g. {@code {"T"}})
     * @param scopeFQNames fully-qualified scope annotation names to check against
     * @param range the LSP range for the diagnostic
     * @param errorCodes three error codes indexed by rule (0 = wildcard, 1 = bare, 2 = scope);
     *            the message key for each is derived via {@link ErrorCode#name()}
     */
    private void checkProducerMember(JavaDiagnosticsContext context, String uri,
                                     List<Diagnostic> diagnostics, IType type,
                                     String[] annotationNames, String typeSignature,
                                     Set<String> typeParamNames, String[] scopeFQNames,
                                     Range range, ErrorCode[] errorCodes) throws JavaModelException {
        // Rule 0: wildcard in type
        if (containsWildcard(typeSignature)) {
            diagnostics.add(context.createDiagnostic(uri, Messages.getMessage(errorCodes[0].name()),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     errorCodes[0], DiagnosticSeverity.Error));
        }

        if (typeParamNames.isEmpty()) {
            return;
        }

        // Rule 1: bare type variable (T or T[]) — always invalid
        if (isBareTypeVariable(typeSignature, typeParamNames)) {
            diagnostics.add(context.createDiagnostic(uri, Messages.getMessage(errorCodes[1].name()),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     errorCodes[1], DiagnosticSeverity.Error));
        }
        // Rule 2: parameterized type with type variable — requires @Dependent scope
        else if (containsTypeVariable(typeSignature, typeParamNames)) {
            boolean hasNonDependentScope = DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                                                      scopeFQNames).stream().anyMatch(s -> !Constants.DEPENDENT_FQ_NAME.equals(s));
            if (hasNonDependentScope) {
                diagnostics.add(context.createDiagnostic(uri, Messages.getMessage(errorCodes[2].name()),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         errorCodes[2], DiagnosticSeverity.Error));
            }
        }
    }

    /**
     * Returns the set of type parameter names declared on {@code type}
     * (e.g. {@code {"T", "K", "V"}} for {@code class Foo<T, K, V>}).
     *
     * @param type the enclosing type
     * @return a possibly-empty set of declared type parameter names
     * @throws JavaModelException if the JDT model cannot be accessed
     */
    private Set<String> getTypeParameterNames(IType type) throws JavaModelException {
        Set<String> names = new HashSet<>();
        for (ITypeParameter tp : type.getTypeParameters()) {
            names.add(tp.getElementName());
        }
        return names;
    }

    /**
     * Returns {@code true} if {@code typeSignature} is a bare type variable (e.g. {@code T})
     * or an array whose ultimate element type is a type variable (e.g. {@code T[]}).
     *
     * <p>JDT source files use source-qualified references ({@code QT;}) for unresolved type
     * parameters, so both the resolved ({@code TT;}) and unresolved ({@code QT;}) forms are
     * recognised by comparing against {@code typeParamNames}.
     *
     * @param typeSignature the JDT type signature to check
     * @param typeParamNames the class-level declared type parameter names
     * @return {@code true} if the signature is a bare type variable or array of one
     */
    private boolean isBareTypeVariable(String typeSignature, Set<String> typeParamNames) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        int kind = Signature.getTypeSignatureKind(typeSignature);
        if (kind == Signature.TYPE_VARIABLE_SIGNATURE) {
            return true;
        }
        if (kind == Signature.CLASS_TYPE_SIGNATURE) {
            return typeParamNames.contains(Signature.getSignatureSimpleName(typeSignature));
        }
        if (kind == Signature.ARRAY_TYPE_SIGNATURE) {
            return isBareTypeVariable(Signature.getElementType(typeSignature), typeParamNames);
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code typeSignature} is a parameterized type that contains
     * at least one type variable in its type arguments (recursively).
     *
     * @param typeSignature the JDT type signature to check
     * @param typeParamNames the class-level declared type parameter names
     * @return {@code true} if the signature is a parameterized type containing a type variable
     */
    private boolean containsTypeVariable(String typeSignature, Set<String> typeParamNames) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.CLASS_TYPE_SIGNATURE) {
            for (String typeArg : Signature.getTypeArguments(typeSignature)) {
                if (isBareTypeVariable(typeArg, typeParamNames) || containsTypeVariable(typeArg, typeParamNames)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns {@code true} if {@code typeSignature} contains a wildcard type parameter
     * ({@code ?}, {@code ? extends}, or {@code ? super}) anywhere in the type tree.
     *
     * @param typeSignature the JDT type signature to check
     * @return {@code true} if the signature contains a wildcard
     */
    private boolean containsWildcard(String typeSignature) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        int kind = Signature.getTypeSignatureKind(typeSignature);
        if (kind == Signature.ARRAY_TYPE_SIGNATURE) {
            return containsWildcard(Signature.getElementType(typeSignature));
        }
        if (kind == Signature.CLASS_TYPE_SIGNATURE) {
            for (String typeArg : Signature.getTypeArguments(typeSignature)) {
                char first = typeArg.charAt(0);
                if (first == Signature.C_STAR || first == Signature.C_EXTENDS || first == Signature.C_SUPER) {
                    return true;
                }
                if (containsWildcard(typeArg)) {
                    return true;
                }
            }
        }
        return false;
    }
}
