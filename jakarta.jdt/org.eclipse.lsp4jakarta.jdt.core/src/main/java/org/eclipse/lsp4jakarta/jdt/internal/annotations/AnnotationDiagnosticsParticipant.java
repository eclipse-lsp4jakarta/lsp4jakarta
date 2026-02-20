/*******************************************************************************
* Copyright (c) 2021, 2026 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple.Two;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

/**
 *
 * Annotations diagnostic participant that manages the use of annotations.
 *
 * @see https://jakarta.ee/specifications/annotations/2.0/annotations-spec-2.0.html#annotations
 *
 */
public class AnnotationDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit != null) {
            ArrayList<Tuple.Two<IAnnotation, IAnnotatable>> annotatables = new ArrayList<Two<IAnnotation, IAnnotatable>>();
            String[] validAnnotations = { Constants.GENERATED_FQ_NAME };
            String[] validTypeAnnotations = { Constants.GENERATED_FQ_NAME,
                                              Constants.RESOURCE_FQ_NAME,
                                              Constants.RESOURCES_FQ_NAME,
                                              Constants.PRIORITY_FQ_NAME };
            String[] validFieldAnnotations = { Constants.GENERATED_FQ_NAME,
                                               Constants.RESOURCE_FQ_NAME,
                                               Constants.RESOURCES_FQ_NAME };
            String[] validMethodAnnotations = { Constants.GENERATED_FQ_NAME,
                                                Constants.POST_CONSTRUCT_FQ_NAME, Constants.PRE_DESTROY_FQ_NAME,
                                                Constants.RESOURCE_FQ_NAME };
            String[] validMethodParamAnnotations = { Constants.GENERATED_FQ_NAME, Constants.PRIORITY_FQ_NAME };

            IPackageDeclaration[] packages = unit.getPackageDeclarations();
            for (IPackageDeclaration p : packages) {
                IAnnotation[] annotations = p.getAnnotations();
                for (IAnnotation annotation : annotations) {
                    if (isValidAnnotation(annotation.getElementName(), validAnnotations))
                        annotatables.add(new Tuple.Two<>(annotation, p));
                }
            }

            IType[] types = unit.getAllTypes();
            for (IType type : types) {
                // Type
                IAnnotation[] annotations = type.getAnnotations();
                for (IAnnotation annotation : annotations) {
                    if (isValidAnnotation(annotation.getElementName(), validTypeAnnotations))
                        annotatables.add(new Tuple.Two<>(annotation, type));
                }
                // Method
                IMethod[] methods = type.getMethods();
                for (IMethod method : methods) {
                    annotations = method.getAnnotations();
                    for (IAnnotation annotation : annotations) {
                        if (isValidAnnotation(annotation.getElementName(), validMethodAnnotations))
                            annotatables.add(new Tuple.Two<>(annotation, method));
                    }
                    // method parameters
                    ILocalVariable[] parameters = method.getParameters();
                    for (ILocalVariable parameter : parameters) {
                        annotations = parameter.getAnnotations();
                        for (IAnnotation annotation : annotations) {
                            if (isValidAnnotation(annotation.getElementName(), validMethodParamAnnotations))
                                annotatables.add(new Tuple.Two<>(annotation, parameter));
                        }
                    }
                }
                // Field
                IField[] fields = type.getFields();
                for (IField field : fields) {
                    annotations = field.getAnnotations();
                    for (IAnnotation annotation : annotations) {
                        if (isValidAnnotation(annotation.getElementName(), validFieldAnnotations))
                            annotatables.add(new Tuple.Two<>(annotation, field));
                    }
                }
            }

