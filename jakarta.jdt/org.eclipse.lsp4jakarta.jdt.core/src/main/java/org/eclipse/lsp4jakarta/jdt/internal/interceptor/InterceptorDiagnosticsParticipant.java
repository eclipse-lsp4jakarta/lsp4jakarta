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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.commons.utils.InterModuleCommonUtils;
import org.eclipse.lsp4jakarta.jdt.core.ASTUtils;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Interceptor diagnostic participant that manages the use of @Interceptor annotation.
 */
public class InterceptorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(InterceptorDiagnosticsParticipant.class.getName());

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
            Map<String, Boolean> constructorInfo = new HashMap<>();
            constructorInfo.put("hasConstructor", false);
            constructorInfo.put("hasValidPublicNoArgsConstructor", false);
            int typeFlag = type.getFlags();
            boolean isInterceptorType = InterModuleCommonUtils.isInterceptorType(type);
            if (isInterceptorType) {
                Range range = PositionUtils.toNameRange(type, context.getUtils());
                if (Flags.isAbstract(typeFlag)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("InvalidInterceptorAbstractClass", type.getElementName()), range,
                                                             Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorAnnotationOnAbstractClass, DiagnosticSeverity.Error));
                }
                for (IMethod method : type.getMethods()) {
                    //Checks if method is a constructor and has valid no-args constructor
                    constructorInfo = DiagnosticUtils.hasValidNoArgsConstructor(method, constructorInfo);
                }
                // Conditions for checking missing public no-args constructor
                if (!constructorInfo.get("hasValidPublicNoArgsConstructor") && constructorInfo.get("hasConstructor")) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("ErrorMessageInterceptorNoArgConstructorMissing", type.getElementName()), range,
                                                             Constants.DIAGNOSTIC_SOURCE, ErrorCode.InvalidInterceptorNoArgsConstructorMissing, DiagnosticSeverity.Error));
                }
            }
        }

        List<MethodDeclaration> allMethodDeclarations = ASTUtils.getMethodDeclarations(unit);
        //Used to get the list of method declarations for interceptor methods that doesn't use proceed method
        List<MethodDeclaration> invocationContextMethodInvocations = allMethodDeclarations.stream().filter(mi -> {
            try {
                return isMatchedInvocationContextMethods(unit, mi);
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
     * Method used to traverse through Interceptor method declarations and invocations to find out if proceed method is invoked.
     *
     * @param unit
     * @param mi
     * @return
     * @throws JavaModelException
     */
    private boolean isMatchedInvocationContextMethods(ICompilationUnit unit, MethodDeclaration mi) throws JavaModelException {
        IType parentType = null;
        IMethodBinding binding = mi.resolveBinding();
        if (binding != null) {
            ITypeBinding declaringClass = binding.getDeclaringClass();
            if (declaringClass != null) {
                IJavaElement javaElement = declaringClass.getJavaElement();
                if (javaElement instanceof IType) {
                    parentType = (IType) javaElement;
                }
            }
        }
        if (InterModuleCommonUtils.isInterceptorReferencedType(parentType, unit)) {
            for (Object modifier : mi.modifiers()) {
                if (modifier instanceof Annotation) {
                    Annotation ann = (Annotation) modifier;
                    String annName = ann.getTypeName().getFullyQualifiedName();
                    if (isInterceptorAnnotation(parentType, annName) && !ASTUtils.containsMethodInvocation(mi,
                                                                                                           Constants.PROCEED, Constants.JAKARTA_INTERCEPTOR_INVOCATION_CONTEXT)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method used to check if annotation matches Interceptor method annotations fully qualified name
     *
     * @param type
     * @param annName
     * @return
     * @throws JavaModelException
     */
    private boolean isInterceptorAnnotation(IType type, String annName) throws JavaModelException {
        return Constants.INTERCEPTOR_METHODS.stream().anyMatch(annotation -> {
            try {
                return DiagnosticUtils.isMatchedJavaElement(type, annName, annotation);
            } catch (JavaModelException e) {
                LOGGER.log(Level.WARNING, "Unable to find matching annotation", e.getMessage());
                return false;
            }
        });
    }
}