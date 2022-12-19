/*******************************************************************************
* Copyright (c) 2021, 2022 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.jdt.core.annotations;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple;
import org.eclipse.lsp4j.jsonrpc.messages.Tuple.Two;
import org.eclipse.lsp4jakarta.jdt.core.AbstractDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.Messages;

/**
 * 
 * jararta.annotation Diagnostics
 * 
 * <li>Diagnostic 1: @Generated 'date' attribute does not follow ISO 8601.</li>
 * <li>Diagnostic 2: @Resource 'name' attribute missing (when annotation is used
 * on a class).</li>
 * <li>Diagnostic 3: @Resource 'type' attribute missing (when annotation is used
 * on a class).</li>
 * <li>Diagnostic 4: @PostConstruct method has parameters.</li>
 * <li>Diagnostic 5: @PostConstruct method is not void.</li>
 * <li>Diagnostic 6: @PostConstruct method throws checked exception(s).</li>
 * <li>Diagnostic 7: @PreDestroy method has parameters.</li>
 * <li>Diagnostic 8: @PreDestroy method is static.</li>
 * <li>Diagnostic 9: @PreDestroy method throws checked exception(s).</li>
 * 
 * @see https://jakarta.ee/specifications/annotations/2.0/annotations-spec-2.0.html#annotations
 *
 */
public class AnnotationDiagnosticsCollector extends AbstractDiagnosticsCollector {

    public AnnotationDiagnosticsCollector() {
        super();
    }

    @Override
    protected String getDiagnosticSource() {
        return AnnotationConstants.DIAGNOSTIC_SOURCE;
    }

