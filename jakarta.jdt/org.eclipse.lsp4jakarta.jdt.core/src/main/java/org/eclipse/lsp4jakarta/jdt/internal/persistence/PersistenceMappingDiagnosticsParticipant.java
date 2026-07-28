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
import java.util.List;
import java.util.logging.Logger;

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
            // 1. Validate class-level @AttributeOverride / @AttributeOverrides
            validateAttributeOverridesOnType(type, unit, context, diagnostics);

            // 2. Validate field-level @AttributeOverride / @AttributeOverrides
            for (IField field : type.getFields()) {
                validateAttributeOverridesOnMember(field, type, unit, context, diagnostics);
            }

            // 3. Validate method-level @AttributeOverride / @AttributeOverrides (property-based access)
            for (IMethod method : type.getMethods()) {
                validateAttributeOverridesOnMember(method, type, unit, context, diagnostics);
            }
        }

        return diagnostics;
    }

    // -----------------------------------------------------------------------
    // Class-level validation
    // -----------------------------------------------------------------------

    /**
     * Validates {@code @AttributeOverride} / {@code @AttributeOverrides} annotations
     * placed directly on a type. The {@code name} must resolve against a field
     * declared anywhere in the {@code @MappedSuperclass} supertype chain.
     */
    private void validateAttributeOverridesOnType(IType type, ICompilationUnit unit,
                                                  JavaDiagnosticsContext context,
                                                  List<Diagnostic> diagnostics) throws CoreException {
        for (IAnnotation annotation : type.getAnnotations()) {
            if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), Constants.ATTRIBUTE_OVERRIDE)) {
                String name = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.NAME, String.class);
                if (name != null) {
                    validateNameAgainstSuperclassChain(name, annotation, type, context, diagnostics);
                }
            } else if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), Constants.ATTRIBUTE_OVERRIDES)) {
                for (IAnnotation nested : getNestedAttributeOverrides(annotation)) {
                    String name = DiagnosticUtils.getAnnotationMemberValue(nested, Constants.NAME, String.class);
                    if (name != null) {
                        validateNameAgainstSuperclassChain(name, nested, type, context, diagnostics);
                    }
                }
            }
        }
    }

    /**
     * Checks that {@code name} resolves to a declared field somewhere in the
     * {@code @MappedSuperclass} supertype chain of {@code type}.
     */
    private void validateNameAgainstSuperclassChain(String name, IAnnotation annotation,
                                                    IType type,
                                                    JavaDiagnosticsContext context,
                                                    List<Diagnostic> diagnostics) throws CoreException {
        try {
            IType superType = findMappedSuperclassWithField(type, name);
            if (superType == null) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                String targetTypeName = resolveSuperclassChainName(type);
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("AttributeOverrideNameNotFound", name, targetTypeName),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.InvalidAttributeOverrideName, DiagnosticSeverity.Error));
            }
        } catch (JavaModelException e) {
            LOGGER.warning("Error validating @AttributeOverride name on type: " + e.getMessage());
        }
    }

    /**
     * Walks the supertype hierarchy of {@code type} looking for a {@code @MappedSuperclass}
     * that declares a field matching {@code name} (dot-notation resolved recursively).
     * Returns the first matching supertype, or {@code null} if none found.
     */
    private IType findMappedSuperclassWithField(IType type, String name) throws CoreException {
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
            boolean isMappedSuperclass = DiagnosticUtils.isMatchedJavaElement(
                                                                              superType, superType.getElementName().isEmpty() ? superclassName : superType.getElementName(),
                                                                              Constants.MAPPEDSUPERCLASS);
            // fall back to annotation scan when simple name check fails
            if (!isMappedSuperclass) {
                for (IAnnotation annotation : superType.getAnnotations()) {
                    if (DiagnosticUtils.isMatchedJavaElement(superType, annotation.getElementName(), Constants.MAPPEDSUPERCLASS)) {
                        isMappedSuperclass = true;
                        break;
                    }
                }
            }
            if (isMappedSuperclass && fieldExistsInType(superType, name)) {
                return superType;
            }
            current = superType;
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Field / method-level validation (property-based access)
    // -----------------------------------------------------------------------

    /**
     * Validates {@code @AttributeOverride} / {@code @AttributeOverrides} placed on a field
     * or getter method (property-based access). Handles {@code @Embedded} members (resolve
     * against the embeddable type) and map {@code @ElementCollection} members (require
     * {@code "key."} or {@code "value."} prefix).
     */
    private void validateAttributeOverridesOnMember(IMember member, IType declaringType,
                                                    ICompilationUnit unit,
                                                    JavaDiagnosticsContext context,
                                                    List<Diagnostic> diagnostics) throws CoreException {
        IAnnotation[] annotations = DiagnosticUtils.getAnnotations(member);
        boolean hasEmbedded = DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.EMBEDDED);
        boolean hasElementCollection = DiagnosticUtils.isMatchedAnnotation(unit, annotations, Constants.ELEMENT_COLLECTION);

        if (!hasEmbedded && !hasElementCollection) {
            return;
        }

        for (IAnnotation annotation : annotations) {
            if (DiagnosticUtils.isMatchedJavaElement(declaringType, annotation.getElementName(), Constants.ATTRIBUTE_OVERRIDE)) {
                String name = DiagnosticUtils.getAnnotationMemberValue(annotation, Constants.NAME, String.class);
                if (name != null) {
                    validateNameOnMember(name, annotation, member, declaringType, hasElementCollection, context, diagnostics);
                }
            } else if (DiagnosticUtils.isMatchedJavaElement(declaringType, annotation.getElementName(), Constants.ATTRIBUTE_OVERRIDES)) {
                for (IAnnotation nested : getNestedAttributeOverrides(annotation)) {
                    String name = DiagnosticUtils.getAnnotationMemberValue(nested, Constants.NAME, String.class);
                    if (name != null) {
                        validateNameOnMember(name, nested, member, declaringType, hasElementCollection, context, diagnostics);
                    }
                }
            }
        }
    }

    /**
     * Dispatches member-level name validation to the appropriate handler based on
     * whether the member is a map {@code @ElementCollection} or a plain {@code @Embedded}.
     * Works for both {@link IField} (field-based access) and {@link IMethod} (property-based access).
     */
    private void validateNameOnMember(String name, IAnnotation annotation, IMember member,
                                      IType declaringType, boolean isElementCollection,
                                      JavaDiagnosticsContext context,
                                      List<Diagnostic> diagnostics) throws CoreException {
        try {
            String typeName = member instanceof IMethod ? JDTTypeUtils.getResolvedResultTypeName((IMethod) member) : JDTTypeUtils.getResolvedTypeName((IField) member);

            if (isElementCollection && JDTTypeUtils.isMap(typeName)) {
                validateNameOnMapElementCollection(name, annotation, member, declaringType, context, diagnostics);
            } else {
                // @Embedded member — resolve against the embeddable type
                if (typeName != null) {
                    IType embeddableType = JDTTypeUtils.findType(declaringType.getJavaProject(), typeName);
                    if (embeddableType != null) {
                        validateNameAgainstType(name, name, annotation, embeddableType, context, diagnostics);
                    }
                }
            }
        } catch (JavaModelException e) {
            LOGGER.warning("Error validating @AttributeOverride name on member: " + e.getMessage());
        }
    }

    /**
     * Validates a name on a map {@code @ElementCollection} member (field or getter method).
     * The name must be prefixed with {@code "key."} or {@code "value."}; the remainder is
     * then resolved against the corresponding map type argument.
     */
    private void validateNameOnMapElementCollection(String name, IAnnotation annotation, IMember member,
                                                    IType declaringType,
                                                    JavaDiagnosticsContext context,
                                                    List<Diagnostic> diagnostics) throws CoreException {
        if (!name.startsWith(Constants.ATTRIBUTE_OVERRIDE_KEY_PREFIX) && !name.startsWith(Constants.ATTRIBUTE_OVERRIDE_VALUE_PREFIX)) {
            Range range = PositionUtils.toNameRange(annotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("AttributeOverrideMissingMapPrefix", name),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidAttributeOverrideName, DiagnosticSeverity.Error));
            return;
        }

        String[] typeArgs = JDTTypeUtils.getResolvedTypeArguments(member);
        // For map generics we get key and value pairs type
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
            validateNameAgainstType(suffix, suffix, annotation, targetType, context, diagnostics);
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
     */
    private void validateNameAgainstType(String fullName, String name, IAnnotation annotation,
                                         IType targetType,
                                         JavaDiagnosticsContext context,
                                         List<Diagnostic> diagnostics) throws CoreException {
        int dotIndex = name.indexOf('.');
        if (dotIndex == -1) {
            // Simple field lookup — if this is a nested call, use dot-notation error message
            if (!hasField(targetType, name)) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                String message = fullName.equals(name) ? Messages.getMessage("AttributeOverrideNameNotFound", name,
                                                                             targetType.getElementName()) : Messages.getMessage("AttributeOverrideDotNotationInvalid", fullName,
                                                                                                                                name, targetType.getElementName());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         message, range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.InvalidAttributeOverrideName, DiagnosticSeverity.Error));
            }
        } else {
            // Dot-notation: resolve first segment, recurse on remainder
            String firstSegment = name.substring(0, dotIndex);
            String remainder = name.substring(dotIndex + 1);
            if (!hasField(targetType, firstSegment)) {
                Range range = PositionUtils.toNameRange(annotation, context.getUtils());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("AttributeOverrideDotNotationInvalid", fullName, firstSegment, targetType.getElementName()),
                                                         range, Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.InvalidAttributeOverrideName, DiagnosticSeverity.Error));
            } else {
                IField nestedField = targetType.getField(firstSegment);
                String nestedTypeName = JDTTypeUtils.getResolvedTypeName(nestedField);
                if (nestedTypeName != null) {
                    IType nestedType = JDTTypeUtils.findType(targetType.getJavaProject(), nestedTypeName);
                    if (nestedType != null) {
                        validateNameAgainstType(fullName, remainder, annotation, nestedType, context, diagnostics);
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
    private boolean fieldExistsInType(IType type, String name) throws JavaModelException {
        int dotIndex = name.indexOf('.');
        if (dotIndex == -1) {
            return hasField(type, name);
        }
        String firstSegment = name.substring(0, dotIndex);
        if (!hasField(type, firstSegment)) {
            return false;
        }
        IField nestedField = type.getField(firstSegment);
        String nestedTypeName = JDTTypeUtils.getResolvedTypeName(nestedField);
        if (nestedTypeName == null) {
            return false;
        }
        IType nestedType = JDTTypeUtils.findType(type.getJavaProject(), nestedTypeName);
        return nestedType != null && fieldExistsInType(nestedType, name.substring(dotIndex + 1));
    }

    /**
     * Returns a human-readable name for the first {@code @MappedSuperclass} in the
     * supertype chain, falling back to the immediate superclass name.
     */
    private String resolveSuperclassChainName(IType type) throws JavaModelException {
        String superclassName = type.getSuperclassName();
        return superclassName != null ? superclassName : type.getElementName();
    }

    /**
     * Extracts the nested {@code @AttributeOverride} annotations from an
     * {@code @AttributeOverrides} container annotation's {@code value} attribute.
     */
    private List<IAnnotation> getNestedAttributeOverrides(IAnnotation container) throws JavaModelException {
        List<IAnnotation> result = new ArrayList<>();
        for (IMemberValuePair pair : container.getMemberValuePairs()) {
            if ("value".equals(pair.getMemberName())) {
                Object val = pair.getValue();
                if (val instanceof Object[]) {
                    for (Object item : (Object[]) val) {
                        if (item instanceof IAnnotation) {
                            result.add((IAnnotation) item);
                        }
                    }
                } else if (val instanceof IAnnotation) {
                    result.add((IAnnotation) val);
                }
            }
        }
        return result;
    }
}
