/*******************************************************************************
* Copyright (c) 2021, 2025 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Himanshu Chotwani - initial API and implementation
*     Ananya Rao - Diagnostic Collection for multiple constructors annotated with inject
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.di;

import static org.eclipse.lsp4jakarta.jdt.internal.di.Constants.INJECT_FQ_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.Primitive;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Dependency injection diagnostics participant that manages the use of
 * the @Inject annotation.
 */
public class DependencyInjectionDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] alltypes;

        alltypes = unit.getAllTypes();
        for (IType type : alltypes) {
            IField[] allFields = type.getFields();
            for (IField field : allFields) {
                Range range = PositionUtils.toNameRange(field,
                                                        context.getUtils());
                if (containsAnnotation(type, field.getAnnotations(), INJECT_FQ_NAME)) {
                    if (Flags.isFinal(field.getFlags())) {
                        String msg = Messages.getMessage("InjectNoFinalField");

                        diagnostics.add(
                                        context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.InvalidInjectAnnotationOnFinalField,
                                                                 DiagnosticSeverity.Error));
                    }
                    if (hasNonStaticInnerClass(type, Signature.toString(field.getTypeSignature()))) {
                        String msg = Messages.getMessage("InjectNonStaticInnerClass");
                        diagnostics.add(
                                        context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.InvalidInjectAnnotationOnNonStaticInnerClass,
                                                                 DiagnosticSeverity.Error));
                    }
                }

            }

            List<IMethod> injectedConstructors = new ArrayList<IMethod>();
            IMethod[] allMethods = type.getMethods();
            for (IMethod method : allMethods) {

                Range range = PositionUtils.toNameRange(method, context.getUtils());
                int methodFlag = method.getFlags();
                if (containsAnnotation(type, method.getAnnotations(), INJECT_FQ_NAME)) {
                    if (DiagnosticUtils.isConstructorMethod(method))
                        injectedConstructors.add(method);
                    if (Flags.isFinal(methodFlag)) {
                        String msg = Messages.getMessage("InjectNoFinalMethod");

                        diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.InvalidInjectAnnotationOnFinalMethod,
                                                                 DiagnosticSeverity.Error));
                    }

                    if (Flags.isAbstract(methodFlag)) {
                        String msg = Messages.getMessage("InjectNoAbstractMethod");
                        diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.InvalidInjectAnnotationOnAbstractMethod,
                                                                 DiagnosticSeverity.Error));
                    }

                    if (Flags.isStatic(methodFlag)) {
                        String msg = Messages.getMessage("InjectNoStaticMethod");
                        diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.InvalidInjectAnnotationOnStaticMethod,
                                                                 DiagnosticSeverity.Error));
                    }

                    if (method.getTypeParameters().length != 0) {
                        String msg = Messages.getMessage("InjectNoGenericMethod");
                        diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.InvalidInjectAnnotationOnGenericMethod,
                                                                 DiagnosticSeverity.Error));
                    }
                    String[] paramTypes = method.getParameterTypes();
                    for (String paramType : paramTypes) {
                        if (hasNonStaticInnerClass(type, Signature.toString(paramType))) {
                            String msg = Messages.getMessage("InjectNonStaticInnerClass");
                            diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                                     ErrorCode.InvalidInjectAnnotationOnNonStaticInnerClass,
                                                                     DiagnosticSeverity.Error));
                        }
                    }

                }
            }

            // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#declaring_bean_constructor:
            // "If a bean class has more than one constructor annotated @Inject, the container automatically
            // detects the problem and treats it as a definition error."
            if (injectedConstructors.size() > 1) {
                String msg = Messages.getMessage("InjectMoreThanOneConstructor");
                for (IMethod method : injectedConstructors) {
                    Range range = PositionUtils.toNameRange(method,
                                                            context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                             ErrorCode.InvalidInjectAnnotationOnMultipleConstructors,
                                                             DiagnosticSeverity.Error));
                }
            }

            // https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0.html#declaring_bean_constructor:
            // "A bean constructor may have any number of parameters. All parameters of a bean constructor
            // are injection points."
            for (IMethod constructor : injectedConstructors) {
                if (constructor.getNumberOfParameters() > 0) {
                    ILocalVariable[] params = constructor.getParameters();
                    for (int i = 0; i < params.length; i++) {
                        ILocalVariable param = params[i];
                        getInjectionPointDiagnostics(diagnostics, context, uri, param);
                    }
                }
            }
        }

        return diagnostics;
    }

    private boolean containsAnnotation(IType type, IAnnotation[] annotations, String annotationFQName) {
        return Stream.of(annotations).anyMatch(annotation -> {
            try {
                return DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), annotationFQName);
            } catch (JavaModelException e) {
                JakartaCorePlugin.logException("Cannot validate annotations", e);
                return false;
            }
        });
    }

    /**
     * hasNonStaticInnerClass
     * This will check whether the parent class contains any nested class that has no static field matching the field’s type.
     *
     * @param outerType
     * @param injectedTypeName
     * @return
     * @throws JavaModelException
     */
    private boolean hasNonStaticInnerClass(IType outerType, String injectedTypeName) throws JavaModelException {

        for (IType inner : outerType.getTypes()) {
            // convert JVM binary name to Source code reference to the nested class
            String innerFqName = inner.getFullyQualifiedName().replace('$', '.');
            if (DiagnosticUtils.isMatchedNestedClass(outerType, injectedTypeName, innerFqName)) {
                return !Flags.isStatic(inner.getFlags());
            }
        }
        return false;
    }

    /**
     * Obtains the injections point diagnostics for the given local variable.
     *
     * @param diagnostics The list of diagnostics to update.
     * @param context The diagnostics context associated with this call.
     * @param uri The URI associated with the file being processed.
     * @param variable The ILocalVariable object being processed.
     * @return
     * @throws JavaModelException
     */
    private void getInjectionPointDiagnostics(List<Diagnostic> diagnostics, JavaDiagnosticsContext context, String uri, ILocalVariable variable) {
        try {
            // Note: Although, these checks apply to all managed bean parameters that are injections points,
            // some of these checks may not apply to other non-managed beans that are injectable.
            // Further consideration is required.
            Range range = PositionUtils.toNameRange(variable, context.getUtils());
            IType variableType = ManagedBean.variableSignatureToType(variable);

            // Check if the type is a primitive.
            if (Primitive.isPrimitive(variable)) {
                String msg = Messages.getMessage("InjectionPointInvalidPrimitiveBean");
                diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InjectionPointInvalidPrimitiveBean,
                                                         DiagnosticSeverity.Warning));

                // Primitive types are special. The checks that follow do not apply to them and/or may cause errors.
                return;
            }

            // Check if the type is an inner class.
            if (ManagedBean.isInnerClass(variableType)) {
                String msg = Messages.getMessage("InjectionPointInvalidInnerClassBean");
                diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InjectionPointInvalidInnerClassBean,
                                                         DiagnosticSeverity.Warning));
            }

            // Check if the type is an abstract class or is not annotated with @Decorator.
            if (ManagedBean.isAbstractClass(variableType) && !ManagedBean.isAnnotatedClass(variableType, ManagedBean.DECORATOR_ANNOTATION)) {
                String msg = Messages.getMessage("InjectionPointInvalidAbstractClassBean");
                diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InjectionPointInvalidAbstractClassBean,
                                                         DiagnosticSeverity.Warning));
            }

            // Check if the type implements jakarta.enterprise.inject.spi.Extension
            if (ManagedBean.implementsExtends(variableType, ManagedBean.EXTENSION_SERVICE_IFACE)) {
                String msg = Messages.getMessage("InjectionPointInvalidExtensionProviderBean");
                diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InjectionPointInvalidExtensionProviderBean,
                                                         DiagnosticSeverity.Warning));
            }

            // Check if the type is annotated @Vetoed or in a package annotated @Vetoed.
            if (ManagedBean.isAnnotatedClass(variableType, ManagedBean.VETOED_ANNOTATION) || ManagedBean.isPackageMetadataAnnotated(variableType, ManagedBean.VETOED_ANNOTATION)) {
                String msg = Messages.getMessage("InjectionPointInvalidVetoedClassBean");
                diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InjectionPointInvalidVetoedClassBean,
                                                         DiagnosticSeverity.Warning));
            }

            // Check if the type does not have a constructor with no parameters or the class declares a constructor that is not annotated @Inject.
            if (!ManagedBean.containsValidConstructor(variableType)) {
                String msg = Messages.getMessage("InjectionPointInvalidConstructorBean");
                diagnostics.add(context.createDiagnostic(uri, msg, range, Constants.DIAGNOSTIC_SOURCE,
                                                         ErrorCode.InjectionPointInvalidConstructorBean,
                                                         DiagnosticSeverity.Warning));
            }
        } catch (JavaModelException jme) {
            JakartaCorePlugin.logException("Cannot obtain injection point diagnostics for variable: " + variable + " in file: " + uri, jme);
        }
    }
}
