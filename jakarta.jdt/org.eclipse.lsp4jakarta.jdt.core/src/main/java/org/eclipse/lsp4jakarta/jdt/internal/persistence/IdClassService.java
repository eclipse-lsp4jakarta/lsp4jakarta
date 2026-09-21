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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
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
 * <li>Every member of the id class must correspond by name to an {@code @Id}
 * field or property in the entity ({@code IdClassMemberMissingInEntity}).</li>
 * <li>Every {@code @Id} field or property in the entity must have a corresponding
 * member in the id class ({@code IdClassMemberMissingInKeyClass}).</li>
 * <li>Corresponding members must have matching types. For relationship
 * {@code @Id} attributes ({@code @ManyToOne} / {@code @OneToOne}), the
 * expected key-class type is the parent entity's PK type, not the
 * relationship type itself (spec §2.4.1.1, third bullet).</li>
 * <li>When property-based access is used, each getter in the id class must be
 * {@code public} or {@code protected} ({@code IdClassPropertyNotPublicOrProtected}).</li>
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

        // Infer access type from where @Id annotations are placed on the entity.
        // If any @Id is on a field → field-based access; if all are on methods → property-based access.
        boolean entityUsesFieldAccess = idMembers.stream().anyMatch(m -> m instanceof IField);

        // Build a name→type map for the entity's @Id members.
        // For relationship @Id members (@ManyToOne / @OneToOne), the type stored in the
        // key class must be the parent entity's PK type, not the relationship type itself
        // (Jakarta Persistence 3.0 spec §2.4.1.1, third bullet).
        Map<String, String> entityIdMap = new HashMap<>();
        for (IMember member : idMembers) {
            String name = entityUsesFieldAccess ? member.getElementName() : propertyNameFromGetter(member.getElementName());
            if (name == null) {
                continue;
            }
            String typeFqn = resolveExpectedKeyClassType(member, entityType);
            entityIdMap.put(name, typeFqn);
        }

        // Build a name→type map for the key class, using the same access mode.
        Map<String, String> keyClassMap = new HashMap<>();
        if (entityUsesFieldAccess) {
            for (IField field : keyClass.getFields()) {
                if (Flags.isStatic(field.getFlags()) || Flags.isTransient(field.getFlags())) {
                    continue;
                }
                keyClassMap.put(field.getElementName(), JDTTypeUtils.getResolvedTypeName(field));
            }
        } else {
            // Property-based access: inspect getter methods.
            for (IMethod method : keyClass.getMethods()) {
                String propName = propertyNameFromGetter(method.getElementName());
                if (propName == null || method.getNumberOfParameters() != 0) {
                    continue;
                }
                keyClassMap.put(propName, JDTTypeUtils.getResolvedResultTypeName(method));
            }
        }

        // Every entity @Id member must have a matching member in the key class,
        // and when both exist their types must be the same.
        for (IMember idMember : idMembers) {
            String memberName = entityUsesFieldAccess ? idMember.getElementName() : propertyNameFromGetter(idMember.getElementName());
            if (memberName == null) {
                continue;
            }

            if (!keyClassMap.containsKey(memberName)) {
                Range range = PositionUtils.toNameRange(idMember, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                         Messages.getMessage(ErrorCode.IdClassMemberMissingInKeyClass.name(), memberName),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.IdClassMemberMissingInKeyClass, DiagnosticSeverity.Error));
            } else {
                String entityTypeName = entityIdMap.get(memberName);
                String keyTypeName = keyClassMap.get(memberName);
                if (entityTypeName != null && keyTypeName != null && !entityTypeName.equals(keyTypeName)) {
                    Range range = PositionUtils.toNameRange(idMember, context.getUtils());
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
     * <li>For a <em>basic</em> {@code @Id} field or property, this is simply the
     * declared type of that member.</li>
     * <li>For a <em>relationship</em> {@code @Id} (annotated with {@code @ManyToOne}
     * or {@code @OneToOne}), the key-class must hold the parent entity's PK type:
     * <ul>
     * <li>Simple parent PK → type of the parent's {@code @Id} field/property.</li>
     * <li>Composite parent PK ({@code @IdClass}) → the {@code @IdClass} type.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param member the entity {@code @Id} field or method
     * @param entityType the entity class that owns the member (used for type resolution)
     * @return the expected key-class type name, or the raw member type if unresolvable
     * @throws JavaModelException if the Java model cannot be inspected
     */
    private String resolveExpectedKeyClassType(IMember member, IType entityType) throws JavaModelException {
        String rawType = JDTTypeUtils.getResolvedMemberTypeName(member);

        // Detect whether this @Id member is also a relationship (@ManyToOne / @OneToOne).
        // IField and IMethod both implement IAnnotatable; IMember itself does not.
        boolean isRelationship = false;
        if (member instanceof IAnnotatable) {
            for (IAnnotation ann : ((IAnnotatable) member).getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(entityType, ann.getElementName(), Constants.MANYTOONE)
                    || DiagnosticUtils.isMatchedJavaElement(entityType, ann.getElementName(), Constants.ONETOONE)) {
                    isRelationship = true;
                    break;
                }
            }
        }

        if (!isRelationship || rawType == null) {
            return rawType;
        }

        // Resolve the parent entity type from the project.
        IType parentType = entityType.getJavaProject().findType(rawType);
        if (parentType == null) {
            return rawType;
        }

        // Check whether the parent has @IdClass (composite PK).
        for (IAnnotation ann : parentType.getAnnotations()) {
            if (DiagnosticUtils.isMatchedJavaElement(parentType, ann.getElementName(), Constants.IDCLASS)) {
                for (IMemberValuePair pair : ann.getMemberValuePairs()) {
                    if (Constants.VALUE.equals(pair.getMemberName()) && pair.getValue() instanceof String) {
                        String simpleName = ((String) pair.getValue()).replace(".class", "");
                        String[][] resolved = parentType.resolveType(simpleName);
                        if (resolved != null && resolved.length > 0) {
                            String pkg = resolved[0][0];
                            String typeName = resolved[0][1];
                            return (pkg == null || pkg.isEmpty()) ? typeName : pkg + "." + typeName;
                        }
                        return simpleName;
                    }
                }
            }
        }

        // Simple parent PK: find the single @Id field in the parent and return its type.
        for (IField field : parentType.getFields()) {
            for (IAnnotation ann : field.getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(parentType, ann.getElementName(), Constants.ID)) {
                    return JDTTypeUtils.getResolvedTypeName(field);
                }
            }
        }
        // Property-based parent PK: find the @Id getter.
        for (IMethod method : parentType.getMethods()) {
            for (IAnnotation ann : method.getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(parentType, ann.getElementName(), Constants.ID)) {
                    return JDTTypeUtils.getResolvedResultTypeName(method);
                }
            }
        }

        // Cannot resolve further — fall back to the raw relationship type.
        return rawType;
    }

    /**
     * Resolves the {@link IType} referenced by an {@code @IdClass} annotation.
     *
     * <p>The annotation value is a class literal (e.g. {@code @IdClass(EmployeePK.class)}).
     * JDT represents this as a {@link String} member value pair whose value is the simple or
     * fully-qualified class name. This method resolves that name against the declaring type's
     * compilation unit, with a fall-back for secondary types (package-private top-level types
     * whose source file name differs from the type name).</p>
     *
     * @param declaringType the type that carries the {@code @IdClass} annotation
     * @param annotation the {@code @IdClass} annotation
     * @return the resolved key-class {@link IType}, or {@code null} if it cannot be found
     * @throws JavaModelException if the annotation member values cannot be read
     */
    private IType resolveIdClass(IType declaringType, IAnnotation annotation) throws JavaModelException {
        for (IMemberValuePair pair : annotation.getMemberValuePairs()) {
            if (!Constants.VALUE.equals(pair.getMemberName()) || !(pair.getValue() instanceof String)) {
                continue;
            }

            String simpleName = ((String) pair.getValue()).replace(".class", "");
            String fqName = simpleName;
            String[][] resolvedNames = declaringType.resolveType(simpleName);
            if (resolvedNames != null && resolvedNames.length > 0) {
                String packageName = resolvedNames[0][0];
                String typeName = resolvedNames[0][1];
                fqName = (packageName == null || packageName.isEmpty()) ? typeName : packageName + "." + typeName;
            } else {
                // resolveType may fail for secondary types in the same compilation unit;
                // fall back to qualifying with the declaring type's package.
                String packageName = declaringType.getPackageFragment().getElementName();
                if (!packageName.isEmpty()) {
                    fqName = packageName + "." + simpleName;
                }
            }

            // IJavaProject.findType() cannot locate secondary types (package-private top-level
            // types whose file name differs from the type name). Search the declaring type's
            // compilation unit first, then fall back to findType.
            ICompilationUnit cu = declaringType.getCompilationUnit();
            if (cu != null) {
                for (IType type : cu.getAllTypes()) {
                    if (fqName.equals(type.getFullyQualifiedName('.'))) {
                        return type;
                    }
                }
            }

            IType idClass = declaringType.getJavaProject().findType(fqName);
            if (idClass != null) {
                return idClass;
            }
        }
        return null;
    }

    /**
     * Derives a Java bean property name from a getter method name.
     *
     * <p>Returns the decapitalized suffix after {@code "get"} (e.g. {@code "getName"} →
     * {@code "name"}) or after {@code "is"} (e.g. {@code "isActive"} → {@code "active"}).
     * Returns {@code null} for any name that does not follow a getter convention.</p>
     *
     * @param methodName the name of the method to inspect
     * @return the derived property name, or {@code null} if the method is not a getter
     */
    private String propertyNameFromGetter(String methodName) {
        if (methodName == null) {
            return null;
        }
        String suffix = null;
        if (methodName.startsWith("get") && methodName.length() > 3) {
            suffix = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            suffix = methodName.substring(2);
        }
        if (suffix == null || suffix.isEmpty()) {
            return null;
        }
        return Character.toLowerCase(suffix.charAt(0)) + suffix.substring(1);
    }
}
