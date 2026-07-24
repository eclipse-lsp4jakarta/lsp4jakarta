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

import static org.eclipse.lsp4jakarta.jdt.internal.cdi.Constants.SCOPE_FQ_NAMES;
import static org.eclipse.lsp4jakarta.jdt.internal.ejb.Constants.SESSION_BEAN_ANNOTATIONS;
import static org.eclipse.lsp4jakarta.jdt.internal.ejb.Constants.STATEFUL_FQ_NAME;
import static org.eclipse.lsp4jakarta.jdt.internal.servlet.Constants.HTTP_SERVLET_FQ_NAME;
import static org.eclipse.lsp4jakarta.jdt.internal.servlet.Constants.WEBFILTER_FQ_NAME;
import static org.eclipse.lsp4jakarta.jdt.internal.servlet.Constants.WEB_LISTENER_FQ_NAME;
import static org.eclipse.lsp4jakarta.jdt.internal.servlet.Constants.WEB_SERVLET_FQ_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
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
import org.eclipse.lsp4jakarta.jdt.core.utils.TypeHierarchyUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Persistence diagnostic participant for {@code @PersistenceContext} injection rules.
 *
 * <p>Two rules are enforced:
 * <ol>
 * <li>{@code PersistenceContextNotInManagedComponent}: {@code @PersistenceContext} is used
 * in a class that is not a container-managed component (CDI bean, EJB, or Servlet).
 * Spec §7.6:
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11791">a11791</a>
 * </li>
 * <li>{@code ExtendedPersistenceContextInNonStatefulBean}: {@code PersistenceContextType.EXTENDED}
 * is used outside a {@code @Stateful} EJB.
 * Spec §7.6.3:
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0#a11810">a11810</a>
 * </li>
 * </ol>
 */
public class PersistenceContextDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
            // Check @PersistenceContext on the type itself
            IAnnotation typeAnnotation = findPersistenceContextAnnotation(unit, type.getAnnotations());
            if (typeAnnotation != null) {
                checkAnnotation(context, uri, unit, type, PositionUtils.toNameRange(type, context.getUtils()),
                                typeAnnotation, diagnostics);
            }

            // Check @PersistenceContext on fields
            for (IField field : type.getFields()) {
                IAnnotation fieldAnnotation = findPersistenceContextAnnotation(unit, field.getAnnotations());
                if (fieldAnnotation != null) {
                    checkAnnotation(context, uri, unit, type, PositionUtils.toNameRange(field, context.getUtils()),
                                    fieldAnnotation, diagnostics);
                }
            }

            // Check @PersistenceContext on methods
            for (IMethod method : type.getMethods()) {
                IAnnotation methodAnnotation = findPersistenceContextAnnotation(unit, method.getAnnotations());
                if (methodAnnotation != null) {
                    checkAnnotation(context, uri, unit, type, PositionUtils.toNameRange(method, context.getUtils()),
                                    methodAnnotation, diagnostics);
                }
            }
        }
        return diagnostics;
    }

    // -------------------------------------------------------------------------
    // Private helpers — delegating to existing shared infrastructure
    // -------------------------------------------------------------------------

    /**
     * Applies both diagnostic rules for a single {@code @PersistenceContext} occurrence.
     */
    private void checkAnnotation(JavaDiagnosticsContext context, String uri, ICompilationUnit unit, IType type,
                                 Range range, IAnnotation pcAnnotation, List<Diagnostic> diagnostics) throws CoreException {
        // Rule 1: the enclosing class must be a container-managed component.
        if (!isManagedComponent(unit, type)) {
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("PersistenceContextNotInManagedComponent"),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.PersistenceContextNotInManagedComponent, DiagnosticSeverity.Error));
            return;
        }

        // Rule 2: EXTENDED is only valid in a @Stateful EJB.
        if (isExtendedContext(pcAnnotation)
            && !DiagnosticUtils.isMatchedAnnotation(unit, type.getAnnotations(), STATEFUL_FQ_NAME)) {
            diagnostics.add(context.createDiagnostic(uri,
                                                     Messages.getMessage("ExtendedPersistenceContextInNonStatefulBean"),
                                                     range, Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.ExtendedPersistenceContextInNonStatefulBean, DiagnosticSeverity.Error));
        }
    }

    /** Returns the {@code @PersistenceContext} annotation from the given array, or {@code null}. */
    private IAnnotation findPersistenceContextAnnotation(ICompilationUnit unit, IAnnotation[] annotations) throws JavaModelException {
        return Arrays.stream(annotations).filter(a -> {
            try {
                return DiagnosticUtils.isMatchedAnnotation(unit, a, Constants.PERSISTENCE_CONTEXT);
            } catch (JavaModelException e) {
                return false;
            }
        }).findFirst().orElse(null);
    }

    /**
     * Returns {@code true} when the class is a container-managed component:
     * <ul>
     * <li>CDI bean: any scope from {@code cdi.Constants.SCOPE_FQ_NAMES}</li>
     * <li>EJB session bean: any annotation from {@code ejb.Constants.SESSION_BEAN_ANNOTATIONS}</li>
     * <li>Servlet component: {@code @WebServlet}, {@code @WebFilter}, or {@code @WebListener}</li>
     * <li>Inherits from {@code HttpServlet} (checked via {@link TypeHierarchyUtils})</li>
     * </ul>
     */
    private boolean isManagedComponent(ICompilationUnit unit, IType type) throws CoreException {
        IAnnotation[] typeAnnotations = type.getAnnotations();

        // CDI scopes
        for (String scope : SCOPE_FQ_NAMES) {
            if (DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotations, scope)) {
                return true;
            }
        }

        // EJB session beans
        for (String ejbFqn : SESSION_BEAN_ANNOTATIONS) {
            if (DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotations, ejbFqn)) {
                return true;
            }
        }

        // Servlet component annotations
        if (DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotations, WEB_SERVLET_FQ_NAME)
            || DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotations, WEBFILTER_FQ_NAME)
            || DiagnosticUtils.isMatchedAnnotation(unit, typeAnnotations, WEB_LISTENER_FQ_NAME)) {
            return true;
        }

        // Subclass of HttpServlet
        return TypeHierarchyUtils.doesITypeHaveSuperType(type, HTTP_SERVLET_FQ_NAME) == TypeHierarchyUtils.HAS_SUPERTYPE;
    }

    /**
     * Returns {@code true} if the {@code @PersistenceContext} annotation explicitly
     * sets {@code type = PersistenceContextType.EXTENDED}.
     */
    private boolean isExtendedContext(IAnnotation pcAnnotation) throws JavaModelException {
        String value = DiagnosticUtils.getAnnotationMemberValue(pcAnnotation, "type", String.class);
        return value != null && value.endsWith(Constants.PERSISTENCE_CONTEXT_TYPE_EXTENDED);
    }
}
