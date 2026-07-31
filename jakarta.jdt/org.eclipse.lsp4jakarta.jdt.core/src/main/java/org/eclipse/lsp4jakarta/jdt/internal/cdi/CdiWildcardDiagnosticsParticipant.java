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
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
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

                    // Rule: wildcard in field type
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

                    Set<String> typeParamNames = getTypeParameterNames(type);

                    // Rule: bare type variable or array-of-type-variable field type is always invalid.
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#producer_field
                    // "If a producer field type is a type variable or is an array type whose component
                    //  type is a type variable the container automatically detects the problem and
                    //  treats it as a definition error."
                    if (!typeParamNames.isEmpty() && isBareTypeVariable(typeSignature, typeParamNames)) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("InvalidProducerFieldWithBareTypeVariableType"),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidProducerFieldWithBareTypeVariableType,
                                                                 DiagnosticSeverity.Error));
                    }

                    // Rule: parameterized field type containing a type variable requires @Dependent scope.
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#producer_field
                    // "If the producer field type is a parameterized type with a type variable,
                    //  it must have scope @Dependent."
                    else if (!typeParamNames.isEmpty() && containsTypeVariable(typeSignature, typeParamNames)) {
                        String[] fieldAnnotationNames = Stream.of(field.getAnnotations()).map(IAnnotation::getElementName).toArray(String[]::new);
                        List<String> fieldScopes = DiagnosticUtils.getMatchedJavaElementNames(
                                                                                              type, fieldAnnotationNames,
                                                                                              Constants.SCOPE_FQ_NAMES.toArray(String[]::new));
                        boolean hasNonDependentScope = fieldScopes.stream().anyMatch(s -> !Constants.DEPENDENT_FQ_NAME.equals(s));
                        if (hasNonDependentScope) {
                            Range range = PositionUtils.toNameRange(field, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("InvalidProducerFieldWithTypeVariableAndNonDependentScope"),
                                                                     range,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     null,
                                                                     ErrorCode.InvalidProducerFieldWithTypeVariableAndNonDependentScope,
                                                                     DiagnosticSeverity.Error));
                        }
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
                    String returnTypeSignature = method.getReturnType();

                    // Rule: wildcard in return type
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

                    // Collect the type parameter names declared on the enclosing class
                    // (e.g. "T", "K", "V") so we can recognise them in return-type signatures.
                    Set<String> typeParamNames = getTypeParameterNames(type);

                    // Rule: bare type variable or array-of-type-variable return type is always invalid.
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#producer_method
                    // "If a producer method return type is a type variable or an array type whose
                    //  component type is a type variable the container automatically detects the
                    //  problem and treats it as a definition error."
                    if (!typeParamNames.isEmpty() && isBareTypeVariable(returnTypeSignature, typeParamNames)) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("InvalidProducerMethodWithBareTypeVariableReturnType"),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidProducerMethodWithBareTypeVariableReturnType,
                                                                 DiagnosticSeverity.Error));
                    }

                    // Rule: parameterized return type containing a type variable requires @Dependent scope.
                    // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#producer_method
                    // "If the producer method return type is a parameterized type with a type variable,
                    //  it must have scope @Dependent."
                    else if (!typeParamNames.isEmpty() && containsTypeVariable(returnTypeSignature, typeParamNames)) {
                        String[] methodAnnotationNames = Stream.of(method.getAnnotations()).map(IAnnotation::getElementName).toArray(String[]::new);
                        List<String> methodScopes = DiagnosticUtils.getMatchedJavaElementNames(
                                                                                               type, methodAnnotationNames,
                                                                                               Constants.SCOPE_FQ_NAMES.toArray(String[]::new));
                        boolean hasNonDependentScope = methodScopes.stream().anyMatch(s -> !Constants.DEPENDENT_FQ_NAME.equals(s));
                        if (hasNonDependentScope) {
                            Range range = PositionUtils.toNameRange(method, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("InvalidProducerMethodWithTypeVariableAndNonDependentScope"),
                                                                     range,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     null,
                                                                     ErrorCode.InvalidProducerMethodWithTypeVariableAndNonDependentScope,
                                                                     DiagnosticSeverity.Error));
                        }
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
     * Returns the set of type parameter names declared on {@code type}
     * (e.g. {@code {"T", "K", "V"}} for {@code class Foo<T, K, V>}).
     *
     * <p>This set is used to identify unresolved source-qualified type signatures that
     * refer to those type parameters (e.g. {@code QT;} for parameter {@code T}).
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
     * Checks if a type signature represents a bare type variable or an array whose
     * ultimate component type is a type variable.
     *
     * <p>In JDT source files, type parameters are represented as source-qualified references
     * ({@code QT;}) rather than resolved type-variable signatures ({@code TT;}). This method
     * resolves the ambiguity by comparing against the known type parameter names.
     *
     * <p>According to CDI 3.0 spec section 3.2:
     * "If a producer method return type is a type variable or an array type whose component
     * type is a type variable the container automatically detects the problem and treats it
     * as a definition error."
     *
     * @param typeSignature the JDT type signature to check
     * @param typeParamNames the set of declared type parameter names on the enclosing class
     * @return {@code true} if the signature is a bare type variable or array of one
     */
    private boolean isBareTypeVariable(String typeSignature, Set<String> typeParamNames) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        // Resolved type-variable signature (e.g. "TT;")
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.TYPE_VARIABLE_SIGNATURE) {
            return true;
        }
        // Unresolved source-qualified reference (e.g. "QT;") — check if name matches
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.CLASS_TYPE_SIGNATURE) {
            String simpleName = Signature.getSignatureSimpleName(typeSignature);
            if (typeParamNames.contains(simpleName)) {
                return true;
            }
        }
        // Array type: check element type recursively
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.ARRAY_TYPE_SIGNATURE) {
            return isBareTypeVariable(Signature.getElementType(typeSignature), typeParamNames);
        }
        return false;
    }

    /**
     * Checks if a parameterized type signature contains at least one type variable in
     * its type arguments (recursively).
     *
     * <p>In JDT source files, type parameter references appear as source-qualified signatures
     * ({@code QT;}) in type argument lists (e.g. {@code QList<QT;>;} for {@code List<T>}).
     * This method recognises both resolved ({@code TT;}) and unresolved ({@code QT;}) forms.
     *
     * <p>According to CDI 3.0 spec section 3.2:
     * "If the producer method return type is a parameterized type with a type variable,
     * it must have scope @Dependent."
     *
     * @param typeSignature the JDT type signature to check
     * @param typeParamNames the set of declared type parameter names on the enclosing class
     * @return {@code true} if the signature is a parameterized type containing a type variable
     */
    private boolean containsTypeVariable(String typeSignature, Set<String> typeParamNames) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.CLASS_TYPE_SIGNATURE) {
            for (String typeArg : Signature.getTypeArguments(typeSignature)) {
                if (isBareTypeVariable(typeArg, typeParamNames)) {
                    return true;
                }
                if (containsTypeVariable(typeArg, typeParamNames)) {
                    return true;
                }
            }
        }
        return false;
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