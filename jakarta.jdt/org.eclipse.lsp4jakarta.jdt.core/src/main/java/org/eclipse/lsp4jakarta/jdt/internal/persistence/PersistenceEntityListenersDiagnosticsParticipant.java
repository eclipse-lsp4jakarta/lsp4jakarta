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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.helpers.ConstructorInfoDiagnosticHelper;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.java.ManagedBean;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Persistence diagnostic participant that validates entity listeners registered
 * using the {@code @EntityListeners} annotation.
 */
public class PersistenceEntityListenersDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(PersistenceEntityListenersDiagnosticsParticipant.class.getName());

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

        IType[] allTypes = unit.getAllTypes();
        for (IType type : allTypes) {
            try {
                collectEntityListenersDiagnostics(type, uri, context, diagnostics);
            } catch (JavaModelException e) {
                LOGGER.log(Level.SEVERE, "Error while collecting @EntityListeners diagnostics for type: " + type.getElementName(), e);
            }
        }

        return diagnostics;
    }

    /**
     * Collects diagnostics for entity listeners on the given type.
     *
     * @param type the type being analyzed
     * @param uri the document URI
     * @param context the Java diagnostics context
     * @param diagnostics the list of diagnostics to collect into
     * @throws JavaModelException if an error occurs reading the Java model
     */
    private void collectEntityListenersDiagnostics(IType type, String uri, JavaDiagnosticsContext context,
                                                   List<Diagnostic> diagnostics) throws JavaModelException {
        IAnnotation[] annotations = type.getAnnotations();
        for (IAnnotation annotation : annotations) {
            if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(), Constants.ENTITY_LISTENERS)) {
                validateEntityListeners(annotation, type, uri, context, diagnostics);
            }
        }
    }

    /**
     * Validates the listeners defined in the {@code @EntityListeners} annotation.
     *
     * @param annotation the {@code @EntityListeners} annotation
     * @param declaringType the type declaring the annotation
     * @param uri the document URI
     * @param context the Java diagnostics context
     * @param diagnostics the list of diagnostics to collect into
     * @throws JavaModelException if an error occurs reading the Java model
     */
    private void validateEntityListeners(IAnnotation annotation, IType declaringType, String uri,
                                         JavaDiagnosticsContext context, List<Diagnostic> diagnostics) throws JavaModelException {
        IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
        Set<IType> validatedListenerTypes = new HashSet<>();
        List<String> nonInstantiableListenerNames = new ArrayList<>();
        List<String> invalidConstructorListenerNames = new ArrayList<>();

        for (IMemberValuePair pair : memberValuePairs) {
            if (Constants.VALUE.equals(pair.getMemberName()) || pair.getMemberName() == null) {
                Object value = pair.getValue();
                if (value instanceof Object[]) {
                    for (Object item : (Object[]) value) {
                        checkListener(item, declaringType, validatedListenerTypes, nonInstantiableListenerNames, invalidConstructorListenerNames);
                    }
                } else if (value != null) {
                    checkListener(value, declaringType, validatedListenerTypes, nonInstantiableListenerNames, invalidConstructorListenerNames);
                }
            }
        }

        Range range = PositionUtils.toNameRange(annotation, context.getUtils());
        if (!nonInstantiableListenerNames.isEmpty()) {
            String classNames = String.join(", ", nonInstantiableListenerNames);
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("EntityListenerMustBeInstantiable", classNames),
                                                     range,
                                                     Constants.DIAGNOSTIC_SOURCE,
                                                     null,
                                                     ErrorCode.InvalidEntityListenerType,
                                                     DiagnosticSeverity.Error));
        }

        if (!invalidConstructorListenerNames.isEmpty()) {
            String classNames = String.join(", ", invalidConstructorListenerNames);
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("EntityListenerNoArgConstructor", classNames),
                                                     range,
                                                     Constants.DIAGNOSTIC_SOURCE,
                                                     null,
                                                     ErrorCode.InvalidConstructorInEntityListener,
                                                     DiagnosticSeverity.Error));
        }
    }

    /**
     * Resolves the listener type from the annotation value object and validates it.
     *
     * @param value the annotation value object (e.g. String representing simple or qualified class name)
     * @param declaringType the declaring type
     * @param validatedListenerTypes set of already checked listener types in this annotation
     * @param nonInstantiableListenerNames list of listener class names that cannot be instantiated (abstract, interface, inner, anonymous, local)
     * @param invalidConstructorListenerNames list of listener class names missing a public no-arg constructor
     * @throws JavaModelException if an error occurs reading the Java model
     */
    private void checkListener(Object value, IType declaringType,
                               Set<IType> validatedListenerTypes,
                               List<String> nonInstantiableListenerNames,
                               List<String> invalidConstructorListenerNames) throws JavaModelException {
        if (!(value instanceof String)) {
            return;
        }

        String className = (String) value;
        IType listenerType = ManagedBean.getChildITypeByName(declaringType, className);

        if (listenerType == null || !listenerType.exists() || validatedListenerTypes.contains(listenerType)) {
            return;
        }

        validatedListenerTypes.add(listenerType);
        if (isNonInstantiableType(listenerType)) {
            nonInstantiableListenerNames.add(listenerType.getElementName());
        } else if (isMissingPublicNoArgsConstructor(listenerType)) {
            invalidConstructorListenerNames.add(listenerType.getElementName());
        }
    }

    /**
     * Checks if the entity listener type is non-instantiable (abstract class, interface, non-static inner class, anonymous class, or local class).
     *
     * @param listenerType the listener type to check
     * @return {@code true} if non-instantiable, {@code false} otherwise
     * @throws JavaModelException if an error occurs reading the Java model
     */
    private boolean isNonInstantiableType(IType listenerType) throws JavaModelException {
        return ManagedBean.isAbstractClass(listenerType) || listenerType.isInterface()
               || ManagedBean.isInnerClass(listenerType) || listenerType.isAnonymous() || listenerType.isLocal();
    }

    /**
     * Checks if the entity listener is missing a public no-argument constructor.
     *
     * @param listenerType the listener type to check
     * @return {@code true} if the listener is missing a public no-arg constructor, {@code false} otherwise
     * @throws JavaModelException if an error occurs reading the Java model
     */
    private boolean isMissingPublicNoArgsConstructor(IType listenerType) throws JavaModelException {
        ConstructorInfoDiagnosticHelper constructorInfo = ConstructorInfoDiagnosticHelper.getConstructorInfo(listenerType);

        // An entity listener must have a public no-arg constructor.
        // If it declares no constructors, the compiler creates a default public constructor.
        // If it declares constructor(s), at least one must be a valid public no-arg constructor.
        return constructorInfo.hasConstructor() && !constructorInfo.hasValidPublicNoArgsConstructor();
    }
}
