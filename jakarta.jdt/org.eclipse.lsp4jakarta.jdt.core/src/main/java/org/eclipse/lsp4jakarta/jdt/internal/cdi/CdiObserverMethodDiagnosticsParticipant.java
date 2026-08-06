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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
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
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that validates custom ObserverMethod implementations.
 *
 * <p>Per the CDI 3.0 specification, a class that directly implements the
 * {@code ObserverMethod} interface must override at least one of:
 * <ul>
 * <li>{@code notify(T event)}</li>
 * <li>{@code notify(EventContext&lt;T&gt; eventContext)}</li>
 * </ul>
 * If neither method is overridden the container cannot invoke the observer logic
 * and must treat the implementation as a definition error.
 *
 * @see <a href="https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#after_bean_discovery">
 *      CDI 3.0 spec §11.5.4</a>
 */
public class CdiObserverMethodDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(CdiObserverMethodDiagnosticsParticipant.class.getName());

    /** The simple name of the two required {@code notify} overloads. */
    private static final String NOTIFY_METHOD_NAME = "notify";

    /**
     * Collects diagnostics for all types in the compilation unit identified by
     * {@code context.getUri()}, flagging any that implement {@code ObserverMethod}
     * without a {@code notify} override.
     *
     * @param context the diagnostics context providing the file URI and utilities
     * @param monitor progress monitor (may be {@code null})
     * @return list of diagnostics; empty if the compilation unit cannot be resolved
     * @throws CoreException if an error occurs during Java model access
     */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        try {
            IType[] types = unit.getAllTypes();
            for (IType type : types) {
                validateObserverMethodImplementation(type, unit, uri, context, diagnostics);
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.SEVERE, "Error validating ObserverMethod implementation", e);
        }

        return diagnostics;
    }

    /**
     * Validates that a concrete class directly implementing {@code ObserverMethod} overrides
     * at least one of the two required {@code notify} methods.
     *
     * <p>Interfaces, annotations, and abstract classes are skipped — they may legally
     * defer the {@code notify} implementation to concrete subclasses.
     *
     * @param type the type to validate
     * @param unit the compilation unit
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateObserverMethodImplementation(IType type, ICompilationUnit unit, String uri,
                                                      JavaDiagnosticsContext context,
                                                      List<Diagnostic> diagnostics) throws CoreException {

        // Only concrete classes that directly implement ObserverMethod are relevant.
        // Interfaces and abstract classes may legally defer the notify implementation.
        if (type.isInterface() || type.isAnnotation() || Flags.isAbstract(type.getFlags())) {
            return;
        }

        if (TypeHierarchyUtils.doesITypeHaveSuperType(type, Constants.OBSERVER_METHOD_FQ_NAME) != TypeHierarchyUtils.HAS_SUPERTYPE) {
            return;
        }

        // Check whether the class declares any method named "notify".
        // The ObserverMethod interface defines two overloads: notify(T) and
        // notify(EventContext<T>).
        boolean hasNotifyOverride = false;
        for (IMethod method : type.getMethods()) {
            if (NOTIFY_METHOD_NAME.equals(method.getElementName()) && !method.isConstructor()) {
                hasNotifyOverride = true;
                break;
            }
        }

        if (!hasNotifyOverride) {
            Range range = PositionUtils.toNameRange(type, context.getUtils());
            String message = Messages.getMessage("InvalidObserverMethodWithoutNotify", type.getElementName());
            diagnostics.add(context.createDiagnostic(uri, message, range,
                                                     Constants.DIAGNOSTIC_SOURCE, null,
                                                     ErrorCode.InvalidObserverMethodWithoutNotify,
                                                     DiagnosticSeverity.Error));
        }
    }
}
