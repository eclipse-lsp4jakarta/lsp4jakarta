/*******************************************************************************
* Copyright (c) 2020, 2025 IBM Corporation and others.
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

package org.eclipse.lsp4jakarta.jdt.internal.beanvalidation;

import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.ASSERT_FALSE;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.ASSERT_TRUE;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.BOOLEAN_FQ;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.CHAR_SEQUENCE_FQ;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.DECIMAL_MAX;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.DECIMAL_MIN;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.DIGITS;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.EMAIL;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.FUTURE;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.FUTURE_OR_PRESENT;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.MAX;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.MIN;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NEGATIVE;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NEGATIVE_OR_ZERO;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NOT_BLANK;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NOT_EMPTY;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NUMERIC_AND_CHAR_WRAPPER_TYPES;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NUMERIC_AND_DECIMAL_WRAPPER_TYPES;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.NUMERIC_WRAPPER_TYPES;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.PAST;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.PAST_OR_PRESENT;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.PATTERN;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.POSITIVE;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.POSITIVE_OR_ZERO;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.PRIMITIVE_TYPES;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.SET_OF_ANNOTATIONS;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.SET_OF_DATE_TYPES;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.SIZE;
import static org.eclipse.lsp4jakarta.jdt.internal.beanvalidation.Constants.STRING_FQ;

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
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Bean validation diagnostics participant that manages the use of validation
 * element constraints.
 */
