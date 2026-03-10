/*******************************************************************************
* Copyright (c) 2020, 2026 IBM Corporation and others.
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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
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

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(BeanValidationDiagnosticsParticipant.class.getName());

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
                Range range = PositionUtils.toNameRange(field, context.getUtils());
                // Check for conflicting constraints on fields
                checkConflictingConstraints(context, uri, field, annotations, diagnostics, range);

                for (IAnnotation annotation : annotations) {
                    String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                         annotation.getElementName(),
                                                                                         SET_OF_ANNOTATIONS.toArray(new String[0]));
                    if (matchedAnnotation != null) {
                        validAnnotation(context, uri, field, range, annotation, matchedAnnotation, diagnostics);
                    }
                }
            }
            allMethods = type.getMethods();
            for (IMethod method : allMethods) {
                annotations = method.getAnnotations();
                Range range = PositionUtils.toNameRange(method, context.getUtils());
                // Check for conflicting constraints on methods
                checkConflictingConstraints(context, uri, method, annotations, diagnostics, range);

                for (IAnnotation annotation : annotations) {
                    String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                         annotation.getElementName(),
                                                                                         SET_OF_ANNOTATIONS.toArray(new String[0]));
                    if (matchedAnnotation != null) {
                        validAnnotation(context, uri, method, range, annotation, matchedAnnotation, diagnostics);
                    }
                }
                // parameter level annotations
                for (ILocalVariable param : method.getParameters()) {
                    IAnnotation[] paramAnnotations = param.getAnnotations();
                    Range paramRange = PositionUtils.toNameRange(param, context.getUtils());
                    // Check for conflicting constraints on parameters
                    checkConflictingConstraints(context, uri, param, paramAnnotations, diagnostics, paramRange);

                    for (IAnnotation annotation : paramAnnotations) {
                        String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                             annotation.getElementName(),
                                                                                             SET_OF_ANNOTATIONS.toArray(new String[0]));

                        if (matchedAnnotation != null) {
                            validAnnotation(context, uri, param, paramRange, annotation, matchedAnnotation, diagnostics);
                        }
                    }
                }
            }
        }

        return diagnostics;
    }

    private void validAnnotation(JavaDiagnosticsContext context, String uri, IJavaElement element, Range range,
                                 IAnnotation annotation,
                                 String matchedAnnotation,
                                 List<Diagnostic> diagnostics) throws CoreException {

        String type = null;
        IType declaringType = null;
        boolean isMethod = false;
        boolean isField = false;
        if (element instanceof IMethod) {
            type = ((IMethod) element).getReturnType();
            declaringType = ((IMember) element).getDeclaringType();
            isMethod = true;
        } else if (element instanceof IField) {
            type = ((IField) element).getTypeSignature();
            declaringType = ((IMember) element).getDeclaringType();
            isField = true;
        } else if (isParameterType(element)) {
            type = ((ILocalVariable) element).getTypeSignature();
            declaringType = ((IMethod) ((ILocalVariable) element).getDeclaringMember()).getDeclaringType();
        }

        if (declaringType != null) {
            String annotationName = annotation.getElementName();

            //The below block throws diagnostics if invalid element type is used with constraint annotations
            switch (matchedAnnotation) {
                case ASSERT_FALSE, ASSERT_TRUE -> {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType,
                                                                                      getDataTypeName(type),
                                                                                      new String[] { BOOLEAN_FQ });
                    String message = getDiagnosticMessage(isMethod, isField, annotationName, "AnnotationBoolean");

                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BOOLEAN)) {
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonBooleanMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
                case DECIMAL_MAX, DECIMAL_MIN, DIGITS -> {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType,
                                                                                      getDataTypeName(type),
                                                                                      NUMERIC_AND_CHAR_WRAPPER_TYPES);

                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG)) {
                        String message = getDiagnosticMessage(isMethod, isField, annotationName,
                                                              "AnnotationBigDecimal");
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation,
                                                                 ErrorCode.InvalidAnnotationOnNonBigDecimalCharByteShortIntLongMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
                case EMAIL, NOT_BLANK, PATTERN -> checkStringOnly(context, uri, range, diagnostics, annotationName, isMethod, type, matchedAnnotation, declaringType, isField);
                case FUTURE, FUTURE_OR_PRESENT, PAST, PAST_OR_PRESENT -> {
                    String dataType = getDataTypeName(type);
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, dataType,
                                                                                      SET_OF_DATE_TYPES.toArray(new String[0]));
                    if (dataTypeFQName == null) {
                        String message = getDiagnosticMessage(isMethod, isField, annotationName, "AnnotationDate");
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonDateTimeMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
                case MIN, MAX -> {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType,
                                                                                      getDataTypeName(type),
                                                                                      NUMERIC_WRAPPER_TYPES);
                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG)) {
                        String message = getDiagnosticMessage(isMethod, isField, annotationName, "AnnotationMinMax");
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonMinMaxMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
                case NEGATIVE, NEGATIVE_OR_ZERO, POSITIVE, POSITIVE_OR_ZERO -> {
                    String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType,
                                                                                      getDataTypeName(type),
                                                                                      NUMERIC_AND_DECIMAL_WRAPPER_TYPES);
                    if (dataTypeFQName == null && !type.equals(Signature.SIG_BYTE)
                        && !type.equals(Signature.SIG_SHORT) && !type.equals(Signature.SIG_INT)
                        && !type.equals(Signature.SIG_LONG) && !type.equals(Signature.SIG_FLOAT)
                        && !type.equals(Signature.SIG_DOUBLE)) {
                        String message = getDiagnosticMessage(isMethod, isField, annotationName, "AnnotationPositive");
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
                case NOT_EMPTY, SIZE -> {
                    if (!(isSizeOrNonEmptyAllowed(declaringType, type))) {
                        String message = getDiagnosticMessage(isMethod, isField, annotationName,
                                                              "SizeOrNonEmptyAnnotations");
                        diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE,
                                                                 matchedAnnotation, ErrorCode.InvalidAnnotationOnNonSizeMethodOrField,
                                                                 DiagnosticSeverity.Error));
                    }
                }
                default -> LOGGER.log(Level.SEVERE, "Unexpected value for annotation");
            }
            //Throws invalid static element diagnostics if the element is static and has constraint annotations
            if (!isParameterType(element) && Flags.isStatic(((IMember) element).getFlags())) {
                String message = isMethod ? Messages.getMessage("ConstraintAnnotationsMethod") : Messages.getMessage("ConstraintAnnotationsField");
                diagnostics.add(context.createDiagnostic(uri, message, range, Constants.DIAGNOSTIC_SOURCE, matchedAnnotation,
                                                         ErrorCode.InvalidConstrainAnnotationOnStaticMethodOrField, DiagnosticSeverity.Error));
            }
        }
    }

    /**
     * getDiagnosticMessage
     *
     * @param isMethod
     * @param isField
     * @param annotationName
     * @param messageKey
     * @return
     */
    private String getDiagnosticMessage(boolean isMethod, boolean isField, String annotationName, String messageKey) {
        String message = isMethod ? Messages.getMessage(messageKey + "Methods",
                                                        "@" + annotationName) : isField ? Messages.getMessage(messageKey + "Fields",
                                                                                                              "@" + annotationName) : Messages.getMessage(messageKey + "Params",
                                                                                                                                                          "@" + annotationName);
        return message;
    }

    private boolean isParameterType(IJavaElement element) {
        return element instanceof ILocalVariable;
    }

    /**
     * isSizeOrNonEmptyAllowed
     * This method checks whether the supported types for the Size and NotEmpty annotations are CharSequence, Collection, Map, or array.
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
                                 String annotationName, boolean isMethod, String type, String matchedAnnotation, IType declaringType, boolean isField) throws JavaModelException {
        String dataTypeFQName = DiagnosticUtils.getMatchedJavaElementName(declaringType, getDataTypeName(type),
                                                                          new String[] { STRING_FQ, CHAR_SEQUENCE_FQ });
        if (dataTypeFQName == null) {
            String message = getDiagnosticMessage(isMethod, isField, annotationName, "AnnotationString");
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
        return TypeHierarchyUtils.doesITypeHaveSuperType(fieldType, superType) == 1;
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

    /**
     * Check for conflicting constraint annotations (e.g., @Min > @Max, @DecimalMin > @DecimalMax, @Size min > max).
     *
     * @param range
     */
    private void checkConflictingConstraints(JavaDiagnosticsContext context, String uri, IJavaElement element,
                                             IAnnotation[] annotations, List<Diagnostic> diagnostics, Range range) throws JavaModelException {
        IType declaringType = element instanceof IMember ? ((IMember) element).getDeclaringType() : element instanceof ILocalVariable ? ((IMethod) ((ILocalVariable) element).getDeclaringMember()).getDeclaringType() : null;
        if (declaringType == null)
            return;

        IAnnotation minAnn = null, maxAnn = null, decMinAnn = null, decMaxAnn = null, sizeAnn = null;

        for (IAnnotation ann : annotations) {
            String matched = DiagnosticUtils.getMatchedJavaElementName(declaringType, ann.getElementName(),
                                                                       new String[] { MIN, MAX, DECIMAL_MIN, DECIMAL_MAX, SIZE });
            if (matched != null) {
                switch (matched) {
                    case MIN -> minAnn = ann;
                    case MAX -> maxAnn = ann;
                    case DECIMAL_MIN -> decMinAnn = ann;
                    case DECIMAL_MAX -> decMaxAnn = ann;
                    case SIZE -> sizeAnn = ann;
                }
            }
        }

        // Check @Min/@Max conflict
        if (minAnn != null && maxAnn != null) {
            Long min = getNumericValue(minAnn, "value", Long.class);
            Long max = getNumericValue(maxAnn, "value", Long.class);
            if (min != null && max != null && min > max) {
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("ConflictingConstraintAnnotationsMinMax", min.toString(), max.toString()),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null, ErrorCode.ConflictingConstraintAnnotations, DiagnosticSeverity.Warning));
            }
        }

        // Check @DecimalMin/@DecimalMax conflict
        if (decMinAnn != null && decMaxAnn != null) {
            String min = DiagnosticUtils.getAnnotationMemberValue(decMinAnn, "value", String.class);
            String max = DiagnosticUtils.getAnnotationMemberValue(decMaxAnn, "value", String.class);
            if (min != null && max != null) {
                try {
                    if (Double.parseDouble(min) > Double.parseDouble(max)) {
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("ConflictingConstraintAnnotationsDecimalMinMax", min, max),
                                                                 range, Constants.DIAGNOSTIC_SOURCE, null, ErrorCode.ConflictingConstraintAnnotations, DiagnosticSeverity.Warning));
                    }
                } catch (NumberFormatException e) {
                    LOGGER.log(Level.INFO, "Ignore invalid number format");
                }
            }
        }

        // Check @Size min/max conflict
        if (sizeAnn != null) {
            Integer min = getNumericValue(sizeAnn, "min", Integer.class);
            Integer max = getNumericValue(sizeAnn, "max", Integer.class);
            if (min != null && max != null && min > max) {
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage("ConflictingConstraintAnnotationsSize", min.toString(), max.toString()),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null, ErrorCode.ConflictingConstraintAnnotations, DiagnosticSeverity.Warning));
            }
        }

    }

    /**
     * getNumericValue
     * Helper method to get numeric annotation values with type conversion.
     * Handles conversion from any Number type to the expected type.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Number> T getNumericValue(IAnnotation annotation, String memberName, Class<T> expectedType) throws JavaModelException {
        Object value = DiagnosticUtils.getAnnotationMemberValue(annotation, memberName, Object.class);
        if (value instanceof Number) {
            Number num = (Number) value;
            if (expectedType == Long.class) {
                return (T) Long.valueOf(num.longValue());
            } else if (expectedType == Integer.class) {
                return (T) Integer.valueOf(num.intValue());
            }
        }
        return null;
    }
}