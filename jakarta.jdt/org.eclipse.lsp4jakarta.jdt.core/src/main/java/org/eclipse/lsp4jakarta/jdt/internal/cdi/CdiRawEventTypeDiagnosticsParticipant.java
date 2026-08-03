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
import org.eclipse.jdt.core.Signature;
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

        IType[] types = unit.getAllTypes();
        for (IType type : types) {
            // Check @Inject fields for raw Event type
            for (IField field : type.getFields()) {
                String[] annotationNames = DiagnosticUtils.getAnnotationNames(field);
                if (hasAnnotation(type, annotationNames, Constants.INJECT_FQ_NAME)) {
                    if (isRawEventType(field.getTypeSignature())) {
                        Range range = PositionUtils.toNameRange(field, context.getUtils());
                        diagnostics.add(context.createDiagnostic(uri,
                                                                 Messages.getMessage("InvalidRawEventTypeInjectionPoint"),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidRawEventTypeInjectionPoint,
                                                                 DiagnosticSeverity.Error));
                    }
                }
            }

            // Check parameters of @Inject methods for raw Event type.
            // The diagnostic range is placed on the method name so that the
            // RemoveAnnotationConflictQuickFix can resolve the @Inject annotation
            // on the method correctly via its parent-type binding.
            for (IMethod method : type.getMethods()) {
                String[] annotationNames = DiagnosticUtils.getAnnotationNames(method);
                if (hasAnnotation(type, annotationNames, Constants.INJECT_FQ_NAME)) {
                    String[] paramTypes = method.getParameterTypes();
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (isRawEventType(paramTypes[i])) {
                            Range range = PositionUtils.toNameRange(method, context.getUtils());
                            diagnostics.add(context.createDiagnostic(uri,
                                                                     Messages.getMessage("InvalidRawEventTypeInjectionPoint"),
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
        }

        return diagnostics;
    }

    /**
     * Checks if an annotation array contains a specific annotation.
     *
     * @param type the type containing the annotations
     * @param annotationNames array of annotation names to check
     * @param annotationFQName the fully qualified name of the annotation to match
     * @return true if the annotation is found, false otherwise
     */
    private boolean hasAnnotation(IType type, String[] annotationNames, String annotationFQName) {
        return DiagnosticUtils.getMatchedJavaElementNames(type, annotationNames,
                                                          new String[] { annotationFQName }).size() > 0;
    }

    /**
     * Checks whether a type signature represents a raw (unparameterized) {@code Event} type.
     *
     * <p>A raw {@code Event} has no type arguments, i.e. the signature has no {@code <…>} part.
     * Parameterized forms such as {@code Event<String>} are valid and not flagged.
     *
     * @param typeSignature the JDT type signature to check
     * @return true if the signature is the raw Event type, false otherwise
     */
    private boolean isRawEventType(String typeSignature) {
        if (typeSignature == null || typeSignature.isEmpty()) {
            return false;
        }
        // Unwrap array component types — Event[] would also be raw
        if (Signature.getTypeSignatureKind(typeSignature) == Signature.ARRAY_TYPE_SIGNATURE) {
            return isRawEventType(Signature.getElementType(typeSignature));
        }
        String erasure = Signature.getTypeErasure(typeSignature);
        String simpleName = Signature.getSignatureSimpleName(erasure);
        if (!simpleName.equals("Event")) {
            return false;
        }
        // Raw type has no type arguments
        String[] typeArgs = Signature.getTypeArguments(typeSignature);
        return typeArgs.length == 0;
    }
}
