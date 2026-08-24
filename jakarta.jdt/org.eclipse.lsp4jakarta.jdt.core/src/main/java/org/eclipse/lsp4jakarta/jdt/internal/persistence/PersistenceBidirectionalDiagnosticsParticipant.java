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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
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
 * Persistence diagnostic participant that validates bidirectional JPA
 * relationships across entity classes.
 *
 * <p>Two rules are checked using cross-file (project-wide) analysis:
 * <ol>
 * <li>The inverse side of a bidirectional relationship must declare the
 * {@code mappedBy} attribute on its {@code @OneToMany}, {@code @OneToOne},
 * or {@code @ManyToMany} annotation.</li>
 * <li>The inverse side of a relationship must not carry a {@code @JoinTable}
 * annotation.</li>
 * </ol>
 *
 * <p>Cross-file analysis is performed by scanning all source compilation units
 * in the project via {@link DiagnosticUtils#findAnnotatedSourceTypes}, which
 * traverses the JDT project model directly and is always consistent with the
 * current workspace state.
 *
 * <p>Specification reference:
 * https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0
 */
public class PersistenceBidirectionalDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context,
                                               IProgressMonitor monitor) throws CoreException {
        String uri = context.getUri();
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IType[] allTypes = unit.getAllTypes();
        for (IType type : allTypes) {
            // Only process @Entity-annotated classes.
            if (!DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.ENTITY)) {
                continue;
            }

            // Build a project-wide map of all @Entity types keyed by simple name.
            // Built once per entity class to avoid repeated full-project scans.
            Map<String, IType> entityTypeMap = DiagnosticUtils.findAnnotatedSourceTypes(
                                                                                        context.getJavaProject(), Constants.ENTITY);

            // Validate relationship annotations on fields.
            for (IField field : type.getFields()) {
                validateRelationshipMember(field, type, unit, entityTypeMap, context, diagnostics);
            }

            // Validate relationship annotations on property getter methods.
            for (IMethod method : type.getMethods()) {
                validateRelationshipMember(method, type, unit, entityTypeMap, context, diagnostics);
            }
        }

        return diagnostics;
    }

    /**
     * Validates relationship annotations on a single field or method.
     *
     * @param member the field or method to inspect
     * @param declaringType the entity type that owns the member
     * @param unit the compilation unit of the declaring type
     * @param entityTypeMap project-wide map from simple class name to {@link IType}
     * @param context the diagnostics context
     * @param diagnostics the list to append new diagnostics to
     * @throws JavaModelException if the JDT model cannot be accessed
     */
    private void validateRelationshipMember(IMember member, IType declaringType,
                                            ICompilationUnit unit,
                                            Map<String, IType> entityTypeMap,
                                            JavaDiagnosticsContext context,
                                            List<Diagnostic> diagnostics) throws JavaModelException {
        IAnnotation[] annotations;
        if (member instanceof IField) {
            annotations = ((IField) member).getAnnotations();
        } else if (member instanceof IMethod) {
            annotations = ((IMethod) member).getAnnotations();
        } else {
            return;
        }

        // Check for each relationship annotation that supports mappedBy.
        for (String relAnnotationFQ : Constants.INVERSE_CAPABLE_RELATIONSHIP_ANNOTATIONS) {
            IAnnotation relAnnotation = DiagnosticUtils.getMatchedAnnotation(unit, annotations, relAnnotationFQ);
            if (relAnnotation == null) {
                continue;
            }

            String simpleName = DiagnosticUtils.getSimpleName(relAnnotationFQ);
            String mappedByValue = DiagnosticUtils.getAnnotationMemberValue(relAnnotation,
                                                                            Constants.MAPPED_BY, String.class);
            boolean hasMappedBy = mappedByValue != null && !mappedByValue.isEmpty();

            // Determine the target entity type referenced by this relationship.
            IType targetType = resolveTargetEntityType(member, entityTypeMap);

            if (hasMappedBy) {
                // This member is explicitly declared as the inverse side.
                // Rule 2: @JoinTable must not be present on the inverse side.
                if (DiagnosticUtils.getMatchedAnnotation(unit, annotations, Constants.JOIN_TABLE) != null) {
                    Range range = PositionUtils.toNameRange(member, context.getUtils());
                    diagnostics.add(context.createDiagnostic(context.getUri(),
                                                             Messages.getMessage("JoinTableOnInverseSide"),
                                                             range, Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.JoinTableOnInverseSide,
                                                             DiagnosticSeverity.Error));
                }
            } else {
                // No mappedBy — could be unidirectional (valid) or bidirectional without
                // mappedBy (invalid). Rule 1: flag only when the target entity has a
                // back-reference to this entity, proving a bidirectional relationship.
                if (targetType != null && isInverseSideOf(targetType, declaringType, relAnnotationFQ)) {
                    Range range = PositionUtils.toNameRange(member, context.getUtils());
                    diagnostics.add(context.createDiagnostic(context.getUri(),
                                                             Messages.getMessage("InverseSideMissingMappedBy", simpleName),
                                                             range, Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InverseSideMissingMappedBy,
                                                             DiagnosticSeverity.Error));
                }
            }
            // Only one relationship annotation per member is expected — stop after first match.
            break;
        }
    }

    /**
     * Searches the target entity type to determine whether it declares a
     * back-reference field or property that references the declaring type,
     * making the {@code declaringType}'s member the owning side of a
     * bidirectional relationship.
     *
     * <p>The target entity is considered to "back-reference" the declaring type
     * when it has a field or method annotated with the corresponding mirrored
     * relationship annotation (e.g. {@code @ManyToOne} mirrors {@code @OneToMany})
     * whose Java type matches the declaring entity's simple name.
     *
     * @param targetType the other side of the relationship
     * @param declaringType the entity type whose member is being validated
     * @param relAnnotationFQ the fully-qualified relationship annotation on the declaring side
     * @return {@code true} if {@code targetType} has a back-reference to {@code declaringType}
     * @throws JavaModelException if JDT cannot inspect the target type
     */
    private boolean isInverseSideOf(IType targetType, IType declaringType,
                                    String relAnnotationFQ) throws JavaModelException {
        ICompilationUnit targetUnit = targetType.getCompilationUnit();
        if (targetUnit == null) {
            return false;
        }

        // Mirrored relationship annotations for each direction:
        //   @OneToMany  ↔  @ManyToOne
        //   @ManyToMany ↔  @ManyToMany  (self-mirroring)
        //   @OneToOne   ↔  @OneToOne    (self-mirroring)
        //
        // For self-mirroring annotations (@ManyToMany, @OneToOne), a bare match of the
        // annotation on the other side is not sufficient — the other side must also carry
        // a non-empty mappedBy, which is the explicit marker that it is the inverse side.
        // Without this extra check both sides would flag each other as the inverse.
        String mirroredAnnotation = getMirroredAnnotation(relAnnotationFQ);
        boolean requiresMappedByOnTarget = mirroredAnnotation.equals(relAnnotationFQ);
        String declaringSimpleName = declaringType.getElementName();

        for (IField field : targetType.getFields()) {
            IAnnotation annotation = DiagnosticUtils.getMatchedAnnotation(
                                                                          targetUnit, field.getAnnotations(), mirroredAnnotation);
            if (annotation != null) {
                if (requiresMappedByOnTarget) {
                    String mappedByValue = DiagnosticUtils.getAnnotationMemberValue(
                                                                                    annotation, Constants.MAPPED_BY, String.class);
                    if (mappedByValue == null || mappedByValue.isEmpty()) {
                        continue;
                    }
                }
                if (declaringSimpleName.equals(
                                               DiagnosticUtils.getElementTypeSimpleName(field.getTypeSignature()))) {
                    return true;
                }
            }
        }

        for (IMethod method : targetType.getMethods()) {
            IAnnotation annotation = DiagnosticUtils.getMatchedAnnotation(
                                                                          targetUnit, method.getAnnotations(), mirroredAnnotation);
            if (annotation != null) {
                if (requiresMappedByOnTarget) {
                    String mappedByValue = DiagnosticUtils.getAnnotationMemberValue(
                                                                                    annotation, Constants.MAPPED_BY, String.class);
                    if (mappedByValue == null || mappedByValue.isEmpty()) {
                        continue;
                    }
                }
                if (declaringSimpleName.equals(
                                               DiagnosticUtils.getElementTypeSimpleName(method.getReturnType()))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns the mirrored relationship annotation for a given annotation.
     * For {@code @OneToMany} the owning side uses {@code @ManyToOne}, and
     * vice versa. {@code @OneToOne} and {@code @ManyToMany} are self-mirroring.
     *
     * @param relAnnotationFQ the fully-qualified relationship annotation name
     * @return the mirrored fully-qualified annotation name
     */
    private String getMirroredAnnotation(String relAnnotationFQ) {
        if (Constants.ONE_TO_MANY.equals(relAnnotationFQ)) {
            return Constants.MANY_TO_ONE;
        }
        if (Constants.MANY_TO_MANY.equals(relAnnotationFQ)) {
            return Constants.MANY_TO_MANY;
        }
        // ONE_TO_ONE is self-mirroring.
        return relAnnotationFQ;
    }

    /**
     * Resolves the target entity {@link IType} for the given relationship member
     * by extracting the element type simple name from the member's type signature
     * and looking it up in the project-wide entity map.
     *
     * <p>For collection-typed fields ({@code List<Employee>}, {@code Set<Order>}),
     * the simple type argument name is extracted. For single-valued fields the
     * field type name is used directly.
     *
     * @param member the relationship field or method
     * @param entityTypeMap project-wide map from simple name to {@link IType}
     * @return the target {@link IType}, or {@code null} if it cannot be resolved
     */
    private IType resolveTargetEntityType(IMember member, Map<String, IType> entityTypeMap) {
        try {
            String rawTypeSig;
            if (member instanceof IField) {
                rawTypeSig = ((IField) member).getTypeSignature();
            } else if (member instanceof IMethod) {
                rawTypeSig = ((IMethod) member).getReturnType();
            } else {
                return null;
            }
            String simpleName = DiagnosticUtils.getElementTypeSimpleName(rawTypeSig);
            return simpleName != null ? entityTypeMap.get(simpleName) : null;
        } catch (JavaModelException e) {
            return null;
        }
    }
}
