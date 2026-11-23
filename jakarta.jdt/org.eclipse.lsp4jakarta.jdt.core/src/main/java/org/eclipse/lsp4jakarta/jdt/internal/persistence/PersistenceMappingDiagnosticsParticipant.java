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
*     IBM Corporation - initial implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaErrorCode;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.JDTTypeUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Persistence diagnostic participant that validates mapping-related annotations
 * on entity classes, embedded fields, and getter methods, ensuring that mapping
 * metadata references actual declared fields or properties in the target type.
 *
 * <p>This participant intentionally has no annotation gate — it walks all types
 * in the compilation unit and is designed to accommodate additional mapping
 * annotation validations over time.
 *
 * @see <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0">
 *      Jakarta Persistence 3.0</a>
 */
public class PersistenceMappingDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(PersistenceMappingDiagnosticsParticipant.class.getName());

    // -----------------------------------------------------------------------
    // Message key bundles — one per annotation family
    // -----------------------------------------------------------------------

    /** Message keys and constants for {@code @AttributeOverride} validation. */
    private static final OverrideAnnotationDescriptor ATTRIBUTE_OVERRIDE_DESCRIPTOR = new OverrideAnnotationDescriptor(Constants.ATTRIBUTE_OVERRIDE, Constants.ATTRIBUTE_OVERRIDES, "value", ErrorCode.InvalidAttributeOverrideName, "AttributeOverrideNameNotFound", "AttributeOverrideDotNotationInvalid", "AttributeOverrideMissingMapPrefix", "AttributeOverrideNoMappedSuperclass");

    /** Message keys and constants for {@code @AssociationOverride} validation. */
    private static final OverrideAnnotationDescriptor ASSOCIATION_OVERRIDE_DESCRIPTOR = new OverrideAnnotationDescriptor(Constants.ASSOCIATION_OVERRIDE, Constants.ASSOCIATION_OVERRIDES, "value", ErrorCode.InvalidAssociationOverrideName, "AssociationOverrideNameNotFound", "AssociationOverrideDotNotationInvalid", "AssociationOverrideMissingMapPrefix", "AssociationOverrideNoMappedSuperclass");

    /**
     * Bundles all annotation-family-specific constants needed to drive generic
     * override-name validation. This keeps the validation logic entirely free of
     * annotation-family names.
     */
    private static final class OverrideAnnotationDescriptor {
        final String singleFqn; // e.g. "jakarta.persistence.AttributeOverride"
        final String containerFqn; // e.g. "jakarta.persistence.AttributeOverrides"
        final String containerMember; // container's value element name (always "value")
        final IJavaErrorCode errorCode;
        final String msgNotFound; // message key: simple name not found
        final String msgDotInvalid; // message key: dot-notation segment missing
        final String msgMapPrefix; // message key: missing map key/value prefix
        final String msgNoMappedSuperclass; // message key: no @MappedSuperclass in the hierarchy

        OverrideAnnotationDescriptor(String singleFqn, String containerFqn, String containerMember,
                                     IJavaErrorCode errorCode, String msgNotFound, String msgDotInvalid, String msgMapPrefix,
                                     String msgNoMappedSuperclass) {
            this.singleFqn = singleFqn;
            this.containerFqn = containerFqn;
            this.containerMember = containerMember;
            this.errorCode = errorCode;
            this.msgNotFound = msgNotFound;
            this.msgDotInvalid = msgDotInvalid;
            this.msgMapPrefix = msgMapPrefix;
            this.msgNoMappedSuperclass = msgNoMappedSuperclass;
        }
    }

    /**
     * {@inheritDoc}
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

        for (IType type : unit.getAllTypes()) {
            // 1. Validate class-level override annotations
            validateOverridesOnType(type, unit, ATTRIBUTE_OVERRIDE_DESCRIPTOR, context, diagnostics);
            // Skip name-validation when the class itself is an invalid target.
            if (!validateAssociationOverridePersistenceType(type, unit, context, diagnostics)) {
                validateOverridesOnType(type, unit, ASSOCIATION_OVERRIDE_DESCRIPTOR, context, diagnostics);
            }

            // 2. Validate field-level override annotations
            for (IField field : type.getFields()) {
                validateOverridesOnMember(field, type, unit, ATTRIBUTE_OVERRIDE_DESCRIPTOR, context, diagnostics);
                validateOverridesOnMember(field, type, unit, ASSOCIATION_OVERRIDE_DESCRIPTOR, context, diagnostics);
            }

            // 3. Validate method-level override annotations (property-based access)
            for (IMethod method : type.getMethods()) {
                validateOverridesOnMember(method, type, unit, ATTRIBUTE_OVERRIDE_DESCRIPTOR, context, diagnostics);
                validateOverridesOnMember(method, type, unit, ASSOCIATION_OVERRIDE_DESCRIPTOR, context, diagnostics);
            }
        }

        return diagnostics;
    }

    // -----------------------------------------------------------------------
    // Class-level validation
    // -----------------------------------------------------------------------

    /**
     * Validates that {@code @AssociationOverride} / {@code @AssociationOverrides}
     * placed directly on a type is only used on an {@code @Entity},
     * {@code @MappedSuperclass}, or {@code @Embeddable} class.
     *
     * <p>Emits {@link ErrorCode#AssociationOverrideOnInvalidType} for each
     * offending annotation when the class lacks all three qualifying annotations.
     *
     * @param type the type to validate
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @return {@code true} if the type is an invalid target and at least one
     *         diagnostic was emitted; {@code false} if the type is valid
     * @see <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13942">
     *      Jakarta Persistence 3.0, Section 13.1.1</a>
     */
    private boolean validateAssociationOverridePersistenceType(IType type, ICompilationUnit unit,
                                                               JavaDiagnosticsContext context,
                                                               List<Diagnostic> diagnostics) throws CoreException {
        // If the type has one of the three qualifying annotations it is a valid target.
        for (IAnnotation typeAnnotation : type.getAnnotations()) {

            if (DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotation, Constants.ENTITY)
                || DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotation, Constants.MAPPEDSUPERCLASS)
                || DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotation, Constants.EMBEDDABLE)) {
                return false;
            }
        }

        // Type lacks qualifying annotations — emit a diagnostic for each override annotation present.
        boolean invalidPersistenceTypeFound = false;
        for (IAnnotation typeAnnotation : type.getAnnotations()) {

            boolean isSingle = DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotation, Constants.ASSOCIATION_OVERRIDE);
            boolean isContainer = DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotation, Constants.ASSOCIATION_OVERRIDES);
            if (isSingle || isContainer) {
                String simpleName = DiagnosticUtils.getSimpleName(typeAnnotation.getElementName());
                Range range = PositionUtils.toNameRange(typeAnnotation, context.getUtils());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("AssociationOverrideOnInvalidType", simpleName),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.AssociationOverrideOnInvalidType, DiagnosticSeverity.Error));
                invalidPersistenceTypeFound = true;
            }
        }
        return invalidPersistenceTypeFound;
    }

    /**
     * Validates single and container override annotations placed directly on a type.
     * The {@code name} must resolve against a field declared anywhere in the
     * {@code @MappedSuperclass} supertype chain.
     * For {@code @AssociationOverride}, also checks that {@code joinColumns} and
     * {@code joinTable} are not both specified on the same annotation.
     */
    private void validateOverridesOnType(IType type, ICompilationUnit unit,
                                         OverrideAnnotationDescriptor desc,
                                         JavaDiagnosticsContext context,
                                         List<Diagnostic> diagnostics) throws CoreException {
        boolean isAssociationOverride = Constants.ASSOCIATION_OVERRIDE.equals(desc.singleFqn);
        for (IAnnotation annotation : type.getAnnotations()) {
            if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), desc.singleFqn)) {
                if (isAssociationOverride) {
                    validateAssociationOverrideAttributeConflict(annotation, context, diagnostics);
                }
                String name = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.NAME, String.class);
                if (name != null) {
                    validateNameAgainstSuperclassChain(name, annotation, type, unit, desc, context, diagnostics);
                }
            } else if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), desc.containerFqn)) {
                List<IAnnotation> nestedOverrides = DiagnosticUtils.getNestedAnnotations(annotation, desc.containerMember);
                if (isAssociationOverride) {
                    if (nestedOverrides.isEmpty()) {
                        Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                        diagnostics.add(context.createDiagnostic(context.getUri(),
                                                                 Messages.getMessage("AssociationOverridesEmptyContainer"),
                                                                 range, Constants.DIAGNOSTIC_SOURCE, null,
                                                                 ErrorCode.AssociationOverridesEmptyContainer, DiagnosticSeverity.Error));
                    }

                    validateAssociationOverridesDuplicateNames(nestedOverrides, context, diagnostics);
                }

                for (IAnnotation nested : nestedOverrides) {
                    if (isAssociationOverride) {
                        validateAssociationOverrideAttributeConflict(nested, context, diagnostics);
                    }
                    String name = DiagnosticUtils.getAnnotationMemberValue(nested, Constants.NAME, String.class);
                    if (name != null) {
                        validateNameAgainstSuperclassChain(name, nested, type, unit, desc, context, diagnostics);
                    }
                }
            }
        }
    }

    /**
     * Walks the supertype chain of {@code type} once and checks that {@code fieldName}
     * (dot-notation supported) resolves to a field declared in a {@code @MappedSuperclass}.
     * If it does not, emits a dedicated message when the hierarchy contains no
     * {@code @MappedSuperclass} at all, otherwise the generic "not found" message.
     */
    private void validateNameAgainstSuperclassChain(String fieldName, IAnnotation annotation,
                                                    IType type, ICompilationUnit unit,
                                                    OverrideAnnotationDescriptor desc,
                                                    JavaDiagnosticsContext context,
                                                    List<Diagnostic> diagnostics) throws CoreException {
        try {
            boolean hasMappedSuperclass = false;
            IType current = type;
            while (current != null) {
                String superclassName = current.getSuperclassName();
                if (superclassName == null) {
                    break;
                }
                IType superType = ManagedBean.getChildITypeByName(current, superclassName);
                if (superType == null) {
                    break;
                }
                boolean isMappedSuperclass = DiagnosticUtils.isMatchedAnnotation(
                                                                                 superType.getCompilationUnit(),
                                                                                 superType.getAnnotations(),
                                                                                 Constants.MAPPEDSUPERCLASS);
                if (isMappedSuperclass) {
                    if (fieldExistsInType(superType, fieldName)) {
                        return;
                    }
                    hasMappedSuperclass = true;
                }
                current = superType;
            }

            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            String message = hasMappedSuperclass ? Messages.getMessage(desc.msgNotFound, fieldName, type.getSuperclassName()) : Messages.getMessage(desc.msgNoMappedSuperclass,
                                                                                                                                                    fieldName,
                                                                                                                                                    type.getElementName());
            diagnostics.add(context.createDiagnostic(context.getUri(), message, range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     desc.errorCode, DiagnosticSeverity.Error));
        } catch (JavaModelException e) {
            LOGGER.warning("Error validating override name on type: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Field / method-level validation (property-based access)
    // -----------------------------------------------------------------------

    /**
     * Validates single and container override annotations placed on a field or getter
     * method. Handles {@code @Embedded} and {@code @EmbeddedId} members (resolve against
     * the embeddable type) and map {@code @ElementCollection}, {@code @OneToMany} and
     * {@code @ManyToMany} members (require {@code "key."} or {@code "value."} prefix).
     * Works for both field-based and property-based access.
     *
     * <p>For {@code @AttributeOverride} / {@code @AttributeOverrides}: if the member
     * lacks {@code @Embedded}, {@code @EmbeddedId}, {@code @ElementCollection}, and is not a
     * {@code Map}-typed {@code @OneToMany} / {@code @ManyToMany}, a diagnostic is emitted for
     * each such annotation — the override has no valid target.
     *
     * @see <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html">
     *      Jakarta Persistence 3.0, Section 11.1.4</a>
     */
    private void validateOverridesOnMember(IMember member, IType declaringType,
                                           ICompilationUnit unit,
                                           OverrideAnnotationDescriptor desc,
                                           JavaDiagnosticsContext context,
                                           List<Diagnostic> diagnostics) throws CoreException {
        IAnnotation[] annotations = DiagnosticUtils.getAnnotations(member);
        boolean hasEmbedded = DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.EMBEDDED);
        boolean hasEmbeddedId = DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.EMBEDDEDID);
        boolean hasElementCollection = DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.ELEMENT_COLLECTION);
        boolean hasMapRelationship = (DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.ONE_TO_MANY)
                                      || DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.MANY_TO_MANY))
                                     && isMapTyped(member);
        boolean hasValidTarget = hasEmbedded || hasEmbeddedId || hasElementCollection || hasMapRelationship;

        // Only @AttributeOverride carries the @Embedded/@EmbeddedId/@ElementCollection/Map-relationship
        // restriction at the field/method level.
        boolean isAttributeOverrideDescriptor = Constants.ATTRIBUTE_OVERRIDE.equals(desc.singleFqn);
        boolean isAssociationOverride = Constants.ASSOCIATION_OVERRIDE.equals(desc.singleFqn);

        for (IAnnotation annotation : annotations) {
            boolean isSingleOverride = DiagnosticUtils.isMatchedJavaElement(declaringType, annotation.getElementName(),
                                                                            desc.singleFqn);
            boolean isContainerOverride = DiagnosticUtils.isMatchedJavaElement(declaringType,
                                                                               annotation.getElementName(), desc.containerFqn);

            if (!isSingleOverride && !isContainerOverride) {
                continue;
            }

            if (isAttributeOverrideDescriptor && !hasValidTarget) {
                // @AttributeOverride / @AttributeOverrides on a field or method that is not
                // @Embedded, @EmbeddedId, @ElementCollection, or a Map @OneToMany/@ManyToMany —
                // no valid override target.
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                String annotationSimpleName = DiagnosticUtils.getSimpleName(annotation.getElementName());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("AttributeOverrideOnNonEmbeddedField", annotationSimpleName), range,
                                                         Constants.DIAGNOSTIC_SOURCE, null, ErrorCode.AttributeOverrideOnNonEmbeddedField,
                                                         DiagnosticSeverity.Error));
                // Skip name-resolution: there is no embeddable type to resolve against.
                continue;
            }

            if (isAssociationOverride && !hasEmbedded && !hasElementCollection) {
                // For @AssociationOverride: member has no supported target annotation —
                // nothing to resolve name against.
                continue;
            }

            if (isSingleOverride) {
                if (isAssociationOverride) {
                    validateAssociationOverrideAttributeConflict(annotation, context, diagnostics);
                }
                String name = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.NAME, String.class);
                if (name != null) {
                    validateNameOnMember(name, annotation, member, declaringType,
                                         hasElementCollection || hasMapRelationship, desc, context, diagnostics);
                }
            } else if (isContainerOverride) {
                List<IAnnotation> nestedOverrides = DiagnosticUtils.getNestedAnnotations(annotation,
                                                                                         desc.containerMember);
                if (isAssociationOverride) {
                    if (nestedOverrides.isEmpty()) {
                        Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                        diagnostics.add(context.createDiagnostic(context.getUri(),
                                                                 Messages.getMessage("AssociationOverridesEmptyContainer"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, null, ErrorCode.AssociationOverridesEmptyContainer,
                                                                 DiagnosticSeverity.Error));
                    }
                    validateAssociationOverridesDuplicateNames(nestedOverrides, context, diagnostics);
                }
                for (IAnnotation nested : nestedOverrides) {
                    if (isAssociationOverride) {
                        validateAssociationOverrideAttributeConflict(nested, context, diagnostics);
                    }
                    String name = DiagnosticUtils.getAnnotationMemberValue(nested, Constants.NAME, String.class);
                    if (name != null) {
                        validateNameOnMember(name, nested, member, declaringType,
                                             hasElementCollection || hasMapRelationship, desc, context, diagnostics);
                    }
                }
            }
        }
    }

    /**
     * Returns {@code true} if the resolved type of {@code member} is a {@code java.util.Map}.
     */
    private boolean isMapTyped(IMember member) {
        String typeName = JDTTypeUtils.getResolvedTypeName(member);
        return typeName != null && JDTTypeUtils.isMap(typeName);
    }

    /**
     * Dispatches member-level name validation to the appropriate handler based on
     * whether the member is a map collection ({@code @ElementCollection},
     * {@code @OneToMany} or {@code @ManyToMany}) or a plain {@code @Embedded} /
     * {@code @EmbeddedId}.
     */
    private void validateNameOnMember(String name, IAnnotation annotation, IMember member,
                                      IType declaringType, boolean isMapCollection,
                                      OverrideAnnotationDescriptor desc,
                                      JavaDiagnosticsContext context,
                                      List<Diagnostic> diagnostics) throws CoreException {
        try {
            String typeName = JDTTypeUtils.getResolvedTypeName(member);

            if (isMapCollection && typeName != null && JDTTypeUtils.isMap(typeName)) {
                validateNameOnMapElementCollection(name, annotation, member, declaringType, desc, context, diagnostics);
            } else {
                // @Embedded member — resolve against the embeddable type
                if (typeName != null) {
                    IType embeddableType = JDTTypeUtils.findType(declaringType.getJavaProject(), typeName);
                    if (embeddableType != null) {
                        validateNameAgainstType(name, name, annotation, embeddableType, desc, context, diagnostics);
                    }
                }
            }
        } catch (JavaModelException e) {
            LOGGER.warning("Error validating override name on member: " + e.getMessage());
        }
    }

    /**
     * Validates a name on a map collection member (field or getter method).
     * The name must be prefixed with {@code "key."} or {@code "value."}; the remainder is
     * then resolved against the corresponding map type argument.
     */
    private void validateNameOnMapElementCollection(String name, IAnnotation annotation, IMember member,
                                                    IType declaringType,
                                                    OverrideAnnotationDescriptor desc,
                                                    JavaDiagnosticsContext context,
                                                    List<Diagnostic> diagnostics) throws CoreException {
        if (!name.startsWith(Constants.ATTRIBUTE_OVERRIDE_KEY_PREFIX)
            && !name.startsWith(Constants.ATTRIBUTE_OVERRIDE_VALUE_PREFIX)) {
            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage(desc.msgMapPrefix, name),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     desc.errorCode, DiagnosticSeverity.Error));
            return;
        }

        String[] typeArgs = JDTTypeUtils.getResolvedTypeArguments(member);
        // For map generics we get key and value type pair
        if (typeArgs == null || typeArgs.length < 2) {
            return; // cannot resolve — skip
        }

        String suffix;
        String targetTypeName;
        if (name.startsWith(Constants.ATTRIBUTE_OVERRIDE_KEY_PREFIX)) {
            suffix = name.substring(Constants.ATTRIBUTE_OVERRIDE_KEY_PREFIX.length());
            targetTypeName = typeArgs[0];
        } else {
            suffix = name.substring(Constants.ATTRIBUTE_OVERRIDE_VALUE_PREFIX.length());
            targetTypeName = typeArgs[1];
        }

        IType targetType = JDTTypeUtils.findType(declaringType.getJavaProject(), targetTypeName);
        if (targetType != null) {
            validateNameAgainstType(suffix, suffix, annotation, targetType, desc, context, diagnostics);
        }
    }

    // -----------------------------------------------------------------------
    // Core name resolution
    // -----------------------------------------------------------------------

    /**
     * Validates {@code name} against the declared fields of {@code targetType},
     * supporting dot-notation for nested embeddables.
     *
     * @param fullName the original full name from the annotation (used in error messages)
     * @param name the remaining name segment(s) to resolve against {@code targetType}
     * @param desc the annotation family descriptor supplying message keys and error code
     */
    private void validateNameAgainstType(String fullName, String name, IAnnotation annotation,
                                         IType targetType,
                                         OverrideAnnotationDescriptor desc,
                                         JavaDiagnosticsContext context,
                                         List<Diagnostic> diagnostics) throws CoreException {
        int dotIndex = name.indexOf('.');
        if (dotIndex == -1) {
            // Simple field lookup
            if (!hasField(targetType, name)) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                String message = fullName.equals(name) ? Messages.getMessage(desc.msgNotFound, name,
                                                                             targetType.getElementName()) : Messages.getMessage(desc.msgDotInvalid, fullName, name,
                                                                                                                                targetType.getElementName());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         message, range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         desc.errorCode, DiagnosticSeverity.Error));
            }
        } else {
            // Dot-notation: resolve first segment, recurse on remainder
            String firstSegment = name.substring(0, dotIndex);
            String remainder = name.substring(dotIndex + 1);
            if (!hasField(targetType, firstSegment)) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage(desc.msgDotInvalid, fullName, firstSegment, targetType.getElementName()),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         desc.errorCode, DiagnosticSeverity.Error));
            } else {
                IField nestedField = targetType.getField(firstSegment);
                String nestedTypeName = JDTTypeUtils.getResolvedTypeName(nestedField);
                if (nestedTypeName != null) {
                    IType nestedType = JDTTypeUtils.findType(targetType.getJavaProject(), nestedTypeName);
                    if (nestedType != null) {
                        validateNameAgainstType(fullName, remainder, annotation, nestedType, desc, context, diagnostics);
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Returns true if {@code targetType} declares a field with the given {@code fieldName}.
     */
    private boolean hasField(IType targetType, String fieldName) throws JavaModelException {
        IField field = targetType.getField(fieldName);
        return field != null && field.exists();
    }

    /**
     * Returns true if {@code type} (or a nested embeddable reached via dot-notation)
     * declares a field matching {@code name}.
     */
    private boolean fieldExistsInType(IType type, String fieldName) throws JavaModelException {
        int dotIndex = fieldName.indexOf('.');
        if (dotIndex == Constants.NOT_FOUND) {
            return hasField(type, fieldName);
        }
        String firstSegment = fieldName.substring(0, dotIndex);
        if (!hasField(type, firstSegment)) {
            return false;
        }
        IField nestedField = type.getField(firstSegment);
        String nestedTypeName = JDTTypeUtils.getResolvedTypeName(nestedField);
        if (nestedTypeName == null) {
            return false;
        }
        IType nestedType = JDTTypeUtils.findType(type.getJavaProject(), nestedTypeName);
        return nestedType != null && fieldExistsInType(nestedType, fieldName.substring(dotIndex + 1));
    }

    /**
     * Emits {@link ErrorCode#AssociationOverridesDuplicateName} for each
     * {@code @AssociationOverride} nested inside a container whose {@code name}
     * element duplicates an earlier entry in the same container.
     */
    private void validateAssociationOverridesDuplicateNames(List<IAnnotation> nestedOverrides,
                                                            JavaDiagnosticsContext context,
                                                            List<Diagnostic> diagnostics) throws JavaModelException {
        Set<String> associationOverridesNames = new HashSet<>();
        for (IAnnotation nested : nestedOverrides) {
            String name = DiagnosticUtils.getAnnotationMemberValue(nested, Constants.NAME, String.class);
            // Set.add returns false when the name was already present (a duplicate).
            if (name != null && !associationOverridesNames.add(name)) {
                Range range = PositionUtils.toNameRange(nested, context.getUtils());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("AssociationOverridesDuplicateName", name),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.AssociationOverridesDuplicateName, DiagnosticSeverity.Error));
            }
        }
    }

    /**
     * Emits {@link ErrorCode#AssociationOverrideBothJoinColumnsAndJoinTable} if the
     * given {@code @AssociationOverride} annotation specifies both {@code joinColumns}
     * and {@code joinTable}.
     *
     * @see <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13942">
     *      Jakarta Persistence 3.0, Section 13.1.1</a>
     */
    private void validateAssociationOverrideAttributeConflict(IAnnotation annotation,
                                                              JavaDiagnosticsContext context,
                                                              List<Diagnostic> diagnostics) throws JavaModelException {
        Set<String> memberNames = Arrays.stream(annotation.getMemberValuePairs()).map(IMemberValuePair::getMemberName).collect(Collectors.toSet());

        if (memberNames.contains(Constants.JOIN_COLUMNS_ATTRIBUTE)
            && memberNames.contains(Constants.JOIN_TABLE_ATTRIBUTE)) {
            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("AssociationOverrideBothJoinColumnsAndJoinTable"),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.AssociationOverrideBothJoinColumnsAndJoinTable,
                                                     DiagnosticSeverity.Error));
        }
    }

}
