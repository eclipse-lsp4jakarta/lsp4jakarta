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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Helper class that encapsulates all {@code @IdClass} validation logic for
 * Jakarta Persistence entity diagnostics.
 *
 * <p>Implements the rules from Jakarta Persistence 3.0 spec §2.4 and §2.4.1.1:
 * <ul>
 * <li>Every {@code @Id} field or property in the entity must have a corresponding
 * member in the id class ({@code IdClassMemberMissingInKeyClass}).</li>
 * <li>Corresponding members must have matching types. For relationship
 * {@code @Id} attributes ({@code @ManyToOne} / {@code @OneToOne}), the
 * expected key-class type is the parent entity's PK type, not the
 * relationship type itself (spec §2.4.1.1, third bullet).</li>
 * </ul>
 * </p>
 */
class IdClassHelper {

    /**
     * A finding produced by {@link #validate} — carries the member to highlight,
     * the human-readable message, and the diagnostic code. Diagnostic creation is
     * intentionally left to the caller so that it can use its own
     * {@code JavaDiagnosticsContext.createDiagnostic} infrastructure.
     */
    record Finding(IMember member, String message, ErrorCode code) {}

    /**
     * Validates that the members of an {@code @IdClass} key class correspond to the
     * {@code @Id} fields or properties of the entity, and that their names and types
     * match per Jakarta Persistence spec §2.4 and §2.4.1.1.
     *
     * @param entityType the {@code @Entity} class under inspection
     * @param idClassAnnotation the {@code @IdClass} annotation on the entity
     * @param idMembers the members of the entity annotated with {@code @Id}
     * @return list of findings; empty when validation passes
     * @throws JavaModelException if the Java model cannot be inspected
     */
    List<Finding> validate(IType entityType, IAnnotation idClassAnnotation,
                           List<IMember> idMembers) throws JavaModelException {
        List<Finding> findings = new ArrayList<>();
        IType keyClass = resolveIdClass(entityType, idClassAnnotation);
        if (keyClass == null || idMembers.isEmpty()) {
            return findings;
        }

        // Infer access type: if any @Id is on a field → field-based; otherwise → property-based.
        boolean fieldAccess = idMembers.stream().anyMatch(m -> m instanceof IField);

        // Build name→member and name→expectedType maps for entity @Id members in one pass.
        // For @ManyToOne / @OneToOne @Id members the expected type is the related entity's
        // PK type, not the relationship type (spec §2.4.1.1, third bullet).
        Map<String, IMember> entityIdMembers = new HashMap<>();
        Map<String, String> entityIdMap = new HashMap<>();
        for (IMember member : idMembers) {
            String memberName = fieldAccess ? member.getElementName() : DiagnosticUtils.getterPropertyName(member.getElementName());
            if (memberName != null) {
                entityIdMembers.put(memberName, member);
                entityIdMap.put(memberName, resolveExpectedKeyClassType(member, entityType));
            }
        }

        // Build name→type map for key class members using the same access mode.
        Map<String, String> keyClassMap = new HashMap<>();
        if (fieldAccess) {
            for (IField field : keyClass.getFields()) {
                if (!Flags.isStatic(field.getFlags()) && !Flags.isTransient(field.getFlags())) {
                    keyClassMap.put(field.getElementName(), JDTTypeUtils.getResolvedTypeName(field));
                }
            }
        } else {
            for (IMethod method : keyClass.getMethods()) {
                String propertyName = DiagnosticUtils.getterPropertyName(method);
                if (propertyName != null) {
                    keyClassMap.put(propertyName, JDTTypeUtils.getResolvedResultTypeName(method));
                }
            }
        }

        // Iterate entityIdMap directly — no need to re-derive memberName from idMembers again.
        for (Map.Entry<String, String> entry : entityIdMap.entrySet()) {
            String memberName = entry.getKey();
            IMember idMember = entityIdMembers.get(memberName);
            if (!keyClassMap.containsKey(memberName)) {
                findings.add(new Finding(idMember, Messages.getMessage(ErrorCode.IdClassMemberMissingInKeyClass.name(), memberName), ErrorCode.IdClassMemberMissingInKeyClass));
            } else {
                String entityTypeName = entry.getValue();
                String keyTypeName = keyClassMap.get(memberName);
                if (entityTypeName != null && keyTypeName != null && !entityTypeName.equals(keyTypeName)) {
                    findings.add(new Finding(idMember, Messages.getMessage(ErrorCode.IdClassMemberTypeMismatch.name(),
                                                                           memberName, entityTypeName, keyTypeName), ErrorCode.IdClassMemberTypeMismatch));
                }
            }
        }
        return findings;
    }

