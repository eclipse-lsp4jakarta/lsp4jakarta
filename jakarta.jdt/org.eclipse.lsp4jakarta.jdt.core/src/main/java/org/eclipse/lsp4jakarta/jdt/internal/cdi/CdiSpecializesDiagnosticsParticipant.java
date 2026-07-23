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
package org.eclipse.lsp4jakarta.jdt.internal.cdi;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
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
 * CDI diagnostics participant that validates specialization.
 *
 * A bean annotated with @Specializes must extend another bean. If the superclass
 * is not a bean (e.g., lacks a scope annotation), the specialization is invalid
 * and is treated as a definition error.
 *
 * @see https://jakarta.ee/specifications/cdi/3.0/jakarta-cdi-spec-3.0#direct_and_indirect_specialization
 */
public class CdiSpecializesDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(CdiSpecializesDiagnosticsParticipant.class.getName());

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
                validateSpecializes(type, uri, context, diagnostics);
            }
        } catch (JavaModelException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while validating @Specializes usage", e);
        }

        return diagnostics;
    }

    /**
     * Validates that a class annotated with @Specializes directly extends a valid bean.
     *
     * Per CDI spec section 3.1.4: "the bean class of X must directly extend the bean class
     * of another managed bean Y". Only the immediate superclass is checked — a scoped
     * grandparent does NOT satisfy this requirement.
     *
     * @param type the type to validate
     * @param uri the file URI
     * @param context the diagnostics context
     * @param diagnostics the list to add diagnostics to
     * @throws JavaModelException if an error occurs accessing the Java model
     */
    private void validateSpecializes(IType type, String uri, JavaDiagnosticsContext context,
                                     List<Diagnostic> diagnostics) throws JavaModelException {
        String[] typeAnnotations = Stream.of(type.getAnnotations()).map(a -> a.getElementName()).toArray(String[]::new);
        // Only validate classes annotated with @Specializes
        if (DiagnosticUtils.getMatchedJavaElementNames(type, typeAnnotations,
                                                       new String[] { Constants.SPECIALIZES_FQ_NAME }).isEmpty()) {
            return;
        }
        // Per CDI spec 3.1.4, only the direct (immediate) superclass must be a bean.
        // directSuperclassHasAnnotation checks only that single level — not grandparents.
        boolean directSuperclassIsBean = Stream.concat(Constants.SCOPE_FQ_NAMES.stream(),
                                                       Stream.of(Constants.NORMAL_SCOPE_FQ_NAME)).anyMatch(scopeFQName -> {
                                                           try {
                                                               return TypeHierarchyUtils.directSuperclassHasAnnotation(type, scopeFQName);
                                                           } catch (JavaModelException e) {
                                                               LOGGER.log(Level.WARNING, "Could not inspect direct superclass annotations", e);
                                                               return false;
                                                           }
                                                       });
        if (directSuperclassIsBean) {
            return;
        }
        // Direct superclass is not a bean — specialization is invalid
        Range range = PositionUtils.toNameRange(type, context.getUtils());
        diagnostics.add(context.createDiagnostic(uri,
                                                 Messages.getMessage("InvalidSpecializesAnnotationOnNonBeanSuperclass"),
                                                 range,
                                                 Constants.DIAGNOSTIC_SOURCE, null,
                                                 ErrorCode.InvalidSpecializesAnnotationOnNonBeanSuperclass,
                                                 DiagnosticSeverity.Error));
    }
}