            for (Tuple.Two<IAnnotation, IAnnotatable> annotatable : annotatables) {
                IAnnotation annotation = annotatable.getFirst();
                IAnnotatable element = annotatable.getSecond();

                // process Types? (class declarations)
                if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.GENERATED_FQ_NAME)) {
                    for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
                        // If date element exists and is non-empty, it must follow ISO 8601 format.
                        if (pair.getMemberName().equals("date")) {
                            if (pair.getValue() instanceof String) {
                                String date = (String) pair.getValue();
                                if (!date.equals("")) {
                                    if (!Pattern.matches(Constants.ISO_8601_REGEX, date)) {

                                        Range annotationRange = PositionUtils.toNameRange(annotation,
                                                                                          context.getUtils());
                                        String diagnosticMessage = Messages.getMessage(
                                                                                       "AnnotationMustDefineAttributeFollowing8601", "@Generated", "date");
                                        diagnostics.add(
                                                        context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                                 ErrorCode.InvalidDateFormat,
                                                                                 DiagnosticSeverity.Error));
                                    }
                                }
                            }
                        }
                    }
                } else if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.RESOURCE_FQ_NAME)) {
                    if (element instanceof IType) {
                        IType type = (IType) element;
                        if (type.getElementType() == IJavaElement.TYPE && ((IType) type).isClass()) {
                            Range annotationRange = PositionUtils.toNameRange(annotation, context.getUtils());
                            Boolean nameEmpty = true;
                            Boolean typeEmpty = true;
                            for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
                                if (pair.getMemberName().equals("name")) {
                                    nameEmpty = false;
                                }
                                if (pair.getMemberName().equals("type")) {
                                    typeEmpty = false;
                                }
                            }
                            String diagnosticMessage;
                            if (nameEmpty) {
                                diagnosticMessage = Messages.getMessage("AnnotationMustDefineAttribute",
                                                                        "@Resource", "name");
                                diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                                         Constants.DIAGNOSTIC_SOURCE,
                                                                         ErrorCode.MissingResourceNameAttribute,
                                                                         DiagnosticSeverity.Error));
                            }

                            if (typeEmpty) {
                                diagnosticMessage = Messages.getMessage("AnnotationMustDefineAttribute",
                                                                        "@Resource", "type");
                                diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                                         Constants.DIAGNOSTIC_SOURCE,
                                                                         ErrorCode.MissingResourceTypeAttribute,
                                                                         DiagnosticSeverity.Error));
                            }
                        }
                    } else if (element instanceof IMethod) {
                        IMethod method = (IMethod) element;
                        Range annotationRange = PositionUtils.toNameRange(annotation, context.getUtils());
                        validateResourceMethods(method, uri, annotationRange, context, diagnostics);
                    }

                } else if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.RESOURCES_FQ_NAME)) {
                    if (element instanceof IType) {
                        for (IMemberValuePair internalAnnotation : annotation.getMemberValuePairs()) {
                            Object[] valuePairs = (Object[]) internalAnnotation.getValue();
                            String diagnosticMessage;
                            Range annotationRange = null;
                            if (valuePairs.length == 0) {
                                annotationRange = PositionUtils.toNameRange(annotation, context.getUtils());
                                diagnosticMessage = Messages.getMessage("ResourcesAnnotationMustDefineResourceAnnotation",
                                                                        "@Resources", "@Resource");
                                diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                                         Constants.DIAGNOSTIC_SOURCE,
                                                                         ErrorCode.MissingResourceAnnotation,
                                                                         DiagnosticSeverity.Error));
                            }
                            int objKind = internalAnnotation.getValueKind();
                            for (Object childAnnotationObj : valuePairs) {
                                if (objKind == IMemberValuePair.K_ANNOTATION) {
                                    IAnnotation childAnnotation = (IAnnotation) childAnnotationObj;
                                    if (DiagnosticUtils.isMatchedAnnotation(unit, childAnnotation,
                                                                            Constants.RESOURCE_FQ_NAME)) {
                                        if (element instanceof IType) {
                                            IType type = (IType) element;
                                            if (type.getElementType() == IJavaElement.TYPE
                                                && ((IType) type).isClass()) {
                                                annotationRange = PositionUtils.toNameRange(childAnnotation,
                                                                                            context.getUtils());
                                                Boolean nameEmpty = true;
                                                Boolean typeEmpty = true;
                                                for (IMemberValuePair pair : childAnnotation.getMemberValuePairs()) {
                                                    if (pair.getMemberName().equals("name")) {
                                                        nameEmpty = false;
                                                    }
                                                    if (pair.getMemberName().equals("type")) {
                                                        typeEmpty = false;
                                                    }
                                                }
                                                if (nameEmpty) {
                                                    diagnosticMessage = Messages.getMessage(
                                                                                            "AnnotationMustDefineAttribute",
                                                                                            "@Resource", "name");
                                                    diagnostics.add(context.createDiagnostic(uri, diagnosticMessage,
                                                                                             annotationRange,
                                                                                             Constants.DIAGNOSTIC_SOURCE,
                                                                                             ErrorCode.MissingResourceNameAttribute,
                                                                                             DiagnosticSeverity.Error));
                                                }

                                                if (typeEmpty) {
                                                    diagnosticMessage = Messages.getMessage(
                                                                                            "AnnotationMustDefineAttribute",
                                                                                            "@Resource", "type");
                                                    diagnostics.add(context.createDiagnostic(uri, diagnosticMessage,
                                                                                             annotationRange,
                                                                                             Constants.DIAGNOSTIC_SOURCE,
                                                                                             ErrorCode.MissingResourceTypeAttribute,
                                                                                             DiagnosticSeverity.Error));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.PRIORITY_FQ_NAME)) {
                    validatePriority(context, uri, diagnostics, annotation, element);
                }

                // process methods now?
                if (DiagnosticUtils.isMatchedAnnotation(unit, annotation, Constants.POST_CONSTRUCT_FQ_NAME)) {
                    if (element instanceof IMethod) {
                        IMethod method = (IMethod) element;
                        Range methodRange = PositionUtils.toNameRange(method, context.getUtils());
                        List<String> checkedExceptions = getCheckedExceptionsDeclared(method);
                        if (checkedExceptions.size() > 0) {
                            String diagnosticMessage = Messages.getMessage(
                                                                           "MethodMustNotThrow", "@PostConstruct");
                            diagnostics.add(
                                            context.createDiagnostic(uri, diagnosticMessage, methodRange,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     (JsonArray) (new Gson().toJsonTree(checkedExceptions)),
                                                                     ErrorCode.PostConstructException,
                                                                     DiagnosticSeverity.Error));
                        }

                        if (method.getNumberOfParameters() != 0) {

                            String diagnosticMessage = Messages.getMessage("MethodMustNotHaveParameters",
                                                                           "@PostConstruct");
                            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, methodRange,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     ErrorCode.PostConstructParams,
                                                                     DiagnosticSeverity.Error));
                        }

                        if (!method.getReturnType().equals("V")) {
                            String diagnosticMessage = Messages.getMessage("MethodMustBeVoid",
                                                                           "@PostConstruct");
                            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, methodRange,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     ErrorCode.PostConstructReturnType,
                                                                     DiagnosticSeverity.Error));
                        }

                    }
                } else if (DiagnosticUtils.isMatchedAnnotation(unit, annotation,
                                                               Constants.PRE_DESTROY_FQ_NAME)) {
                    if (element instanceof IMethod) {
                        IMethod method = (IMethod) element;
                        Range methodRange = PositionUtils.toNameRange(method, context.getUtils());
                        List<String> checkedExceptions = getCheckedExceptionsDeclared(method);
                        if (checkedExceptions.size() > 0) {
                            String diagnosticMessage = Messages.getMessage(
                                                                           "MethodMustNotThrow", "@PreDestroy");
                            diagnostics.add(
                                            context.createDiagnostic(uri, diagnosticMessage, methodRange,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     (JsonArray) (new Gson().toJsonTree(checkedExceptions)),
                                                                     ErrorCode.PreDestroyException,
                                                                     DiagnosticSeverity.Error));
                        }
                        if (method.getNumberOfParameters() != 0) {
                            String diagnosticMessage = Messages.getMessage("MethodMustNotHaveParameters",
                                                                           "@PreDestroy");
                            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, methodRange,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     ErrorCode.PreDestroyParams,
                                                                     DiagnosticSeverity.Error));
                        }

                        if (Flags.isStatic(method.getFlags())) {
                            String diagnosticMessage = Messages.getMessage("MethodMustNotBeStatic",
                                                                           "@PreDestroy");
                            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, methodRange,
                                                                     Constants.DIAGNOSTIC_SOURCE,
                                                                     ErrorCode.PreDestroyStatic,
                                                                     DiagnosticSeverity.Error));
                        }
                    }
                }
            }
        }

        return diagnostics;
    }

    /**
     * validatePriority
     * This method validates priority values to check whether any negative values have been applied.
     *
     * @param context
     * @param uri
     * @param diagnostics
     * @param annotation
     * @param element
     * @throws JavaModelException
     */
    private void validatePriority(JavaDiagnosticsContext context, String uri, List<Diagnostic> diagnostics,
                                  IAnnotation annotation, IAnnotatable element) throws JavaModelException {

        // Priority is valid only for elements that are either classes or method parameters.
        if (element instanceof IType || element instanceof ILocalVariable) {
            for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
                if ("value".equals(pair.getMemberName()) && pair.getValue() instanceof Integer) {
                    int priority = (Integer) pair.getValue();
                    if (priority < 0) {
                        Range annotationRange = PositionUtils.toNameRange(annotation, context.getUtils());
                        String diagnosticMessage = Messages.getMessage(
                                                                       "PriorityShouldBeNonNegative");
                        diagnostics.add(context.createDiagnostic(uri, diagnosticMessage,
                                                                 annotationRange,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 ErrorCode.PriorityShouldBeNonNegative,
                                                                 DiagnosticSeverity.Warning));
                    }
                }
            }
        }
    }

    /**
     * getCheckedExceptionsDeclared
     * This method scans the exception signatures to identify if any checked exceptions are declared.
     *
     * @param method
     * @return
     * @throws JavaModelException
     * @throws CoreException
     */
    private List<String> getCheckedExceptionsDeclared(IMethod method) throws JavaModelException, CoreException {

        IType parentType = method.getDeclaringType();
        List<String> checkedExceptions = new ArrayList<String>();
        String[] exceptionSignatures = method.getExceptionTypes();
        for (String sig : exceptionSignatures) {
            IType exceptionType = ManagedBean.getChildITypeByName(parentType, Signature.toString(sig));
            /*
             * A checked exception is any class that extends java.lang.Exception but not
             * java.lang.RuntimeException.
             * An unchecked exception is any class that extends java.lang.RuntimeException
             * or java.lang.Error.
             */
            if (extendsException(exceptionType)) {
                if (notExtendsRunTimeException(exceptionType)) {
                    checkedExceptions.add(exceptionType.getFullyQualifiedName());
                }
            }
        }
        return checkedExceptions;
    }

    /**
     * validateResourceMethods
     * This method is responsible for finding diagnostics in methods annotated with @Resource.
     *
     * @param m
     * @param uri
     * @param annotationRange
     * @param context
     * @param diagnostics
     * @throws JavaModelException
     */
    public static void validateResourceMethods(IMethod m, String uri, Range annotationRange,
                                               JavaDiagnosticsContext context, List<Diagnostic> diagnostics) throws JavaModelException {
        String methodName = m.getElementName();
        if (!methodName.startsWith("set")) {

            String diagnosticMessage = Messages.getMessage("AnnotationNameMustStartWithSet",
                                                           "@Resource", methodName);
            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                     Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.ResourceNameMustStartWithSet,
                                                     DiagnosticSeverity.Error));
        }
        if (!"V".equalsIgnoreCase(m.getReturnType())) {
            String diagnosticMessage = Messages.getMessage("AnnotationReturnTypeMustBeVoid",
                                                           "@Resource", methodName);
            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                     Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.ResourceReturnTypeMustBeVoid,
                                                     DiagnosticSeverity.Error));
        }
        if (m.getParameterTypes().length != 1) {
            String diagnosticMessage = Messages.getMessage("AnnotationMustDeclareExactlyOneParam",
                                                           "@Resource", methodName);
            diagnostics.add(context.createDiagnostic(uri, diagnosticMessage, annotationRange,
                                                     Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.ResourceMustDeclareExactlyOneParam,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * notExtendsRunTimeException
     *
     * @param exceptionType The root type of which the super-types are checked.
     * @return true if RuntimeException is not the superType of the given exception type.
     * @throws CoreException
     */
    private boolean notExtendsRunTimeException(IType exceptionType) throws CoreException {
        return TypeHierarchyUtils.doesITypeHaveSuperType(exceptionType, Constants.RUNTIME_EXCEPTION) == -1;
    }

    /**
     * extendsException
     *
     * @param exceptionType The root type of which the super-types are checked.
     * @return true if Exception is the superType of the given exception type.
     * @throws CoreException
     */
    private boolean extendsException(IType exceptionType) throws CoreException {
        return TypeHierarchyUtils.doesITypeHaveSuperType(exceptionType, Constants.EXCEPTION) == 1;
    }

    /**
     * Returns true if the input annotation is valid. False, otherwise.
     *
     * @param annotationName The annotation to validate.
     * @param validAnnotations The list of valid annotations.
     *
     * @return True if the input annotation is valid. False, otherwise.
     */
    private static boolean isValidAnnotation(String annotationName, String[] validAnnotations) {
        for (String fqName : validAnnotations) {
            if (fqName.endsWith(annotationName)) {
                return true;
            }
        }
        return false;
    }

}
