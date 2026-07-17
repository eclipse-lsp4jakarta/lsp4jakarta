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
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
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
 * Persistence diagnostic participant that validates the use of
 * @TableGenerator, @TableGenerators, @SequenceGenerator, @SequenceGenerators,
 * @SecondaryTable, and @SecondaryTables annotations.
 * <p>@TableGenerator, @TableGenerators, @SequenceGenerator, and @SequenceGenerators
 * may appear on TYPE, METHOD, or FIELD elements.
 *
 * @SecondaryTable and @SecondaryTables may only appear on TYPE elements.
 *                 <p>Validates that:
 *                 <ul>
 *                 <li>@TableGenerator must specify a non-empty 'name' attribute</li>
 *                 <li>@TableGenerators must specify at least one @TableGenerator</li>
 *                 <li>@SequenceGenerator must specify a non-empty 'name' attribute</li>
 *                 <li>@SequenceGenerators must specify at least one @SequenceGenerator</li>
 *                 <li>@SecondaryTable must specify a non-empty 'name' attribute</li>
 *                 <li>@SecondaryTables must specify at least one @SecondaryTable</li>
 *                 </ul>
 */
public class PersistenceGeneratorDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(PersistenceGeneratorDiagnosticsParticipant.class.getName());

    /**
     * Collects diagnostics for all types in the compilation unit identified by the given context.
     * <p>
     * For each type, inspects annotations at the type, field, and method level and delegates
     * validation to {@link #validateAnnotation}.
     *
     * @param context the diagnostics context providing the document URI and utilities
     * @param monitor progress monitor (unused but required by the interface contract)
     * @return list of {@link Diagnostic} instances found; never {@code null}
     * @throws CoreException if the compilation unit cannot be accessed
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

        IType[] allTypes = unit.getAllTypes();
        for (IType type : allTypes) {
            // Check type-level annotations (@SecondaryTable/s, @TableGenerator/s, @SequenceGenerator/s)
            Arrays.stream(type.getAnnotations()).forEach(typeAnnotation -> validateAnnotation(typeAnnotation, type, context, uri, diagnostics));
            // Check field-level annotations (@TableGenerator/s, @SequenceGenerator/s)
            for (IField field : type.getFields()) {
                Arrays.stream(field.getAnnotations()).forEach(fieldAnnotation -> validateAnnotation(fieldAnnotation, type, context, uri, diagnostics));
            }
            // Check method-level annotations (@TableGenerator/s, @SequenceGenerator/s)
            for (IMethod method : type.getMethods()) {
                Arrays.stream(method.getAnnotations()).forEach(methodAnnotation -> validateAnnotation(methodAnnotation, type, context, uri, diagnostics));
            }
        }

        return diagnostics;
    }

    /**
     * Dispatches validation for a single annotation found on a type, field, or method.
     * <p>
     * Singular annotations ({@code @TableGenerator}, {@code @SequenceGenerator},
     * {@code @SecondaryTable}) are validated via {@link #validateNameAttribute}.
     * Container annotations ({@code @TableGenerators}, {@code @SequenceGenerators},
     * {@code @SecondaryTables}) are validated via {@link #validateContainerAnnotation}.
     * Annotations that do not match any of the six known names are silently ignored.
     *
     * @param annotation the annotation to validate
     * @param type the enclosing type, used for import resolution
     * @param context the diagnostics context
     * @param uri the document URI, used when creating diagnostics
     * @param diagnostics the mutable list to which any new diagnostics are appended
     */
    private void validateAnnotation(IAnnotation annotation, IType type, JavaDiagnosticsContext context,
                                    String uri, List<Diagnostic> diagnostics) {
        try {
            String name = annotation.getElementName();
            if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.TABLEGENERATOR)) {
                validateNameAttribute(annotation, context, uri, diagnostics,
                                      "TableGeneratorInvalidEmptyName", ErrorCode.TableGeneratorInvalidEmptyName);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.SEQUENCEGENERATOR)) {
                validateNameAttribute(annotation, context, uri, diagnostics,
                                      "SequenceGeneratorInvalidEmptyName", ErrorCode.SequenceGeneratorInvalidEmptyName);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.SECONDARYTABLE)) {
                validateNameAttribute(annotation, context, uri, diagnostics,
                                      "SecondaryTableInvalidEmptyName", ErrorCode.SecondaryTableInvalidEmptyName);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.TABLEGENERATORS)) {
                validateNonEmptyMappingArray(annotation, context, uri, diagnostics,
                                             "TableGeneratorsMissingTableGeneratorMapping", ErrorCode.TableGeneratorsMissingTableGeneratorMapping,
                                             "TableGeneratorInvalidEmptyName", ErrorCode.TableGeneratorInvalidEmptyName);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.SEQUENCEGENERATORS)) {
                validateNonEmptyMappingArray(annotation, context, uri, diagnostics,
                                             "SequenceGeneratorsMissingSequenceGeneratorMapping", ErrorCode.SequenceGeneratorsMissingSequenceGeneratorMapping,
                                             "SequenceGeneratorInvalidEmptyName", ErrorCode.SequenceGeneratorInvalidEmptyName);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.SECONDARYTABLES)) {
                validateNonEmptyMappingArray(annotation, context, uri, diagnostics,
                                             "SecondaryTablesMissingSecondaryTableMapping", ErrorCode.SecondaryTablesMissingSecondaryTableMapping,
                                             "SecondaryTableInvalidEmptyName", ErrorCode.SecondaryTableInvalidEmptyName);
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.WARNING, "Error while validating persistence generator annotations", e);
        }
    }

    /**
     * Validates that the given annotation declares a non-empty {@code name} attribute.
     * <p>
     * A diagnostic is added to {@code diagnostics} if the {@code name} attribute is absent,
     * {@code null}, or an empty string.
     * Uses {@link DiagnosticUtils#getAnnotationMemberValue} to retrieve the attribute value.
     *
     * @param annotation the annotation whose {@code name} attribute is checked
     * @param context the diagnostics context
     * @param uri the document URI, used when creating the diagnostic
     * @param diagnostics the mutable list to which a diagnostic is appended on failure
     * @param messageKey message bundle key for the diagnostic message
     * @param errorCode error code to attach to the diagnostic
     * @throws JavaModelException if the annotation's member value pairs cannot be read
     */
    private void validateNameAttribute(IAnnotation annotation, JavaDiagnosticsContext context,
                                       String uri, List<Diagnostic> diagnostics,
                                       String messageKey, ErrorCode errorCode) throws JavaModelException {
        String name = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.NAME, String.class);
        if (name == null || name.isEmpty()) {
            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri, Messages.getMessage(messageKey),
                                                     range, Constants.DIAGNOSTIC_SOURCE,
                                                     null, errorCode, DiagnosticSeverity.Error));
        }
    }

    /**
     * Validates the annotations (e.g. {@code @TableGenerators}, {@code @SequenceGenerators},
     * {@code @SecondaryTables})
     * <p>
     * Reads the {@code value} member once via {@link DiagnosticUtils#getAnnotationMemberValue}.
     * If the value is absent or an empty array, emits a diagnostic using
     * {@code containerMsgKey}/{@code containerCode} and returns immediately.
     * Otherwise iterates every nested {@link IAnnotation} in the array and validates
     * its {@code name} attribute via {@link #validateNameAttribute}.
     *
     * @param annotation the container annotation to validate
     * @param context the diagnostics context
     * @param uri the document URI, used when creating diagnostics
     * @param diagnostics the mutable list to which any new diagnostics are appended
     * @param emptyMappingMsgKey message key for the empty-array diagnostic
     * @param emptyMappingCode error code for the empty-array diagnostic
     * @param emptyNameMappingMsgKey message key for an empty {@code name} on a nested annotation
     * @param emptyNameMappingCode error code for an empty {@code name} on a nested annotation
     * @throws JavaModelException if the annotation's member value pairs cannot be read
     */
    private void validateNonEmptyMappingArray(IAnnotation annotation, JavaDiagnosticsContext context,
                                              String uri, List<Diagnostic> diagnostics,
                                              String emptyMappingMsgKey, ErrorCode emptyMappingCode,
                                              String emptyNameMappingMsgKey, ErrorCode emptyNameMappingCode) throws JavaModelException {
        Object value = DiagnosticUtils.getAnnotationMemberValue(annotation, "value", Object.class);
        boolean isEmpty = (value == null) || (value instanceof Object[] && ((Object[]) value).length == 0);
        if (isEmpty) {
            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(uri, Messages.getMessage(emptyMappingMsgKey),
                                                     range, Constants.DIAGNOSTIC_SOURCE,
                                                     null, emptyMappingCode, DiagnosticSeverity.Error));
            return;
        }
        Object[] nested = (value instanceof Object[]) ? (Object[]) value : new Object[] { value };
        for (Object obj : nested) {
            if (obj instanceof IAnnotation) {
                validateNameAttribute((IAnnotation) obj, context, uri, diagnostics, emptyNameMappingMsgKey, emptyNameMappingCode);
            }
        }
    }
}
