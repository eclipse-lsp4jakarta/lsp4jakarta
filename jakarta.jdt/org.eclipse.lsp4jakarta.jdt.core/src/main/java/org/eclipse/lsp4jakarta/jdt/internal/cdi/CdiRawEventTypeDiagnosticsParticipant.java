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
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * CDI diagnostics participant that detects raw {@code Event} injection points.
 *
 * <p>According to CDI specification section 10.4.2:
 * "If an injection point of raw type Event is defined, the container automatically
 * detects the problem and treats it as a definition error."
 *
 * <p>This applies to:
 * <ul>
 * <li>{@code @Inject} fields of raw type {@code Event}</li>
 * <li>Parameters of {@code @Inject} methods of raw type {@code Event}</li>
 * </ul>
 */
public class CdiRawEventTypeDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /** Logger object to record events for this class. */
    private static final Logger LOGGER = Logger.getLogger(CdiRawEventTypeDiagnosticsParticipant.class.getName());

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        // Early exit: if jakarta.enterprise.event.Event is not imported, no raw-Event
        // injection point can exist in this file.
        if (!DiagnosticUtils.isImportedJavaElement(unit, Constants.EVENT_FQ_NAME)) {
            return diagnostics;
        }

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            // Check @Inject fields for raw Event type
            for (IField field : type.getFields()) {
                if (DiagnosticUtils.isRawEventType(field.getTypeSignature())
                    && DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), Constants.INJECT_FQ_NAME)) {
                    Range range = PositionUtils.toNameRange(field, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage(ErrorCode.InvalidRawEventTypeInjectionPoint.name()),
                                                             range,
                                                             Constants.DIAGNOSTIC_SOURCE,
                                                             null,
                                                             ErrorCode.InvalidRawEventTypeInjectionPoint,
                                                             DiagnosticSeverity.Error));
                }
            }

            // Check parameters of @Inject methods for raw Event type.
            // The diagnostic range is placed on the method name so that the
            // RemoveAnnotationConflictQuickFix can resolve the @Inject annotation
            // on the method correctly via its parent-type binding.
            for (IMethod method : type.getMethods()) {
                String[] paramTypes = method.getParameterTypes();
                for (String paramType : paramTypes) {
                    if (DiagnosticUtils.isRawEventType(paramType)
                        && DiagnosticUtils.isMatchedAnnotation(unit, method.getAnnotations(), Constants.INJECT_FQ_NAME)) {
                        Range range = PositionUtils.toNameRange(method, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage(ErrorCode.InvalidRawEventTypeInjectionPoint.name()),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidRawEventTypeInjectionPoint,
                                                                 DiagnosticSeverity.Error));
                        // One diagnostic per method is sufficient — the whole @Inject must be removed
                        break;
                    }
                }
            }
        }

        return diagnostics;
    }

}
