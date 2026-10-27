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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.IJavaDiagnosticsParticipant;
import org.eclipse.lsp4jakarta.jdt.core.java.diagnostics.JavaDiagnosticsContext;
import org.eclipse.lsp4jakarta.jdt.core.utils.PositionUtils;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.Messages;
import org.eclipse.lsp4jakarta.jdt.internal.core.ls.JDTUtilsLSImpl;
import org.eclipse.lsp4jakarta.jdt.internal.search.AnnotationConsumer;
import org.eclipse.lsp4jakarta.jdt.internal.search.JakartaSearchSettings;
import org.eclipse.lsp4jakarta.jdt.internal.search.ProjectWideNameScanner;

/**
 * Validates that {@code @NamedEntityGraph} names are unique within the persistence unit.
 * Spec §3.7.4:
 * <a href="https://jakarta.ee/specifications/persistence/3.0/jakarta-persistence-spec-3.0.html#a13662">a13662</a>
 */
public class NamedEntityGraphDiagnosticsParticipant implements IJavaDiagnosticsParticipant {

    /** {@inheritDoc} */
    @Override
    public List<Diagnostic> collectDiagnostics(JavaDiagnosticsContext context, IProgressMonitor monitor) throws CoreException {
        ICompilationUnit unit = JDTUtilsLSImpl.getInstance().resolveCompilationUnit(context.getUri());
        List<Diagnostic> diagnostics = new ArrayList<>();

        if (unit == null) {
            return diagnostics;
        }
        // Guard: O(annotations-in-file) — skip the expensive scan if this file has
        // no class that carries @NamedEntityGraph or @NamedEntityGraphs.
        if (!fileHasGraphAnnotation(unit)) {
            return diagnostics;
        }
        // Feature gate: disabled globally → no scan, no diagnostics.
        if (!JakartaSearchSettings.isSearchEngineDiagnosticsEnabled()) {
            return diagnostics;
        }

        // Maps each @NamedEntityGraph name to the number of times it is declared
        // across the entire project. Any name with a count > 1 is a duplicate.
        Map<String, Integer> graphNameOccurrences = ProjectWideNameScanner.scan(
                                                                                context.getJavaProject(), this::extractNamesFromType, monitor);

        for (IType type : unit.getAllTypes()) {
            validateType(type, unit, graphNameOccurrences, diagnostics, context);
        }
        return diagnostics;
    }

    // -------------------------------------------------------------------------
    // File-level guard
    // -------------------------------------------------------------------------

