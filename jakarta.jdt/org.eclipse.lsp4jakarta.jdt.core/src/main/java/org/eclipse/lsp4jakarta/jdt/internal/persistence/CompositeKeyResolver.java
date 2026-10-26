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

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;

/**
 * Stateless JPA model-query helpers used by
 * {@link DerivedIdentityDiagnosticsParticipant} to resolve composite-key
 * metadata and relational-entity primary key types.
 *
 * <p>All methods are {@code static}; this class is not instantiated.</p>
 */
final class CompositeKeyResolver {

    private CompositeKeyResolver() {
        // utility class — not instantiated
    }

    /**
     * Holds composite-key classification for an entity. Only the information
     * needed by the {@code @MapsId} checks is kept.
     */
    static final class KeyStrategy {
        /** Resolved {@code @EmbeddedId} class, or {@code null} if none. */
        final IType embeddedIdType;
        /** {@code true} if the entity declares {@code @IdClass}. */
        final boolean hasIdClass;

        KeyStrategy(IType embeddedIdType, boolean hasIdClass) {
            this.embeddedIdType = embeddedIdType;
            this.hasIdClass = hasIdClass;
        }

        boolean isComposite() {
            return embeddedIdType != null || hasIdClass;
        }
    }

    /**
     * Scans the direct members of {@code entityType} to determine whether a
     * composite key is declared and to resolve the {@code @EmbeddedId} class.
     */
    static KeyStrategy resolveKeyStrategy(IType entityType, ICompilationUnit unit) throws JavaModelException {
        IType embeddedIdType = null;

        // Check @EmbeddedId on fields
        for (IField field : entityType.getFields()) {
            if (DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), Constants.EMBEDDEDID)) {
                String fqn = JDTTypeUtils.getResolvedTypeName(field);
                if (fqn != null) {
                    embeddedIdType = JDTTypeUtils.findType(entityType.getJavaProject(), fqn);
                }
                break;
            }
        }

        // Check @EmbeddedId on property getters (if not found on fields)
        if (embeddedIdType == null) {
            for (IMethod method : entityType.getMethods()) {
                if (DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.EMBEDDEDID)) {
                    String fqn = JDTTypeUtils.getResolvedResultTypeName(method);
                    if (fqn != null) {
                        embeddedIdType = JDTTypeUtils.findType(entityType.getJavaProject(), fqn);
                    }
                    break;
                }
            }
        }

        // Check for @IdClass at the type level
        boolean hasIdClass = DiagnosticUtils.isMatchedAnnotation(unit, entityType.getAnnotations(), Constants.IDCLASS);

        return new KeyStrategy(embeddedIdType, hasIdClass);
    }

    /** Holds the resolved relational entity and the FQ type of its primary key. */
    static final class RelationalEntityPk {
        final IType entity;
        final String pkFqType;

        RelationalEntityPk(IType entity, String pkFqType) {
            this.entity = entity;
            this.pkFqType = pkFqType;
        }
    }

    /**
     * Resolves the relational entity referenced by the relationship {@code member}
     * and the FQ type of its primary key. Returns {@code null} if the entity
     * cannot be resolved (library type, unresolvable FQN, or no primary key).
     */
    static RelationalEntityPk resolveRelationalEntityPk(IMember member) throws JavaModelException {
        String relationalEntityFqn = JDTTypeUtils.getResolvedMemberTypeName(member);
        if (relationalEntityFqn == null) {
            return null;
        }
        IType relationalEntity = JDTTypeUtils.findType(member.getJavaProject(), relationalEntityFqn);
        if (relationalEntity == null) {
            return null;
        }
        String pkFqType = resolvePkType(relationalEntity);
        if (pkFqType == null) {
            return null;
        }
        return new RelationalEntityPk(relationalEntity, pkFqType);
    }

    /**
     * Returns the FQ type of the primary key of {@code entityType} as defined by
     * spec §2.4.1.1: the {@code @IdClass} class, else the {@code @EmbeddedId}
     * class, else the type of the simple {@code @Id} field or property.
     * Returns {@code null} if none can be determined.
     */
    static String resolvePkType(IType entityType) throws JavaModelException {
        ICompilationUnit cu = entityType.getCompilationUnit();
        if (cu == null) {
            return null;
        }
        IAnnotation idClass = DiagnosticUtils.getMatchedAnnotation(cu, entityType.getAnnotations(), Constants.IDCLASS);
        if (idClass != null) {
            String literal = DiagnosticUtils.getAnnotationMemberValue(idClass, Constants.VALUE, String.class);
            return literal == null ? null : ManagedBean.getFullyQualifiedClassName(entityType, literal);
        }
        return resolveIdOrEmbeddedIdType(entityType, cu);
    }

    /**
     * Returns the FQ type of the {@code @EmbeddedId} or simple {@code @Id} field or
     * property of {@code entityType}, where {@code cu} is its compilation unit.
     * Returns {@code null} if none can be determined.
     */
    static String resolveIdOrEmbeddedIdType(IType entityType, ICompilationUnit cu) throws JavaModelException {
        for (IField field : entityType.getFields()) {
            if (isIdOrEmbeddedId(cu, field.getAnnotations())) {
                return JDTTypeUtils.getResolvedTypeName(field);
            }
        }
        for (IMethod method : entityType.getMethods()) {
            if (isIdOrEmbeddedId(cu, method.getAnnotations())) {
                return JDTTypeUtils.getResolvedResultTypeName(method);
            }
        }
        return null;
    }

    private static boolean isIdOrEmbeddedId(ICompilationUnit cu, IAnnotation[] annotations) throws JavaModelException {
        return DiagnosticUtils.isMatchedAnnotation(cu, annotations, Constants.ID)
               || DiagnosticUtils.isMatchedAnnotation(cu, annotations, Constants.EMBEDDEDID);
    }

    /**
     * Returns the resolved FQ type of attribute {@code attrName} in
     * {@code embeddableType}, checking fields first (via JDT's direct
     * named-field lookup) then property getters. Returns {@code null} if
     * not found.
     */
    static String resolveAttributeType(IType embeddableType, String attrName) throws JavaModelException {
        if (attrName == null || attrName.isEmpty()) {
            return null;
        }
        // IType.getField(name) is JDT's O(1) named-field lookup — no loop needed.
        IField field = embeddableType.getField(attrName);
        if (field != null && field.exists()) {
            return JDTTypeUtils.getResolvedTypeName(field);
        }
        // Fall back to property-access style: look for a matching no-arg getter (get... or is...).
        for (IMethod method : embeddableType.getMethods()) {
            if (method.getNumberOfParameters() != 0) {
                continue;
            }
            String methodName = method.getElementName();
            String propertyName = null;
            if (methodName.startsWith("get") && methodName.length() > 3) {
                propertyName = Introspector.decapitalize(methodName.substring(3));
            } else if (methodName.startsWith("is") && methodName.length() > 2) {
                propertyName = Introspector.decapitalize(methodName.substring(2));
            }
            if (attrName.equals(propertyName)) {
                return JDTTypeUtils.getResolvedResultTypeName(method);
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if {@code typeA} and {@code typeB} are compatible
     * for JPA identity purposes: identical FQ names, or a primitive/wrapper
     * boxing pair (spec §2.4). Uses {@link Constants#PRIMITIVE_TO_WRAPPER}
     * which maps every primitive name to its FQ wrapper — e.g.
     * {@code "long"} → {@code "java.lang.Long"}.
     */
    static boolean typesCompatible(String typeA, String typeB) {
        if (typeA == null || typeB == null) {
            return true;
        }
        return typeA.equals(typeB)
               || typeB.equals(Constants.PRIMITIVE_TO_WRAPPER.get(typeA))
               || typeA.equals(Constants.PRIMITIVE_TO_WRAPPER.get(typeB));
    }
}