public class BeanValidationDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
                                 List<Diagnostic> diagnostics) throws CoreException {
        IType declaringType = element.getDeclaringType();
        if (declaringType != null) {
            String annotationName = annotation.getElementName();
            boolean isMethod = element instanceof IMethod;

            if (Flags.isStatic(element.getFlags())) {
                String message = isMethod ? Messages.getMessage("ConstraintAnnotationsMethod") : Messages.getMessage("ConstraintAnnotationsField");
                diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE, matchedAnnotation,
                                                         ErrorCode.InvalidConstrainAnnotationOnStaticMethodOrField, DiagnosticSeverity.Error));
            } else {
                String type = (isMethod) ? ((IMethod) element).getReturnType() : ((IField) element).getTypeSignature();

                if (matchedAnnotation.equals(ASSERT_FALSE) || matchedAnnotation.equals(ASSERT_TRUE)) {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, getDataTypeName(type),
                                                                                      new String[] { BOOLEAN_FQ });
                    String message = isMethod ? Messages.getMessage("AnnotationBooleanMethods", "@" + annotationName) : Messages.getMessage("AnnotationBooleanFields",
                                                                                                                                            "@" + annotationName);
                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BOOLEAN)) {
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonBooleanMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(DECIMAL_MAX) || matchedAnnotation.equals(DECIMAL_MIN)
                           || matchedAnnotation.equals(DIGITS)) {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, getDataTypeName(type),
                                                                                      NUMERIC_AND_CHAR_WRAPPER_TYPES);

                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG)) {
                        String message = isMethod ? Messages.getMessage("AnnotationBigDecimalMethods", "@" + annotationName) : Messages.getMessage("AnnotationBigDecimalFields",
                                                                                                                                                   "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation,
                                                                 ErrorCode.InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(EMAIL)) {
                    checkStringOnly(context, uri, range, diagnostics, annotationName, isMethod,
                                    type, matchedAnnotation, declaringType);
                } else if (matchedAnnotation.equals(NOT_BLANK)) {
                    checkStringOnly(context, uri, range, diagnostics, annotationName, isMethod,
                                    type, matchedAnnotation, declaringType);
                } else if (matchedAnnotation.equals(PATTERN)) {
                    checkStringOnly(context, uri, range, diagnostics, annotationName, isMethod,
                                    type, matchedAnnotation, declaringType);
                } else if (matchedAnnotation.equals(FUTURE) || matchedAnnotation.equals(FUTURE_OR_PRESENT)
                           || matchedAnnotation.equals(PAST) || matchedAnnotation.equals(PAST_OR_PRESENT)) {
                    String dataType = getDataTypeName(type);
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, dataType,
                                                                                      SET_OF_DATE_TYPES.toArray(new String[0]));
                    if (dataTypeFQName == null) {
                        String message = isMethod ? Messages.getMessage("AnnotationDateMethods", "@" + annotationName) : Messages.getMessage("AnnotationDateFields",
                                                                                                                                             "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonDateTimeMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(MIN) || matchedAnnotation.equals(MAX)) {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, getDataTypeName(type),
                                                                                      NUMERIC_WRAPPER_TYPES);
                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG)) {
                        String message = isMethod ? Messages.getMessage("AnnotationMinMaxMethods", "@" + annotationName) : Messages.getMessage("AnnotationMinMaxFields",
                                                                                                                                               "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonMinMaxMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                } else if (matchedAnnotation.equals(NEGATIVE) || matchedAnnotation.equals(NEGATIVE_OR_ZERO)
                           || matchedAnnotation.equals(POSITIVE) || matchedAnnotation.equals(POSITIVE_OR_ZERO)) {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, getDataTypeName(type),
                                                                                      NUMERIC_AND_DECIMAL_WRAPPER_TYPES);
                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG) && !type.equals(Signature.SIG_FLOAT)
                        && !type.equals(Signature.SIG_DOUBLE)) {
                        String message = isMethod ? Messages.getMessage("AnnotationPositiveMethods", "@" + annotationName) : Messages.getMessage("AnnotationPositiveFields",
                                                                                                                                                 "@" + annotationName);
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonPositiveMethodOrField,
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

                else if (matchedAnnotation.equals(NOT_EMPTY) || matchedAnnotation.equals(SIZE)) {
                    if (!(isSizeOrNonEmptyAllowed(declaringType, type))) {
                        String message = isMethod ? Messages.getMessage("SizeOrNonEmptyAnnotationsMethod") : Messages.getMessage("SizeOrNonEmptyAnnotationsField");
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonSizeMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
            }
        }
    }

    /**
     * isSizeOrNonEmptyAllowed
     * The annotated element size must be between the specified boundaries (included).
     * https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0#builtinconstraints-size
     * https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0#builtinconstraints-notempty
     *
     * @param parentType
     * @param childTypeString
     * @return
     * @throws CoreException
     */
    boolean isSizeOrNonEmptyAllowed(IType parentType, String childTypeString) throws CoreException {
        if (isArrayType(childTypeString)) {
            return true;
        } else if (PRIMITIVE_TYPES.contains(childTypeString)) {
            return false;
        } else {
            IType fieldType = ManagedBean.getChildITypeByName(parentType, getDataTypeName(childTypeString));
            return fieldType != null
                   && (doesITypeHaveSuperType(fieldType, Constants.CHAR_SEQUENCE_FQ)
                       || doesITypeHaveSuperType(fieldType, Constants.COLLECTION_FQ)
                       || doesITypeHaveSuperType(fieldType, Constants.MAP_FQ));
        }
    }

    private void checkStringOnly(JavaDiagnosticsContext context, String uri, Range range,
                                 List<Diagnostic> diagnostics,
                                 String annotationName, boolean isMethod, String type, String matchedAnnotation, IType declaringType) throws JavaModelException {
        String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, getDataTypeName(type),
                                                                          new String[] { STRING_FQ, CHAR_SEQUENCE_FQ });
        if (dataTypeFQName == null) {
            String message = isMethod ? Messages.getMessage("AnnotationStringMethods", "@" + annotationName) : Messages.getMessage("AnnotationStringFields", "@" + annotationName);
            diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                     matchedAnnotation, ErrorCode.InvalidAnnotationOnNonStringMethodOrField,
                                                     DiagnosticSeverity.Error));
        }
    }

    private static String getDataTypeName(String type) {
        int length = type.length();
        if (length > 0 && type.charAt(0) == 'Q' && type.charAt(length - 1) == ';') {
            return type.substring(1, length - 1);
        }
        return type;
    }

    /**
     * doesITypeHaveSuperType
     * Check if specified superType is present or not in the type hierarchy
     *
     * @param fieldType
     * @param superType
     * @return
     * @throws CoreException
     */
    private boolean doesITypeHaveSuperType(IType fieldType, String superType) throws CoreException {
        return TypeHierarchyUtils.doesITypeHaveSuperType(fieldType, superType) > 0;
    }

    /**
     * Return true if it is Array type, and false otherwise
     *
     * @param childTypeString
     * @return
     */
    public static boolean isArrayType(String childTypeString) {
        return null != childTypeString && childTypeString.startsWith("[");
    }
}