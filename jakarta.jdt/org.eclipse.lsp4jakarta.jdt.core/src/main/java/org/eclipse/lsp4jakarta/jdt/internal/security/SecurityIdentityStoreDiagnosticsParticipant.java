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
package org.eclipse.lsp4jakarta.jdt.internal.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * Jakarta Security diagnostics participant that validates identity store definition beans.
 *
 * Ensures that beans annotated with @LdapIdentityStoreDefinition or @DatabaseIdentityStoreDefinition
 * comply with the Jakarta Security specification by having the required @ApplicationScoped scope.
 *
 * @see <a href="https://jakarta.ee/specifications/security/2.0/jakarta-security-spec-2.0#annotations-and-built-in-identitystore-beans">
 *      Jakarta Security Specification - Identity Store Annotations</a>
 */
public class SecurityIdentityStoreDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        IJDTUtils utils = JDTUtilsLSImpl.getInstance();
        String uri = context.getUri();
        ICompilationUnit unit = utils.resolveCompilationUnit(uri);
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }

        // Identity store definition annotations to check
        List<String> identityStoreAnnotations = Arrays.asList(
                                                              Constants.LDAP_IDENTITY_STORE_DEFINITION_FQ_NAME,
                                                              Constants.DATABASE_IDENTITY_STORE_DEFINITION_FQ_NAME);

        for (IType type : unit.getAllTypes()) {
            // Get all annotations on the type
            IAnnotation[] typeAnnotations = type.getAnnotations();
            List<String> annotationNames = Stream.of(typeAnnotations).map(annotation -> annotation.getElementName()).collect(Collectors.toList());

            // Check if type has any identity store definition annotation
            List<String> identityStoreDefAnnotations = DiagnosticUtils.getMatchedJavaElementNames(
                                                                                                  type, annotationNames, identityStoreAnnotations);

            if (!identityStoreDefAnnotations.isEmpty()) {
                // Type has an identity store definition annotation
                // Now check if it has @ApplicationScoped
                boolean hasApplicationScoped = !DiagnosticUtils.getMatchedJavaElementNames(
                                                                                           type, annotationNames,
                                                                                           Collections.singletonList(Constants.APPLICATION_SCOPED_FQ_NAME)).isEmpty();

                if (!hasApplicationScoped) {
                    // Check if it has any other scope annotation
                    List<String> foundScopes = DiagnosticUtils.getMatchedJavaElementNames(
                                                                                          type, annotationNames, Constants.SCOPE_FQ_NAMES);

                    // Get the identity store annotation name for the diagnostic message
                    String identityStoreAnnotationSimpleName = DiagnosticUtils.getSimpleName(identityStoreDefAnnotations.get(0));

                    Range range = PositionUtils.toNameRange(type, context.getUtils());

                    if (foundScopes.isEmpty()) {
                        // Missing @ApplicationScoped annotation
                        diagnostics.add(context.createDiagnostic(
                                                                 uri,
                                                                 Messages.getMessage("MissingApplicationScopedOnIdentityStoreDefinition",
                                                                                     identityStoreAnnotationSimpleName),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.MissingApplicationScopedOnIdentityStoreDefinition,
                                                                 DiagnosticSeverity.Error));
                    } else {
                        // Has wrong scope annotation
                        String wrongScopeSimpleName = DiagnosticUtils.getSimpleName(foundScopes.get(0));
                        diagnostics.add(context.createDiagnostic(
                                                                 uri,
                                                                 Messages.getMessage("InvalidScopeOnIdentityStoreDefinition",
                                                                                     identityStoreAnnotationSimpleName,
                                                                                     wrongScopeSimpleName),
                                                                 range,
                                                                 Constants.DIAGNOSTIC_SOURCE,
                                                                 null,
                                                                 ErrorCode.InvalidScopeOnIdentityStoreDefinition,
                                                                 DiagnosticSeverity.Error));
                    }
                }
            }
        }

        return diagnostics;
    }
}