    /**
     * Resolves the type that the corresponding key-class member should have for a
     * given entity {@code @Id} member, following Jakarta Persistence spec §2.4.1.1.
     *
     * <ul>
     * <li>For a basic {@code @Id} field or property this is the declared type.</li>
     * <li>For a relationship {@code @Id} ({@code @ManyToOne} / {@code @OneToOne}),
     * spec §2.4.1.1 (third bullet) requires the key-class to hold the <em>related</em>
     * entity's PK type — not the entity type itself:
     * <ul>
     * <li>If the related entity has a simple {@code @Id}, the key-class member type is
     * that field/property type.</li>
     * <li>If the related entity uses {@code @EmbeddedId}, the key-class member type is
     * the embeddable class type of that field/property.</li>
     * <li>If the related entity itself has a composite PK ({@code @IdClass}), the
     * key-class member type is that {@code @IdClass} type.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param member the entity {@code @Id} field or method
     * @param entityType the entity class that owns the member
     * @return the expected key-class type name, or the {@code @Id} field type if unresolvable
     * @throws JavaModelException if the Java model cannot be inspected
     */
    private String resolveExpectedKeyClassType(IMember member, IType entityType) throws JavaModelException {
        String idFieldType = JDTTypeUtils.getResolvedMemberTypeName(member);

        // Only relationship @Id fields need special handling.
        // IField and IMethod both implement IAnnotatable; cast is safe for @Id members.
        ICompilationUnit entityCu = entityType.getCompilationUnit();
        IAnnotation[] memberAnnotations = ((IAnnotatable) member).getAnnotations();
        if (!DiagnosticUtils.isMatchedAnnotation(entityCu, memberAnnotations, Constants.MANY_TO_ONE)
            && !DiagnosticUtils.isMatchedAnnotation(entityCu, memberAnnotations, Constants.ONE_TO_ONE)) {
            return idFieldType;
        }

        if (idFieldType == null) {
            return null;
        }

        // Resolve the related entity type.
        IType idType = entityType.getJavaProject().findType(idFieldType);
        if (idType == null) {
            return idFieldType;
        }

        // Spec §2.4.1.1 third bullet: if the *related* entity itself has a composite PK
        // (@IdClass), the key-class member must use that @IdClass type, not the entity type.
        ICompilationUnit idTypeCu = idType.getCompilationUnit();
        for (IAnnotation annotation : idType.getAnnotations()) {
            if (DiagnosticUtils.isMatchedAnnotation(idTypeCu, annotation, Constants.IDCLASS)) {
                String classLiteral = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.VALUE, String.class);
                if (classLiteral != null) {
                    return ManagedBean.getFullyQualifiedClassName(idType, classLiteral);
                }
            }
        }

        // Simple PK on the related entity: return its @Id or @EmbeddedId field/getter type.
        // @EmbeddedId holds an embeddable class; the key-class member must be of that
        // embeddable type, not the entity type (spec §2.4.1.1, third bullet).
        for (IField field : idType.getFields()) {
            if (DiagnosticUtils.isMatchedAnnotation(idTypeCu, field.getAnnotations(), Constants.ID)
                || DiagnosticUtils.isMatchedAnnotation(idTypeCu, field.getAnnotations(), Constants.EMBEDDEDID)) {
                return JDTTypeUtils.getResolvedTypeName(field);
            }
        }
        for (IMethod method : idType.getMethods()) {
            if (DiagnosticUtils.isMatchedAnnotation(idTypeCu, method.getAnnotations(), Constants.ID)
                || DiagnosticUtils.isMatchedAnnotation(idTypeCu, method.getAnnotations(), Constants.EMBEDDEDID)) {
                return JDTTypeUtils.getResolvedResultTypeName(method);
            }
        }

        return idFieldType;
    }

    /**
     * Resolves the {@link IType} referenced by an {@code @IdClass} annotation using
     * {@link ManagedBean#getChildITypeByName}.
     *
     * @param declaringType the type that carries the {@code @IdClass} annotation
     * @param annotation the {@code @IdClass} annotation
     * @return the resolved key-class {@link IType}, or {@code null} if it cannot be found
     * @throws JavaModelException if the annotation member values cannot be read
     */
    private IType resolveIdClass(IType declaringType, IAnnotation annotation) throws JavaModelException {
        String classLiteral = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.VALUE, String.class);
        if (classLiteral == null) {
            return null;
        }
        return ManagedBean.getChildITypeByName(declaringType, classLiteral);
    }

}
