/*******************************************************************************
* Copyright (c) 2020, 2026 IBM Corporation, Ankush Sharma and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0.
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     IBM Corporation, Ankush Sharma - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4jakarta.jdt.internal.persistence;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;

/**
 * @author ankushsharma
 * @brief Diagnostics implementation for Jakarta Persistence 3.0
 */

// Imports
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
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
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Persistence diagnostic participant that manages the use of @Entity
 * annotations.
 */
public class PersistenceEntityDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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

        IType[] alltypes;
        IAnnotation[] allAnnotations;

        alltypes = unit.getAllTypes();
        for (IType type : alltypes) {
            allAnnotations = type.getAnnotations();

            IAnnotation EntityAnnotation = null;
            for (IAnnotation annotation : allAnnotations) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                         Constants.ENTITY)) {
                    EntityAnnotation = annotation;
                }
            }

            if (EntityAnnotation != null) {
                // Define boolean requirements for the diagnostics
                boolean hasPublicOrProtectedNoArgConstructor = false;
                boolean hasArgConstructor = false;
                boolean isEntityClassFinal = false;

                // Validate @Version annotations
                validateVersionAnnotations(type, diagnostics, context);

                // Get the Methods of the annotated Class
                for (IMethod method : type.getMethods()) {
                    if (DiagnosticUtils.isConstructorMethod(method)) {
                        // We have found a method that is a constructor
                        if (method.getNumberOfParameters() > 0) {
                            hasArgConstructor = true;
                            continue;
                        }
                        // Don't need to perform subtractions to check flags because eclipse notifies on
                        // illegal constructor modifiers
                        if (method.getFlags() != Flags.AccPublic && method.getFlags() != Flags.AccProtected)
                            continue;
                        hasPublicOrProtectedNoArgConstructor = true;
                    }
                    // All Methods of this class should not be final
                    if (isFinal(method.getFlags())) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("EntityNoFinalMethods"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, method.getElementType(),
                                                                 ErrorCode.InvalidFinalMethodInEntityAnnotatedClass, DiagnosticSeverity.Error));
                    }

                    validatePKDateTemporal(type, method, diagnostics, context);
                }

                // Go through the instance variables and make sure no instance vars are final
                for (IField field : type.getFields()) {
                    // If a field is static, we do not care about it, we care about all other field
                    if (isStatic(field.getFlags())) {
                        continue;
                    }
                    // If we find a non-static variable that is final, this is a problem
                    if (isFinal(field.getFlags())) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("EntityNoFinalVariables"), range,
                                                                 Constants.DIAGNOSTIC_SOURCE, field.getElementType(),
                                                                 ErrorCode.InvalidPersistentFieldInEntityAnnotatedClass, DiagnosticSeverity.Error));
                    }

                    validatePKDateTemporal(type, field, diagnostics, context);
                }

                // Ensure that the Entity class is not given a final modifier
                if (isFinal(type.getFlags()))
                    isEntityClassFinal = true;

                // Create Diagnostics if needed
                if (!hasPublicOrProtectedNoArgConstructor && hasArgConstructor) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("EntityNoArgConstructor"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidConstructorInEntityAnnotatedClass, DiagnosticSeverity.Error));

                }

                if (isEntityClassFinal) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("EntityNoFinalClass"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, type.getElementType(),
                                                             ErrorCode.InvalidFinalModifierOnEntityAnnotatedClass, DiagnosticSeverity.Error));
                }
            }
        }

        return diagnostics;
    }

    /**
     * Check the annotation value is TemporalType.DATE Enum
     *
     * @param pair
     * @return
     */
    private boolean isValidTemporalDateValue(IMemberValuePair pair) {
        if (pair == null) {
            return false;
        }

        String memberName = pair.getMemberName();
        Object value = pair.getValue();
        int valueKind = pair.getValueKind();

        return "value".equals(memberName)
               && valueKind == IMemberValuePair.K_QUALIFIED_NAME
               && value instanceof String
               && Constants.TEMPORAL_TYPE_DATE.equals((String) value);
    }

    /**
     * Check @Temporal annotation exist for primary key field/property with @Id annotation
     * Specification: https://jakarta.ee/specifications/persistence/3.2/jakarta-persistence-spec-3.2#a132
     *
     * @param type
     * @param member
     * @param diagnostics
     * @param context
     * @throws JavaModelException
     */
    private void validatePKDateTemporal(IType type, IMember member, List<Diagnostic> diagnostics,
                                        JavaDiagnosticsContext context) throws JavaModelException {
        IAnnotation[] allAnnotations = null;
        IAnnotation id = null, temporal = null;
        String typeFQ = null;
        Range range = null;

        if (member instanceof IMethod) {
            allAnnotations = ((IMethod) member).getAnnotations();
            typeFQ = JDTTypeUtils.getResolvedResultTypeName((IMethod) member);
            range = PositionUtils.toNameRange((IMethod) member, context.getUtils());
        } else if (member instanceof IField) {
            allAnnotations = ((IField) member).getAnnotations();
            typeFQ = JDTTypeUtils.getResolvedTypeName((IField) member);
            range = PositionUtils.toNameRange((IField) member, context.getUtils());
        }

        for (IAnnotation annotation : allAnnotations) {
            String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                 annotation.getElementName(),
                                                                                 Constants.SET_OF_PRIMARY_KEY_DATE_ANNOTATIONS);
            if (matchedAnnotation != null) {
                if (matchedAnnotation.equals(Constants.ID)) {
                    id = annotation;
                } else if (matchedAnnotation.equals(Constants.TEMPORAL)) {
                    temporal = annotation;
                }
            }
        }

        if (id != null) {
            if (typeFQ.equals(Constants.UTIL_DATE)) {
                if (temporal != null) {
                    // Check value
                    IMemberValuePair[] memberValuePairs = temporal.getMemberValuePairs();
                    for (IMemberValuePair pair : memberValuePairs) {
                        if (!isValidTemporalDateValue(pair)) {
                            // Add diagnostics for invalid type
                            range = PositionUtils.toNameRange(temporal, context.getUtils());
                            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                                     Messages.getMessage("InvalidValueInTemporalAnnotation"), range,
                                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                                     ErrorCode.InvalidValueInTemporalAnnotation, DiagnosticSeverity.Error));
                        }
                    }
                } else {
                    // Add diagnostics for missing annotation
                    diagnostics.add(context.createDiagnostic(context.getUri(),
                                                             Messages.getMessage("MissingTemporalAnnotation"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.MissingTemporalAnnotation, DiagnosticSeverity.Error));
                }
            }
        }
    }

    /**
     * check if the modifier provided is static
     *
     * @param flag
     * @return
     * @note modifier flags are an addition of all flags combined
     */
    private boolean isStatic(int flag) {
        // If a field is static, we do not care about it, we care about all other field
        Integer isPublicStatic = flag - Flags.AccPublic;
        Integer isPrivateStatic = flag - Flags.AccPrivate;
        Integer isFinalStatic = flag - Flags.AccFinal;
        Integer isProtectedStatic = flag - Flags.AccProtected;
        Integer isStatic = flag;
        if (isPublicStatic.equals(Flags.AccStatic) || isPrivateStatic.equals(Flags.AccStatic)
            || isStatic.equals(Flags.AccStatic) || isFinalStatic.equals(Flags.AccStatic)
            || isProtectedStatic.equals(Flags.AccStatic)) {
            return true;
        }
        return false;
    }

    /**
     * check if the modifier provided is final
     *
     * @param flag
     * @return
     * @note modifier flags are an addition of all flags combined
     */
    private boolean isFinal(int flag) {
        Integer isPublicFinal = flag - Flags.AccPublic;
        Integer isPrivateFinal = flag - Flags.AccPrivate;
        Integer isProtectedFinal = flag - Flags.AccProtected;
        Integer isFinal = flag;
        if (isPublicFinal.equals(Flags.AccFinal) || isPrivateFinal.equals(Flags.AccFinal)
            || isProtectedFinal.equals(Flags.AccFinal) || isFinal.equals(Flags.AccFinal)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if any annotation in the given array is a @Version annotation.
     *
     * @param type the type containing the annotations
     * @param annotations the array of annotations to check
     * @return true if any annotation is @Version, false otherwise
     * @throws JavaModelException
     */
    private boolean hasVersionAnnotation(IType type, IAnnotation[] annotations) throws JavaModelException {
        for (IAnnotation annotation : annotations) {
            if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), Constants.VERSION)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates @Version annotations on entity class.
     * Checks for:
     * 1. Multiple @Version annotations within the same class
     * 2. @Version annotation in both parent and child entity classes
     *
     * @param type the entity class type
     * @param diagnostics list to add diagnostics to
     * @param context the diagnostics context
     * @throws JavaModelException
     */
    private void validateVersionAnnotations(IType type, List<Diagnostic> diagnostics,
                                            JavaDiagnosticsContext context) throws JavaModelException {
        List<IMember> versionMembers = new ArrayList<>();

        // Check fields for @Version annotation
        for (IField field : type.getFields()) {
            if (hasVersionAnnotation(type, field.getAnnotations())) {
                versionMembers.add(field);
            }
        }

        // Check methods for @Version annotation
        for (IMethod method : type.getMethods()) {
            if (hasVersionAnnotation(type, method.getAnnotations())) {
                versionMembers.add(method);
            }
        }

        // Check for duplicate @Version in the same class
        if (versionMembers.size() > 1) {
            for (IMember member : versionMembers) {
                Range range = PositionUtils.toNameRange(member, context.getUtils());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("DuplicateVersionAnnotation"), range,
                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.DuplicateVersionAnnotationInClass, DiagnosticSeverity.Error));
            }
        }

        // Check for @Version in parent entity classes
        if (versionMembers.size() > 0 && hasVersionInParentEntity(type)) {
            for (IMember member : versionMembers) {
                Range range = PositionUtils.toNameRange(member, context.getUtils());
                diagnostics.add(context.createDiagnostic(context.getUri(),
                                                         Messages.getMessage("VersionAnnotationInHierarchy"), range,
                                                         Constants.DIAGNOSTIC_SOURCE, null,
                                                         ErrorCode.DuplicateVersionAnnotationInHierarchy, DiagnosticSeverity.Error));
            }
        }
    }

    /**
     * Checks if any parent entity class has a @Version annotation.
     *
     * @param type the current entity class type
     * @return true if a parent entity has @Version annotation, false otherwise
     * @throws JavaModelException
     */
    private boolean hasVersionInParentEntity(IType type) throws JavaModelException {
        ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
        IType superclass = hierarchy.getSuperclass(type);

        while (superclass != null && !superclass.getFullyQualifiedName().equals(Constants.OBJECT)) {
            // Check if parent class is an entity
            boolean isEntity = false;
            for (IAnnotation annotation : superclass.getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(superclass, annotation.getElementName(), Constants.ENTITY)) {
                    isEntity = true;
                    break;
                }
            }

            if (isEntity) {
                // Check if parent entity has @Version annotation on fields
                for (IField field : superclass.getFields()) {
                    if (hasVersionAnnotation(superclass, field.getAnnotations())) {
                        return true;
                    }
                }

                // Check if parent entity has @Version annotation on methods
                for (IMethod method : superclass.getMethods()) {
                    if (hasVersionAnnotation(superclass, method.getAnnotations())) {
                        return true;
                    }
                }
            }

            superclass = hierarchy.getSuperclass(superclass);
        }

        return false;
    }

}
