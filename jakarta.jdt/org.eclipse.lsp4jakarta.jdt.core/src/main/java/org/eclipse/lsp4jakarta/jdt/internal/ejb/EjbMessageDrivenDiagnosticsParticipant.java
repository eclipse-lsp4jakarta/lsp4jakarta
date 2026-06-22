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
package org.eclipse.lsp4jakarta.jdt.internal.ejb;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import static org.eclipse.lsp4jakarta.jdt.core.ejb.Constants.DIAGNOSTIC_CODE_MESSAGE;
import static org.eclipse.lsp4jakarta.jdt.core.ejb.Constants.DIAGNOSTIC_SOURCE;
import static org.eclipse.lsp4jakarta.jdt.core.ejb.Constants.MESSAGE_DRIVEN_FQ_NAME;
import static org.eclipse.lsp4jakarta.jdt.core.ejb.Constants.MESSAGE_LISTENER_FQ_NAME;

import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Jakarta Enterprise Beans @MessageDriven annotation diagnostic participant.
 *
 * Validates that classes annotated with @MessageDriven implement the required
 * message listener interface (jakarta.jms.MessageListener for JMS message-driven beans).
 *
 * @see <a href="https://jakarta.ee/specifications/enterprise-beans/4.0/jakarta-enterprise-beans-spec-core-4.0#the-required-message-listener-interface">
 *      Jakarta Enterprise Beans Specification - Message Listener Interface</a>
 */
public class EjbMessageDrivenDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
            IAnnotation messageDrivenAnnotation = null;

            // Check if the class has @MessageDriven annotation
            for (IAnnotation annotation : type.getAnnotations()) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                         MESSAGE_DRIVEN_FQ_NAME)) {
                    messageDrivenAnnotation = annotation;
                    break;
                }
            }

            // If @MessageDriven is present, check if MessageListener interface is implemented
            if (messageDrivenAnnotation != null) {
                String[] interfaces = { MESSAGE_LISTENER_FQ_NAME };
                boolean isMessageListenerImplemented = DiagnosticUtils.doesImplementInterfaces(type, interfaces);

                if (!isMessageListenerImplemented) {
                    Range range = PositionUtils.toNameRange(type, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage(DIAGNOSTIC_CODE_MESSAGE),
                                                             range,
                                                             DIAGNOSTIC_SOURCE,
                                                             null,
                                                             ErrorCode.ImplementMessageListener,
                                                             DiagnosticSeverity.Error));
                }
            }
        }

        return diagnostics;
    }
}