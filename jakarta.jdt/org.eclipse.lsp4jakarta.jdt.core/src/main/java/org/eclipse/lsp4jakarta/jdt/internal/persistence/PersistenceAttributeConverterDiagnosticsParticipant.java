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
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
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
 * Persistence diagnostic participant that manages the use of the @Converter annotation.
 */
public class PersistenceAttributeConverterDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    private static final Logger LOGGER = Logger.getLogger(PersistenceAttributeConverterDiagnosticsParticipant.class.getName());

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

        IType[] alltypes = unit.getAllTypes();

        for (IType type : alltypes) {
            IAnnotation[] allAnnotations = type.getAnnotations();
            boolean isConverterAnnotated = false;

            for (IAnnotation annotation : allAnnotations) {
                if (DiagnosticUtils.isMatchedJavaElement(type, annotation.getElementName(),
                                                         Constants.CONVERTER)) {
                    isConverterAnnotated = true;
                    break;
                }
            }

            String[] interfaces = { Constants.ATTRIBUTE_CONVERTER };
            boolean implementsAttributeConverter = DiagnosticUtils.doesImplementInterfaces(type, interfaces);

            if (isConverterAnnotated && !implementsAttributeConverter) {
                Range range = PositionUtils.toNameRange(type, context.getUtils());
                diagnostics.add(context.createDiagnostic(uri,
                                                        Messages.getMessage("ConverterMustImplementAttributeConverter"),
                                                        range, Constants.DIAGNOSTIC_SOURCE, null,
                                                        ErrorCode.ConverterMustImplementAttributeConverter,
                                                        DiagnosticSeverity.Error));
            }
        }

        return diagnostics;
    }
}
