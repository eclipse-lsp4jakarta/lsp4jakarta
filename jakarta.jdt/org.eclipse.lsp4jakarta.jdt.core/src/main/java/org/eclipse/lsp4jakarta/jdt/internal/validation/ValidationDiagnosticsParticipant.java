/*******************************************************************************
* Copyright (c) 2020, 2023 IBM Corporation and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Reza Akhavan - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.validation;

import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.ASSERT_FALSE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.ASSERT_TRUE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.BIG_DECIMAL;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.BIG_INTEGER;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.BOOLEAN;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.BYTE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.CHAR_SEQUENCE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.DECIMAL_MAX;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.DECIMAL_MIN;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.DIGITS;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.DOUBLE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.EMAIL;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.FLOAT;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.FUTURE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.FUTURE_OR_PRESENT;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.INTEGER;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.LONG;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.MAX;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.MIN;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.NEGATIVE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.NEGATIVE_OR_ZERO;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.NOT_BLANK;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.PAST;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.PAST_OR_PRESENT;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.PATTERN;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.POSITIVE;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.POSITIVE_OR_ZERO;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.SET_OF_ANNOTATIONS;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.SET_OF_DATE_TYPES;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.SHORT;
import static org.eclipse.lsp4jakarta.jdt.internal.validation.Constants.STRING;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
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
 * Bean validation diagnostics participant that manages the use of validation
 * element constraints.
 */