    @Override
    public void collectDiagnostics(ICompilationUnit unit, List<Diagnostic> diagnostics) {
        if (unit != null) {
            try {
                ArrayList<Tuple.Two<IAnnotation, IAnnotatable>> annotatables = new ArrayList<Two<IAnnotation, IAnnotatable>>();
                String[] validAnnotations = { AnnotationConstants.GENERATED_FQ_NAME };
                String[] validTypeAnnotations = { AnnotationConstants.GENERATED_FQ_NAME,
                        AnnotationConstants.RESOURCE_FQ_NAME };
                String[] validMethodAnnotations = { AnnotationConstants.GENERATED_FQ_NAME,
                        AnnotationConstants.POST_CONSTRUCT_FQ_NAME, AnnotationConstants.PRE_DESTROY_FQ_NAME,
                        AnnotationConstants.RESOURCE_FQ_NAME };

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
                                if (isValidAnnotation(annotation.getElementName(), validAnnotations))
                                    annotatables.add(new Tuple.Two<>(annotation, parameter));
                            }
                        }
                    }
                    // Field
                    IField[] fields = type.getFields();
                    for (IField field : fields) {
                        annotations = field.getAnnotations();
                        for (IAnnotation annotation : annotations) {
                            if (isValidAnnotation(annotation.getElementName(), validTypeAnnotations))
                                annotatables.add(new Tuple.Two<>(annotation, field));
                        }
                    }
                }

                for (Tuple.Two<IAnnotation, IAnnotatable> annotatable : annotatables) {
                    IAnnotation annotation = annotatable.getFirst();
                    IAnnotatable element = annotatable.getSecond();

                    if (isMatchedAnnotation(unit, annotation, AnnotationConstants.GENERATED_FQ_NAME)) {
                        for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
                            // If date element exists and is non-empty, it must follow ISO 8601 format.
                            if (pair.getMemberName().equals("date")) {
                                if (pair.getValue() instanceof String) {
                                    String date = (String) pair.getValue();
                                    if (!date.equals("")) {
                                        if (!Pattern.matches(AnnotationConstants.ISO_8601_REGEX, date)) {

                                            String diagnosticMessage = Messages.getMessage(
                                                    "AnnotationMustDefineAttributeFollowing8601", "@Generated", "date");
                                            diagnostics.add(createDiagnostic(annotation, unit, diagnosticMessage,
                                                    AnnotationConstants.DIAGNOSTIC_CODE_DATE_FORMAT, null,
                                                    DiagnosticSeverity.Error));
                                        }
                                    }
                                }
                            }
                        }
                    } else if (isMatchedAnnotation(unit, annotation, AnnotationConstants.RESOURCE_FQ_NAME)) {
                        if (element instanceof IType) {
                            IType type = (IType) element;
                            if (type.getElementType() == IJavaElement.TYPE && ((IType) type).isClass()) {
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
                                    diagnostics.add(createDiagnostic(annotation, unit, diagnosticMessage,
                                            AnnotationConstants.DIAGNOSTIC_CODE_MISSING_RESOURCE_NAME_ATTRIBUTE, null,
                                            DiagnosticSeverity.Error));
                                }

                                if (typeEmpty) {
                                    diagnosticMessage = Messages.getMessage("AnnotationMustDefineAttribute",
                                            "@Resource", "type");
                                    diagnostics.add(createDiagnostic(annotation, unit, diagnosticMessage,
                                            AnnotationConstants.DIAGNOSTIC_CODE_MISSING_RESOURCE_TYPE_ATTRIBUTE, null,
                                            DiagnosticSeverity.Error));
                                }
                            }
                        }
                    }
                    if (isMatchedAnnotation(unit, annotation, AnnotationConstants.POST_CONSTRUCT_FQ_NAME)) {
                        if (element instanceof IMethod) {
                            IMethod method = (IMethod) element;
                            if (method.getNumberOfParameters() != 0) {
                                String diagnosticMessage = generateDiagnosticMethod("PostConstruct",
                                        "not have any parameters.");
                                diagnostics.add(createDiagnostic(method, unit, diagnosticMessage,
                                        AnnotationConstants.DIAGNOSTIC_CODE_POSTCONSTRUCT_PARAMS, null,
                                        DiagnosticSeverity.Error));
                            }

                            if (!method.getReturnType().equals("V")) {
                                String diagnosticMessage = generateDiagnosticMethod("PostConstruct", "be void.");
                                diagnostics.add(createDiagnostic(method, unit, diagnosticMessage,
                                        AnnotationConstants.DIAGNOSTIC_CODE_POSTCONSTRUCT_RETURN_TYPE, null,
                                        DiagnosticSeverity.Error));
                            }

                            if (method.getExceptionTypes().length != 0) {
                                String diagnosticMessage = generateDiagnosticMethod("PostConstruct",
                                        "not throw checked exceptions.");
                                diagnostics.add(createDiagnostic(method, unit, diagnosticMessage,
                                        AnnotationConstants.DIAGNOSTIC_CODE_POSTCONSTRUCT_EXCEPTION, null,
                                        DiagnosticSeverity.Warning));
                            }
                        }
                    } else if (isMatchedAnnotation(unit, annotation, AnnotationConstants.PRE_DESTROY_FQ_NAME)) {
                        if (element instanceof IMethod) {
                            IMethod method = (IMethod) element;
                            if (method.getNumberOfParameters() != 0) {
                                String diagnosticMessage = generateDiagnosticMethod("PreDestroy",
                                        "not have any parameters.");
                                diagnostics.add(createDiagnostic(method, unit, diagnosticMessage,
                                        AnnotationConstants.DIAGNOSTIC_CODE_PREDESTROY_PARAMS, null,
                                        DiagnosticSeverity.Error));
                            }

                            if (Flags.isStatic(method.getFlags())) {
                                String diagnosticMessage = generateDiagnosticMethod("PreDestroy", "not be static.");
                                diagnostics.add(createDiagnostic(method, unit, diagnosticMessage,
                                        AnnotationConstants.DIAGNOSTIC_CODE_PREDESTROY_STATIC, method.getElementType(),
                                        DiagnosticSeverity.Error));
                            }

                            if (method.getExceptionTypes().length != 0) {
                                String diagnosticMessage = generateDiagnosticMethod("PreDestroy",
                                        "not throw checked exceptions.");
                                diagnostics.add(createDiagnostic(method, unit, diagnosticMessage,
                                        AnnotationConstants.DIAGNOSTIC_CODE_PREDESTROY_EXCEPTION, null,
                                        DiagnosticSeverity.Warning));
                            }
                        }
                    }
                }
            } catch (JavaModelException e) {
                JakartaCorePlugin.logException("Cannot calculate diagnostics", e);
            }
        }
    }

    private static String generateDiagnosticMethod(String annotation, String message) {
        String finalMessage = "A method with the @" + annotation + " annotation must " + message;
        return finalMessage;
    }

    private static boolean isValidAnnotation(String annotationName, String[] validAnnotations) {
        for (String fqName : validAnnotations) {
            if (fqName.endsWith(annotationName)) {
                return true;
            }
        }
        return false;
    }
}
