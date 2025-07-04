/*******************************************************************************
* Copyright (c) 2023 IBM Corporation and others.
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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.JakartaCorePlugin;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;

/**
 * Persistence diagnostic participant that manages the use
 * of @MapKeyClass, @MapKey, and @MapKeyJoinColumn annotations.
 */
public class PersistenceMapKeyDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

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
        IMethod[] methods;
        IField[] fields;

        for (IType type : alltypes) {
            methods = type.getMethods();
            for (IMethod method : methods) {
                List<IAnnotation> mapKeyJoinCols = new ArrayList<IAnnotation>();
                boolean hasMapKeyAnnotation = false;
                boolean hasMapKeyClassAnnotation = false;
                allAnnotations = method.getAnnotations();
                for (IAnnotation annotation : allAnnotations) {
                    String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                         annotation.getElementName(),
                                                                                         Constants.SET_OF_PERSISTENCE_ANNOTATIONS);
                    if (matchedAnnotation != null) {
                        if (Constants.MAPKEY.equals(matchedAnnotation))
                            hasMapKeyAnnotation = true;
                        else if (Constants.MAPKEYCLASS.equals(matchedAnnotation))
                            hasMapKeyClassAnnotation = true;
                        else if (Constants.MAPKEYJOINCOLUMN.equals(matchedAnnotation)) {
                            mapKeyJoinCols.add(annotation);
                        }
                    }
                }
                if (hasMapKeyAnnotation && hasMapKeyClassAnnotation) {
                    // A single field cannot have the same
                    Range range = PositionUtils.toNameRange(method, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("MapKeyAnnotationsNotOnSameMethod"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidMapKeyAnnotationsOnSameMethod, DiagnosticSeverity.Error));
                }
                // If we have multiple MapKeyJoinColumn annotations on a single method we must
                // ensure each has a name and referencedColumnName
                if (mapKeyJoinCols.size() > 1) {
                    validateMapKeyJoinColumnAnnotations(context, uri, mapKeyJoinCols, method, unit, diagnostics);
                }
            }

            // Go through each field to ensure they do not have both MapKey and MapKeyColumn
            // Annotations
            fields = type.getFields();
            for (IField field : fields) {
                List<IAnnotation> mapKeyJoinCols = new ArrayList<IAnnotation>();
                boolean hasMapKeyAnnotation = false;
                boolean hasMapKeyClassAnnotation = false;
                allAnnotations = field.getAnnotations();
                for (IAnnotation annotation : allAnnotations) {
                    String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                         annotation.getElementName(),
                                                                                         Constants.SET_OF_PERSISTENCE_ANNOTATIONS);
                    if (matchedAnnotation != null) {
                        if (Constants.MAPKEY.equals(matchedAnnotation))
                            hasMapKeyAnnotation = true;
                        else if (Constants.MAPKEYCLASS.equals(matchedAnnotation))
                            hasMapKeyClassAnnotation = true;
                        else if (Constants.MAPKEYJOINCOLUMN.equals(matchedAnnotation)) {
                            mapKeyJoinCols.add(annotation);
                        }
                    }
                }
                if (hasMapKeyAnnotation && hasMapKeyClassAnnotation) {
                    // A single field cannot have the same
                    Range range = PositionUtils.toNameRange(field, context.getUtils());
                    diagnostics.add(context.createDiagnostic(uri,
                                                             Messages.getMessage("MapKeyAnnotationsNotOnSameField"), range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             ErrorCode.InvalidMapKeyAnnotationsOnSameField, DiagnosticSeverity.Error));
                }
                if (mapKeyJoinCols.size() > 1) {
                    validateMapKeyJoinColumnAnnotations(context, uri, mapKeyJoinCols, field, unit, diagnostics);
                }
            }
        }

        return diagnostics;
    }

    private void validateMapKeyJoinColumnAnnotations(JavaDiagnosticsContext context, String uri,
                                                     List<IAnnotation> annotations,
                                                     IMember element,
                                                     ICompilationUnit unit, List<Diagnostic> diagnostics) throws CoreException {

        annotations.forEach(annotation -> {
            boolean allNamesSpecified, allReferencedColumnNameSpecified;
            try {
                Range range = null;
                String message = null;
                ErrorCode errorCode = null;
                if (element instanceof IMethod) {
                    range = PositionUtils.toNameRange((IMethod) element, context.getUtils());
                    errorCode = ErrorCode.InvalidMethodWithMultipleMPJCAnnotations;
                    message = Messages.getMessage("MultipleMapKeyJoinColumnMethod");
                } else {
                    range = PositionUtils.toNameRange((IField) element, context.getUtils());
                    errorCode = ErrorCode.InvalidFieldWithMultipleMPJCAnnotations;
                    message = Messages.getMessage("MultipleMapKeyJoinColumnField");
                }

                List<IMemberValuePair> memberValues = Arrays.asList(annotation.getMemberValuePairs());
                allNamesSpecified = memberValues.stream().anyMatch((mv) -> mv.getMemberName().equals(Constants.NAME));
                allReferencedColumnNameSpecified = memberValues.stream().anyMatch((mv) -> mv.getMemberName().equals(Constants.REFERENCEDCOLUMNNAME));
                if (!allNamesSpecified || !allReferencedColumnNameSpecified) {
                    diagnostics.add(context.createDiagnostic(uri,
                                                             message, range,
                                                             Constants.DIAGNOSTIC_SOURCE, null,
                                                             errorCode, DiagnosticSeverity.Error));
                }
            } catch (JavaModelException e) {
                JakartaCorePlugin.logException("Error while retrieving member values of @MapKeyJoinColumn Annotation",
                                               e);
            }
        });
    }
    
    private void collectMemberDiagnostics(IAnnotation[] allAnnotations, IType type) throws CoreException {
    	
    	List<IAnnotation> mapKeyJoinCols = new ArrayList<IAnnotation>();
        boolean hasMapKeyAnnotation = false;
        boolean hasMapKeyClassAnnotation = false;
        
        for (IAnnotation annotation : allAnnotations) {
            String matchedAnnotation = DiagnosticUtils.getMatchedJavaElementName(type,
                                                                                 annotation.getElementName(),
                                                                                 Constants.SET_OF_PERSISTENCE_ANNOTATIONS);
            if (matchedAnnotation != null) {
                if (Constants.MAPKEY.equals(matchedAnnotation))
                    hasMapKeyAnnotation = true;
                else if (Constants.MAPKEYCLASS.equals(matchedAnnotation))
                    hasMapKeyClassAnnotation = true;
                else if (Constants.MAPKEYJOINCOLUMN.equals(matchedAnnotation)) {
                    mapKeyJoinCols.add(annotation);
                }
            }
        }
    }
}
