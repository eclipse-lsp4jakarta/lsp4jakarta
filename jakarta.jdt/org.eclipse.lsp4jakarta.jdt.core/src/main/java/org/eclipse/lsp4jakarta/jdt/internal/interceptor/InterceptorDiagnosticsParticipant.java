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
*     IBM Corporation, Archana Iyer R - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.commons.utils.InterModuleCommonUtils;
import org.eclipse.lsp4jakarta.jdt.core.ASTUtils;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.helpers.ConstructorInfoDiagnosticHelper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 * Interceptor diagnostic participant that manages the use of @Interceptor annotation.
 */
public class InterceptorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            int typeFlag = type.getFlags();
            boolean isInterceptorType = InterModuleCommonUtils.isInterceptorReferencedType(type, unit);
            if (isInterceptorType) {
                Range range = PositionUtils.toNameRange(type, context.getUtils());
                // Check 1: Validate if class is abstract
                if (Flags.isAbstract(typeFlag)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("InvalidInterceptorAbstractClass", type.getElementName()), range,
                                                             Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorAnnotationOnAbstractClass, DiagnosticSeverity.Error));
                } else {
                    // Get constructor information
                    ConstructorInfoDiagnosticHelper constructorInfo = ConstructorInfoDiagnosticHelper.getConstructorInfo(type);

                    // Conditions for checking missing public no-args constructor
                    if (constructorInfo.hasConstructor() && !constructorInfo.hasValidPublicNoArgsConstructor()) {
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("ErrorMessageInterceptorNoArgConstructorMissing",
                                                                                     type.getElementName()),
                                                                 range, Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorNoArgsConstructorMissing,
                                                                 DiagnosticSeverity.Error));
                    }
                }
                for (IMethod method : type.getMethods()) {
                    // Validate interceptor method modifiers
                    validateInterceptorMethodModifiers(context, uri, diagnostics, type, method);
                }
            }
        }
        List<MethodDeclaration> allMethodDeclarations = ASTUtils.getMethodDeclarations(unit);
        //Used to get the list of method declarations for interceptor methods that doesn't use proceed method
        List<MethodDeclaration> invocationContextMethodInvocations = allMethodDeclarations.stream().filter(methodDecl -> {
            try {
                return isMatchedInvocationContextMethods(unit, methodDecl);
            } catch (JavaModelException e) {
                return false;
            }
        }).collect(Collectors.toList());
        for (MethodDeclaration m : invocationContextMethodInvocations) {
            Range range = JDTUtils.toRange(unit, m.getName().getStartPosition(), m.getName().getLength());
            diagnostics.add(context.createDiagnostic(uri, Messages.getMessage("InvalidInterceptorMethodsProceedMissing"),
                                                     range, Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorMethodsProceedMissing,
                                                     DiagnosticSeverity.Error));
        }
        return diagnostics;
    }

    /**
     * Validates whether an interceptor method properly invokes the proceed() method.
     *
     * <p>This method checks if a given method declaration is an interceptor method (annotated with
     * one of the Jakarta Interceptors lifecycle annotations) and verifies that it contains an
     * invocation of the {@code proceed()} method on an {@code InvocationContext} object, as required
     * by the Jakarta Interceptors specification.</p>
     *
     * @param unit
     * @param methodDecl
     * @return {@code true} if the method is an interceptor method that does NOT invoke the
     *         {@code proceed()} method (indicating a validation error); {@code false} otherwise
     * @throws JavaModelException
     */
    private boolean isMatchedInvocationContextMethods(ICompilationUnit unit, MethodDeclaration methodDecl) throws JavaModelException {
        IType targetClass = null;
        IMethodBinding binding = methodDecl.resolveBinding();
        if (binding != null) {
            ITypeBinding declaringClass = binding.getDeclaringClass();
            if (declaringClass != null) {
                IJavaElement javaElement = declaringClass.getJavaElement();
                if (javaElement instanceof IType) {
                    targetClass = (IType) javaElement;
                }
            }
        }
        if (InterModuleCommonUtils.isInterceptorReferencedType(targetClass, unit)) {
            for (Object modifier : methodDecl.modifiers()) {
                if (modifier instanceof Annotation) {
                    Annotation annotation = (Annotation) modifier;
                    String annotationName = annotation.getTypeName().getFullyQualifiedName();
                    String[] interceptorMethods = Constants.INTERCEPTOR_METHODS.toArray(String[]::new);
                    //Verifies that the method does not invoke {@code proceed()}
                    if (DiagnosticUtils.getMatchedJavaElementName(targetClass, annotationName, interceptorMethods) != null
                        && !ASTUtils.containsMethodInvocation(methodDecl, Constants.PROCEED, Constants.JAKARTA_INTERCEPTOR_INVOCATION_CONTEXT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Validates interceptor method modifiers and creates diagnostics for invalid modifiers.
     * Checks for final, abstract, and static modifiers on interceptor methods.
     *
     * @param context the diagnostics context
     * @param uri the file URI
     * @param diagnostics the list to add diagnostics to
     * @param type the declaring type
     * @param method the method to validate
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private void validateInterceptorMethodModifiers(JavaDiagnosticsContext context, String uri,
                                                    List<Diagnostic> diagnostics, IType type,
                                                    IMethod method) throws JavaModelException {
        List<String> interceptorAnnotations = getInterceptorMethodAnnotations(type, method);

        if (interceptorAnnotations.isEmpty()) {
            return;
        }

        int methodFlag = method.getFlags();
        String annotationNames = getSimpleAnnotationNames(interceptorAnnotations);
        JsonArray annotationData = (JsonArray) new Gson().toJsonTree(interceptorAnnotations);
        // Check for final modifier
        if (Flags.isFinal(methodFlag)) {
            createMethodModifierDiagnostic(context, uri, diagnostics, method, annotationNames,
                                           annotationData, "InvalidInterceptorMethodAnnotationFinalMethod",
                                           ErrorCode.InvalidInterceptorMethodAnnotationOnFinalMethod,
                                           DiagnosticSeverity.Error);
        }
        // Check for abstract modifier
        if (Flags.isAbstract(methodFlag)) {
            createMethodModifierDiagnostic(context, uri, diagnostics, method, annotationNames,
                                           annotationData, "InvalidInterceptorMethodAnnotationAbstractMethod",
                                           ErrorCode.InvalidInterceptorMethodAnnotationOnAbstractMethod,
                                           DiagnosticSeverity.Error);
        }
        // Check for static modifier
        if (Flags.isStatic(methodFlag)) {
            validateStaticModifier(context, uri, diagnostics, type, method,
                                   annotationNames, annotationData, interceptorAnnotations);
        }
    }

    /**
     * Gets the list of interceptor method annotations present on a method.
     *
     * @param type the declaring type
     * @param method the method to check
     * @return list of matched interceptor annotation FQ names
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private List<String> getInterceptorMethodAnnotations(IType type, IMethod method) throws JavaModelException {
        return DiagnosticUtils.getMatchedJavaElementNames(type,
                                                          Stream.of(method.getAnnotations()).map(IAnnotation::getElementName).toArray(String[]::new),
                                                          Constants.INTERCEPTOR_METHODS.toArray(String[]::new));
    }

    /**
     * Validates static modifier on interceptor methods.
     * Lifecycle callback methods get a warning, other interceptor methods get an error.
     *
     * @param context the diagnostics context
     * @param uri the file URI
     * @param diagnostics the list to add diagnostics to
     * @param type the declaring type
     * @param method the method being validated
     * @param annotationNames the simple annotation names
     * @param annotationData the annotation data for the diagnostic
     * @param interceptorAnnotations the full list of interceptor annotations
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private void validateStaticModifier(JavaDiagnosticsContext context, String uri,
                                        List<Diagnostic> diagnostics, IType type, IMethod method, String annotationNames,
                                        JsonArray annotationData, List<String> interceptorAnnotations) throws JavaModelException {
        boolean isLifecycleCallback = !DiagnosticUtils.getMatchedJavaElementNames(type,
                                                                                  Stream.of(method.getAnnotations()).map(IAnnotation::getElementName).toArray(String[]::new),
                                                                                  Constants.LIFECYCLE_CALLBACK_INTERCEPTOR_METHODS).isEmpty();

        if (isLifecycleCallback) {
            createMethodModifierDiagnostic(context, uri, diagnostics, method, annotationNames,
                                           annotationData, "InvalidLifecycleCallbackMethodAnnotationStaticMethod",
                                           ErrorCode.InvalidInterceptorMethodAnnotationOnStaticMethod,
                                           DiagnosticSeverity.Warning);
        } else {
            createMethodModifierDiagnostic(context, uri, diagnostics, method, annotationNames,
                                           annotationData, "InvalidInterceptorMethodAnnotationStaticMethod",
                                           ErrorCode.InvalidInterceptorMethodAnnotationOnStaticMethod,
                                           DiagnosticSeverity.Error);
        }
    }

    /**
     * Creates a diagnostic for an invalid method modifier.
     *
     * @param context the diagnostics context
     * @param uri the file URI
     * @param diagnostics the list to add diagnostics to
     * @param method the method being validated
     * @param annotationNames the simple annotation names for the message
     * @param annotationData the annotation data
     * @param messageKey the message key
     * @param errorCode the error code
     * @param severity the diagnostic severity
     * @throws JavaModelException
     */
    private void createMethodModifierDiagnostic(JavaDiagnosticsContext context, String uri,
                                                List<Diagnostic> diagnostics, IMethod method,
                                                String annotationNames, JsonArray annotationData,
                                                String messageKey, ErrorCode errorCode,
                                                DiagnosticSeverity severity) throws JavaModelException {
        String message = Messages.getMessage(messageKey, annotationNames);
        Range range = PositionUtils.toNameRange(method, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                 annotationData, errorCode, severity));
    }

    /**
     * Converts a list of fully qualified annotation names to simple names.
     *
     * @param annotations the list of FQ annotation names
     * @return comma-separated string of simple annotation names
     * @throws JavaModelException if there's an error accessing the Java model
     */
    private String getSimpleAnnotationNames(List<String> annotations) throws JavaModelException {
        return annotations.stream().map(DiagnosticUtils::getSimpleName).distinct().collect(Collectors.joining(", "));
    }
}