    private boolean fileHasGraphAnnotation(ICompilationUnit unit) throws JavaModelException {
        for (IType type : unit.getAllTypes()) {
            for (IAnnotation annotation : type.getAnnotations()) {
                String name = annotation.getElementName();
                if (DiagnosticUtils.isMatchedJavaElement(type, name, Constants.NAMEDENTITYGRAPH)
                    || DiagnosticUtils.isMatchedJavaElement(type, name, Constants.NAMEDENTITYGRAPHS)) {
                    return true;
                }
            }
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Extraction — passed to ProjectWideNameScanner as a method reference
    // -------------------------------------------------------------------------

    private void extractNamesFromType(IType type, Map<String, Integer> graphNameOccurrences) throws JavaModelException {
        for (IAnnotation annotation : type.getAnnotations()) {
            forEachGraph(type, annotation, nested -> mergeGraphName(nested, graphNameOccurrences));
        }
    }

    // -------------------------------------------------------------------------
    // Validation — flags duplicates and invalid attributes
    // -------------------------------------------------------------------------

    private void validateType(IType type, ICompilationUnit unit, Map<String, Integer> graphNameOccurrences,
                              List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {
        // JPA access type is determined by where @Id is placed.
        // Only validate attribute node existence when the entity uses field-based access
        // (i.e. at least one field in the hierarchy carries @Id).  Property-based access
        // (getter-level @Id) is deliberately excluded — resolving property names from
        // methods is complex and out of scope here.
        boolean fieldBased = DiagnosticUtils.isFieldBasedAccess(type, unit, Constants.ID, Constants.EMBEDDEDID);

        for (IAnnotation typeAnnotation : type.getAnnotations()) {
            String elementName = typeAnnotation.getElementName();
            if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMEDENTITYGRAPH)) {
                validateEntityGraph(typeAnnotation, type, fieldBased, graphNameOccurrences, diagnostics, context);
            } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMEDENTITYGRAPHS)) {
                forEachNestedGraph(typeAnnotation, nested -> validateEntityGraph(nested, type, fieldBased, graphNameOccurrences, diagnostics, context));
            }
        }
    }

    private void validateEntityGraph(IAnnotation graphAnnotation, IType type, boolean fieldBased,
                                     Map<String, Integer> graphNameOccurrences, List<Diagnostic> diagnostics,
                                     JavaDiagnosticsContext context) throws JavaModelException {
        checkForDuplicate(graphAnnotation, graphNameOccurrences, diagnostics, context);
        if (fieldBased) {
            validateAttributeNodes(graphAnnotation, type, diagnostics, context);
        }
    }

    private void checkForDuplicate(IAnnotation graphAnnotation, Map<String, Integer> graphNameOccurrences,
                                   List<Diagnostic> diagnostics, JavaDiagnosticsContext context) throws JavaModelException {
        String graphName = DiagnosticUtils.getAnnotationMemberValue(graphAnnotation, "name", String.class);
        if (graphName != null && graphNameOccurrences.getOrDefault(graphName, 0) > 1) {
            Range range = PositionUtils.toNameRange(graphAnnotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("DuplicateNamedEntityGraphName", graphName),
                                                     range, Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.DuplicateNamedEntityGraphName,
                                                     DiagnosticSeverity.Error));
        }
    }

    private void validateAttributeNodes(IAnnotation parentAnnotation, IType targetType,
                                        List<Diagnostic> diagnostics,
                                        JavaDiagnosticsContext context) throws JavaModelException {
        for (IAnnotation attributeNode : DiagnosticUtils.getAnnotationMemberValues(parentAnnotation, "attributeNodes", IAnnotation.class)) {
            validateSingleAttributeNode(attributeNode, targetType, diagnostics, context);
        }
    }

    private void validateSingleAttributeNode(IAnnotation attributeNodeAnnotation, IType targetType,
                                             List<Diagnostic> diagnostics,
                                             JavaDiagnosticsContext context) throws JavaModelException {
        String attrName = DiagnosticUtils.getAnnotationMemberValue(attributeNodeAnnotation, "value", String.class);
        if (attrName != null && !attrName.isEmpty() && !DiagnosticUtils.hasFieldInHierarchy(targetType, attrName)) {
            Range range = PositionUtils.toNameRange(attributeNodeAnnotation, context.getUtils());
            diagnostics.add(context.createDiagnostic(context.getUri(),
                                                     Messages.getMessage("NamedAttributeNodeAttributeNotFound", attrName, targetType.getElementName()),
                                                     range, Constants.DIAGNOSTIC_SOURCE,
                                                     ErrorCode.NamedAttributeNodeAttributeNotFound,
                                                     DiagnosticSeverity.Error));
        }
    }

    // -------------------------------------------------------------------------
    // Shared helpers
    // -------------------------------------------------------------------------

    /**
     * Invokes {@code consumer} for every individual {@code @NamedEntityGraph}
     * reachable from {@code annotation}: directly if it is a
     * {@code @NamedEntityGraph}, or once per nested entry if it is a
     * {@code @NamedEntityGraphs} container.
     */
    private void forEachGraph(IType type, IAnnotation annotation, AnnotationConsumer consumer) throws JavaModelException {
        String elementName = annotation.getElementName();
        if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMEDENTITYGRAPH)) {
            consumer.accept(annotation);
        } else if (DiagnosticUtils.isMatchedJavaElement(type, elementName, Constants.NAMEDENTITYGRAPHS)) {
            forEachNestedGraph(annotation, consumer);
        }
    }

    private void mergeGraphName(IAnnotation annotation, Map<String, Integer> graphNameOccurrences) throws JavaModelException {
        String name = DiagnosticUtils.getAnnotationMemberValue(annotation, "name", String.class);
        if (name != null) {
            graphNameOccurrences.merge(name, 1, Integer::sum);
        }
    }

    /**
     * Iterates every {@code @NamedEntityGraph} nested inside the given
     * {@code @NamedEntityGraphs} container annotation and passes each to
     * {@code consumer}.
     *
     * <p>JDT's {@link org.eclipse.jdt.core.IMemberValuePair#getValue()} returns:
     * <ul>
     * <li>{@code Object[]} (containing {@link IAnnotation} elements) when the container
     * specifies multiple elements or array notation, e.g. {@code @NamedEntityGraphs({@NamedEntityGraph(...), ...})}</li>
     * <li>A single {@link IAnnotation} instance when a single element is provided without array notation,
     * e.g. {@code @NamedEntityGraphs(@NamedEntityGraph(...))}</li>
     * </ul>
     */
    private void forEachNestedGraph(IAnnotation container, AnnotationConsumer consumer) throws JavaModelException {
        for (IAnnotation graph : DiagnosticUtils.getAnnotationMemberValues(container, "value", IAnnotation.class)) {
            consumer.accept(graph);
        }
    }
}
