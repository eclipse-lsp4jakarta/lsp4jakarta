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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
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
 * <li>The inverse side of a relationship must not carry {@code @JoinTable},
 * {@code @JoinColumn}, or {@code @JoinColumns} annotations.</li>
 * </ol>
 *
 * <p>The target entity type is resolved per-member using
 * {@link JDTTypeUtils#getResolvedTypeArguments} (for collection-typed fields)
 * or {@link JDTTypeUtils#getResolvedMemberTypeName} (for single-valued fields),
 * followed by {@link IJavaProject#findType(String)}. This avoids a full
 * project-wide scan and is consistent with the current workspace state.
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
        ICompilationUnit unit = JDTUtilsLSImpl.getInstance().resolveCompilationUnit(context.getUri());
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        IJavaProject javaProject = context.getJavaProject();
        IType[] allTypes = unit.getAllTypes();
        for (IType type : allTypes) {
            // Only process @Entity-annotated classes.
            if (!DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), Constants.ENTITY)) {
                continue;
            }

            // Validate relationship annotations on fields.
            for (IField field : type.getFields()) {
                validateRelationshipMember(field, type, unit, javaProject, context, diagnostics);
            }

            // Validate relationship annotations on property getter methods.
            for (IMethod method : type.getMethods()) {
                validateRelationshipMember(method, type, unit, javaProject, context, diagnostics);
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
     * @param javaProject the Java project used to resolve target entity types
     * @param context the diagnostics context
     * @param diagnostics the list to append new diagnostics to
     * @throws JavaModelException if the JDT model cannot be accessed
     */
    private void validateRelationshipMember(IMember member, IType declaringType,
                                            ICompilationUnit unit,
                                            IJavaProject javaProject,
                                            JavaDiagnosticsContext context,
                                            List<Diagnostic> diagnostics) throws JavaModelException {
        IAnnotation[] annotations = member instanceof IField ? ((IField) member).getAnnotations() : ((IMethod) member).getAnnotations();

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

            if (hasMappedBy) {
                // This member is explicitly declared as the inverse side.
                // Rule 2: owner-only annotations (@JoinTable, @JoinColumn, @JoinColumns)
                // must not be present on the inverse side.
                for (String ownerOnlyAnnotation : Constants.OWNER_ONLY_ANNOTATIONS) {
                    if (DiagnosticUtils.isMatchedAnnotation(unit, annotations, ownerOnlyAnnotation)) {
                        String messageKey = Constants.JOIN_TABLE.equals(ownerOnlyAnnotation) ? "JoinTableOnInverseSide" : "JoinColumnOnInverseSide";
                        ErrorCode errorCode = Constants.JOIN_TABLE.equals(ownerOnlyAnnotation) ? ErrorCode.JoinTableOnInverseSide : ErrorCode.JoinColumnOnInverseSide;
                        Range range = PositionUtils.toNameRange(member, context.getUtils());
                        diagnostics.add(context.createDiagnostic(context.getUri(),
                                                                 Messages.getMessage(messageKey),
                                                                 range, Constants.DIAGNOSTIC_SOURCE, null,
                                                                 errorCode,
                                                                 DiagnosticSeverity.Error));
                    }
                }
            } else {
                // No mappedBy — could be unidirectional (valid) or bidirectional without
                // mappedBy (invalid). Rule 1: flag only when the target entity has a
                // back-reference to this entity, proving a bidirectional relationship.
                IType targetType = resolveTargetEntityType(member, javaProject);
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
            if (hasMirroredBackReference(field, targetUnit, mirroredAnnotation,
                                         requiresMappedByOnTarget, declaringSimpleName)) {
                return true;
            }
        }

        for (IMethod method : targetType.getMethods()) {
            if (hasMirroredBackReference(method, targetUnit, mirroredAnnotation,
                                         requiresMappedByOnTarget, declaringSimpleName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} when {@code member} carries the given {@code mirroredAnnotation}
     * and its declared type matches {@code declaringSimpleName}.
     *
     * <p>When {@code requiresMappedByOnTarget} is {@code true} (self-mirroring annotations
     * such as {@code @OneToOne} and {@code @ManyToMany}), the annotation must also have a
     * non-empty {@code mappedBy} attribute to confirm the member is explicitly the inverse side.
     *
     * @param member the field or method to inspect
     * @param unit the compilation unit that owns {@code member}
     * @param mirroredAnnotation the fully-qualified annotation to look for
     * @param requiresMappedByOnTarget whether a non-empty {@code mappedBy} is required
     * @param declaringSimpleName the simple name of the declaring entity type
     * @return {@code true} if this member is a matching back-reference
     * @throws JavaModelException if the JDT model cannot be accessed
     */
    private boolean hasMirroredBackReference(IMember member, ICompilationUnit unit,
                                             String mirroredAnnotation,
                                             boolean requiresMappedByOnTarget,
                                             String declaringSimpleName) throws JavaModelException {
        IAnnotation[] annotations = member instanceof IField ? ((IField) member).getAnnotations() : ((IMethod) member).getAnnotations();
        IAnnotation annotation = DiagnosticUtils.getMatchedAnnotation(unit, annotations, mirroredAnnotation);
        if (annotation == null) {
            return false;
        }
        if (requiresMappedByOnTarget) {
            String mappedByValue = DiagnosticUtils.getAnnotationMemberValue(
                                                                            annotation, Constants.MAPPED_BY, String.class);
            if (mappedByValue == null || mappedByValue.isEmpty()) {
                return false;
            }
        }
        String typeSignature = member instanceof IField ? ((IField) member).getTypeSignature() : ((IMethod) member).getReturnType();
        return declaringSimpleName.equals(DiagnosticUtils.getElementTypeSimpleName(typeSignature));
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
     * Resolves the target entity {@link IType} for the given relationship member.
     *
     * <p>For collection-typed members ({@code List<Employee>}, {@code Set<Order>}),
     * the first resolved type argument FQN is used. For single-valued members the
     * member's own resolved type FQN is used. The resolved FQN is then looked up
     * via {@link IJavaProject#findType(String)}.
     *
     * <p>Only types annotated with {@code @Entity} are returned; non-entity types
     * (e.g. plain value objects or Java library types) yield {@code null}.
     *
     * @param member the relationship field or method
     * @param javaProject the Java project used to look up types by FQN
     * @return the target entity {@link IType}, or {@code null} if it cannot be resolved
     */
    private IType resolveTargetEntityType(IMember member, IJavaProject javaProject) {
        // For collection-typed fields (List<Employee>, Set<Order>), resolve the first
        // type argument FQN. For single-valued fields, resolve the member type FQN.
        String fqName = null;
        String[] typeArgs = JDTTypeUtils.getResolvedTypeArguments(member);
        if (typeArgs != null && typeArgs.length > 0) {
            fqName = typeArgs[0];
        }
        if (fqName == null) {
            fqName = JDTTypeUtils.getResolvedMemberTypeName(member);
        }
        if (fqName == null) {
            return null;
        }

        IType targetType = JDTTypeUtils.findType(javaProject, fqName);
        if (targetType == null) {
            return null;
        }

        // Only consider @Entity-annotated types to avoid false positives.
        try {
            ICompilationUnit targetUnit = targetType.getCompilationUnit();
            if (targetUnit == null
                || !DiagnosticUtils.isMatchedAnnotation(targetUnit, targetType.getAnnotations(), Constants.ENTITY)) {
                return null;
            }
        } catch (JavaModelException e) {
            return null;
        }

        return targetType;
    }
}