public class ValidationDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] alltypes;
        IField[] allFields;
        IAnnotation[] annotations;
        IMethod[] allMethods;

        alltypes = unit.getAllTypes();
        for (IType type : alltypes) {
            allFields = type.getFields();
            for (IField field : allFields) {
                annotations = field.getAnnotations();
                for (IAnnotation annotation : annotations) {
                    String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                         annotation.getElementName(),
                                                                                         SET_OF_ANNOTATIONS.toArray(new String[0]));
                    if (matchedAnnotation != null) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        validAnnotation(context, uri, field, range, annotation, matchedAnnotation, diagnostics);
                    }
                }
            }
            allMethods = type.getMethods();
            for (IMethod method : allMethods) {
                annotations = method.getAnnotations();
                for (IAnnotation annotation : annotations) {
                    String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                         annotation.getElementName(),
                                                                                         SET_OF_ANNOTATIONS.toArray(new String[0]));
                    if (matchedAnnotation != null) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        validAnnotation(context, uri, method, range, annotation, matchedAnnotation, diagnostics);
                    }
                }
            }
        }

        return diagnostics;
    }

    private void validAnnotation(JavaDiagnosticsContext context, String uri, IMember element, Range range,
                                 IAnnotation annotation,
                                 String matchedAnnotation,
                                 List<Diagnostic> diagnostics) throws JavaModelException {
        IType declaringType = element.getDeclaringType();
        if (declaringType != null) {
            String annotationName = annotation.getElementName();
            boolean isMethod = element instanceof IMethod;

            if (Flags.isStatic(element.getFlags())) {
                String message = isMethod ? Messages.getMessage("ConstraintAnnotationsMethod") : Messages.getMessage("ConstraintAnnotationsField");
                diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE, annotationName,
                                                         ErrorCode.InvalidConstrainAnnotationOnStaticMethodOrField, DiagnosticSeverity.Error));
            } else {
                String type = (isMethod) ? ((IMethod) element).getReturnType() : ((IField) element).getTypeSignature();

                if (matchedAnnotation.equals(ASSERT_FALSE) || matchedAnnotation.equals(ASSERT_TRUE)) {
                    String message = isMethod ? Messages.getMessage("AnnotationBooleanMethods", "@" + annotationName) : Messages.getMessage("AnnotationBooleanFields",
                                                                                                                                            "@" + annotationName);
                    if (!type.equals(getSignatureFormatOfType(BOOLEAN)) && !type.equals(Signature.SIG_BOOLEAN)) {
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 annotationName, ErrorCode.InvalidAnnotationOnNonBooleanMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(DECIMAL_MAX) || matchedAnnotation.equals(DECIMAL_MIN)
                           || matchedAnnotation.equals(DIGITS)) {
                    if (!type.equals(getSignatureFormatOfType(BIG_DECIMAL))
                        && !type.equals(getSignatureFormatOfType(BIG_INTEGER))
                        && !type.equals(getSignatureFormatOfType(CHAR_SEQUENCE))
                        && !type.equals(getSignatureFormatOfType(BYTE))
                        && !type.equals(getSignatureFormatOfType(SHORT))
                        && !type.equals(getSignatureFormatOfType(INTEGER))
                        && !type.equals(getSignatureFormatOfType(LONG)) && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG)) {
                        String message = isMethod ? Messages.getMessage("AnnotationBigDecimalMethods", "@" + annotationName) : Messages.getMessage("AnnotationBigDecimalFields",
                                                                                                                                                   "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 annotationName,
                                                                 ErrorCode.InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(EMAIL)) {
                    checkStringOnly(context, uri, element, range, declaringType, diagnostics, annotationName, isMethod,
                                    type);
                } else if (matchedAnnotation.equals(NOT_BLANK)) {
                    checkStringOnly(context, uri, element, range, declaringType, diagnostics, annotationName, isMethod,
                                    type);
                } else if (matchedAnnotation.equals(PATTERN)) {
                    checkStringOnly(context, uri, element, range, declaringType, diagnostics, annotationName, isMethod,
                                    type);
                } else if (matchedAnnotation.equals(FUTURE) || matchedAnnotation.equals(FUTURE_OR_PRESENT)
                           || matchedAnnotation.equals(PAST) || matchedAnnotation.equals(PAST_OR_PRESENT)) {
                    String dataType = getDataTypeName(type);
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, dataType,
                                                                                      SET_OF_DATE_TYPES.toArray(new String[0]));
                    if (dataTypeFQName == null) {
                        String message = isMethod ? Messages.getMessage("AnnotationDateMethods", "@" + annotationName) : Messages.getMessage("AnnotationDateFields",
                                                                                                                                             "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 annotationName, ErrorCode.InvalidAnnotationOnNonDateTimeMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(MIN) || matchedAnnotation.equals(MAX)) {
                    if (!type.equals(getSignatureFormatOfType(BIG_DECIMAL))
                        && !type.equals(getSignatureFormatOfType(BIG_INTEGER))
                        && !type.equals(getSignatureFormatOfType(BYTE))
                        && !type.equals(getSignatureFormatOfType(SHORT))
                        && !type.equals(getSignatureFormatOfType(INTEGER))
                        && !type.equals(getSignatureFormatOfType(LONG)) && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG)) {
                        String message = isMethod ? Messages.getMessage("AnnotationMinMaxMethods", "@" + annotationName) : Messages.getMessage("AnnotationMinMaxFields",
                                                                                                                                               "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 annotationName, ErrorCode.InvalidAnnotationOnNonMinMaxMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(NEGATIVE) || matchedAnnotation.equals(NEGATIVE_OR_ZERO)
                           || matchedAnnotation.equals(POSITIVE) || matchedAnnotation.equals(POSITIVE_OR_ZERO)) {
                    if (!type.equals(getSignatureFormatOfType(BIG_DECIMAL))
                        && !type.equals(getSignatureFormatOfType(BIG_INTEGER))
                        && !type.equals(getSignatureFormatOfType(BYTE))
                        && !type.equals(getSignatureFormatOfType(SHORT))
                        && !type.equals(getSignatureFormatOfType(INTEGER))
                        && !type.equals(getSignatureFormatOfType(LONG))
                        && !type.equals(getSignatureFormatOfType(FLOAT))
                        && !type.equals(getSignatureFormatOfType(DOUBLE)) && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG) && !type.equals(Signature.SIG_FLOAT)
                        && !type.equals(Signature.SIG_DOUBLE)) {
                        String message = isMethod ? Messages.getMessage("AnnotationPositiveMethods", "@" + annotationName) : Messages.getMessage("AnnotationPositiveFields",
                                                                                                                                                 "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 annotationName, ErrorCode.InvalidAnnotationOnNonPositiveMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }

                // These ones contains check on all collection types which requires resolving
                // the String of the type somehow
                // This will also require us to check if the field type was a custom collection
                // subtype which means we
                // have to resolve it and get the super interfaces and check to see if
                // Collection, Map or Array was implemented
                // for that custom type (which could as well be a user made subtype)

//    			else if (annotation.getElementName().equals(NOT_EMPTY) || annotation.getElementName().equals(SIZE)) {
//
//    				System.out.println("--Field name: " + Signature.getTypeSignatureKind(fieldType));
//    				System.out.println("--Field name: " + Signature.getParameterTypes(fieldType));
//    				if (	!fieldType.equals(getSignatureFormatOfType(CHAR_SEQUENCE)) &&
//    						!fieldType.contains("List") &&
//    						!fieldType.contains("Set") &&
//    						!fieldType.contains("Collection") &&
//    						!fieldType.contains("Array") &&
//    						!fieldType.contains("Vector") &&
//    						!fieldType.contains("Stack") &&
//    						!fieldType.contains("Queue") &&
//    						!fieldType.contains("Deque") &&
//    						!fieldType.contains("Map")) {
//
//    					diagnostics.add(new Diagnostic(fieldAnnotationrange,
//    							"This annotation can only be used on CharSequence, Collection, Array, "
//    							+ "Map type fields."));
//    				}
//    			}
            }
        }
    }

    private void checkStringOnly(JavaDiagnosticsContext context, String uri, IMember element, Range range,
                                 IType declaringType,
                                 List<Diagnostic> diagnostics,
                                 String annotationName, boolean isMethod, String type) throws JavaModelException {
        if (!type.equals(getSignatureFormatOfType(STRING))
            && !type.equals(getSignatureFormatOfType(CHAR_SEQUENCE))) {
            String message = isMethod ? Messages.getMessage("AnnotationStringMethods", "@" + annotationName) : Messages.getMessage("AnnotationStringFields", "@" + annotationName);
            diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                     annotationName, ErrorCode.InvalidAnnotationOnNonStringMethodOrField,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * Refer to Class signature documentation for the formating
     * https://www.ibm.com/support/knowledgecenter/sl/SS5JSH_9.5.0/org.eclipse.jdt.
     * doc.isv/reference/api/org/eclipse/jdt/core/Signature.html
     */
    private static String getSignatureFormatOfType(String type) {
        return "Q" + type + ";";
    }

    private static String getDataTypeName(String type) {
        int length = type.length();
        if (length > 0 && type.charAt(0) == 'Q' && type.charAt(length - 1) == ';') {
            return type.substring(1, length - 1);
        }
        return type;
    }
}