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

import java.beans.Introspector;
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
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;

/**
 * Service class that encapsulates all {@code @IdClass} validation logic for
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
class IdClassService {

    /**
     * Validates that the members of an {@code @IdClass} key class correspond to the
     * {@code @Id} fields or properties of the entity, and that their names and types
     * match per Jakarta Persistence spec §2.4 and §2.4.1.1.
     *
     * @param entityType the {@code @Entity} class under inspection
     * @param idClassAnnotation the {@code @IdClass} annotation on the entity
     * @param idMembers the members of the entity annotated with {@code @Id}
     * @param context the diagnostics context
     * @param uri the URI of the compilation unit being analysed
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if the Java model cannot be inspected
     */
    void validate(IType entityType, IAnnotation idClassAnnotation,
                  List<IMember> idMembers,
                  JavaDiagnosticsContext context, String uri,
                  List<Diagnostic> diagnostics) throws JavaModelException {
        IType keyClass = resolveIdClass(entityType, idClassAnnotation);
        if (keyClass == null || idMembers.isEmpty()) {
            return;
        }

        // Infer access type: if any @Id is on a field → field-based; otherwise → property-based.
        boolean fieldAccess = idMembers.stream().anyMatch(m -> m instanceof IField);

        // Build name→expectedType map for entity @Id members.
        // For @ManyToOne / @OneToOne @Id members the expected type is the parent entity's
        // PK type, not the relationship type (spec §2.4.1.1, third bullet).
        Map<String, String> entityIdMap = new HashMap<>();
        for (IMember member : idMembers) {
            String name = fieldAccess ? member.getElementName() : getterPropertyName(member.getElementName());
            if (name != null) {
                entityIdMap.put(name, resolveExpectedKeyClassType(member, entityType));
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
                String propName = getterPropertyName(method.getElementName());
                if (propName != null && method.getNumberOfParameters() == 0) {
                    keyClassMap.put(propName, JDTTypeUtils.getResolvedResultTypeName(method));
                }
            }
        }

        // Check every entity @Id member against the key class map.
        for (IMember idMember : idMembers) {
            String memberName = fieldAccess ? idMember.getElementName() : getterPropertyName(idMember.getElementName());
            if (memberName == null) {
                continue;
            }
            Range range = PositionUtils.toNameRange(idMember, context.getUtils());
            if (!keyClassMap.containsKey(memberName)) {
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage(ErrorCode.IdClassMemberMissingInKeyClass.name(), memberName),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.IdClassMemberMissingInKeyClass, DiagnosticSeverity.Error));
            } else {
                String entityTypeName = entityIdMap.get(memberName);
                String keyTypeName = keyClassMap.get(memberName);
                if (entityTypeName != null && keyTypeName != null && !entityTypeName.equals(keyTypeName)) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage(ErrorCode.IdClassMemberTypeMismatch.name(),
                                                                                 memberName, entityTypeName, keyTypeName),
                                                             range, Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.IdClassMemberTypeMismatch, DiagnosticSeverity.Error));
                }
            }
        }
    }

    /**
     * Resolves the type that the corresponding key-class member should have for a
     * given entity {@code @Id} member, following Jakarta Persistence spec §2.4.1.1.
     *
     * <ul>
     * <li>For a basic {@code @Id} field or property this is the declared type.</li>
     * <li>For a relationship {@code @Id} ({@code @ManyToOne} / {@code @OneToOne}),
     * the key-class must hold the parent entity's PK type (simple PK) or
     * {@code @IdClass} type (composite PK).</li>
     * </ul>
     *
     * @param member the entity {@code @Id} field or method
     * @param entityType the entity class that owns the member
     * @return the expected key-class type name, or the raw member type if unresolvable
     * @throws JavaModelException if the Java model cannot be inspected
     */
    private String resolveExpectedKeyClassType(IMember member, IType entityType) throws JavaModelException {
        String rawType = JDTTypeUtils.getResolvedMemberTypeName(member);

        // Only relationship @Id fields need special handling.
        // IField and IMethod both implement IAnnotatable; cast is safe for @Id members.
        ICompilationUnit entityCu = entityType.getCompilationUnit();
        IAnnotation[] memberAnnotations = ((IAnnotatable) member).getAnnotations();
        if (!DiagnosticUtils.isMatchedAnnotation(entityCu, memberAnnotations, Constants.MANYTOONE)
            && !DiagnosticUtils.isMatchedAnnotation(entityCu, memberAnnotations, Constants.ONETOONE)) {
            return rawType;
        }

        if (rawType == null) {
            return null;
        }

        // Resolve the parent entity type.
        IType parentType = entityType.getJavaProject().findType(rawType);
        if (parentType == null) {
            return rawType;
        }

        // If the parent has @IdClass (composite PK), return that class's type.
        ICompilationUnit parentCu = parentType.getCompilationUnit();
        for (IAnnotation ann : parentType.getAnnotations()) {
            if (DiagnosticUtils.isMatchedAnnotation(parentCu, ann, Constants.IDCLASS)) {
                String classLiteral = DiagnosticUtils.getAnnotationMemberValue(ann, Constants.VALUE, String.class);
                if (classLiteral != null) {
                    String simpleName = classLiteral.replace(".class", "");
                    return ManagedBean.getFullyQualifiedClassName(parentType, simpleName);
                }
            }
        }

        // Simple parent PK: return the type of the parent's @Id field or getter.
        for (IField field : parentType.getFields()) {
            if (DiagnosticUtils.isMatchedAnnotation(parentCu, field.getAnnotations(), Constants.ID)) {
                return JDTTypeUtils.getResolvedTypeName(field);
            }
        }
        for (IMethod method : parentType.getMethods()) {
            if (DiagnosticUtils.isMatchedAnnotation(parentCu, method.getAnnotations(), Constants.ID)) {
                return JDTTypeUtils.getResolvedResultTypeName(method);
            }
        }

        return rawType;
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
        return ManagedBean.getChildITypeByName(declaringType, classLiteral.replace(".class", ""));
    }

    /**
     * Derives a Java bean property name from a getter method name using
     * {@link Introspector#decapitalize}, consistent with the approach in
     * {@link PersistenceMapKeyDiagnosticsParticipant}.
     * Returns {@code null} if the method name does not follow getter conventions.
     *
     * @param methodName the getter method name (e.g. {@code "getName"}, {@code "isActive"})
     * @return the property name (e.g. {@code "name"}, {@code "active"}), or {@code null}
     */
    private String getterPropertyName(String methodName) {
        if (methodName == null) {
            return null;
        }
        if (methodName.startsWith("get") && methodName.length() > 3) {
            return Introspector.decapitalize(methodName.substring(3));
        }
        if (methodName.startsWith("is") && methodName.length() > 2) {
            return Introspector.decapitalize(methodName.substring(2));
        }
        return null;
    }
}
