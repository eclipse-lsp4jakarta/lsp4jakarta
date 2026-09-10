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
package org.eclipse.lsp4jakarta.jdt.internal.search;

import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Caller-supplied strategy that decides <em>what</em> to extract from a visited
 * {@link IType} and how to accumulate it into the name-count map.
 *
 * <p>The implementation has full access to everything reachable from the type:
 * <ul>
 * <li>Class-level annotation attributes (e.g. {@code @NamedEntityGraph(name)})</li>
 * <li>Field-level annotation attributes (e.g. injection point names)</li>
 * <li>Method-level annotation attributes</li>
 * <li>Any combination of the above</li>
 * </ul>
 *
 * <p>Example — collect {@code @NamedEntityGraph} names from class annotations:
 *
 * <pre>{@code
 * NameExtractorStrategy extractor = (type, nameCount) -> {
 *     for (IAnnotation ann : type.getAnnotations()) {
 *         if (DiagnosticUtils.isMatchedJavaElement(type, ann.getElementName(), NAMED_ENTITY_GRAPH)) {
 *             String name = getNameAttr(ann);
 *             if (name != null)
 *                 nameCount.merge(name, 1, Integer::sum);
 *         }
 *     }
 * };
 * }</pre>
 *
 * <p>Example — collect {@code @Inject} field names:
 *
 * <pre>{@code
 * NameExtractorStrategy extractor = (type, nameCount) -> {
 *     for (IField field : type.getFields()) {
 *         if (DiagnosticUtils.isMatchedAnnotation(unit, field.getAnnotations(), INJECT)) {
 *             nameCount.merge(field.getElementName(), 1, Integer::sum);
 *         }
 *     }
 * };
 * }</pre>
 */
@FunctionalInterface
public interface NameExtractorStrategy {

    /**
     * Inspect {@code type} and merge any names of interest into {@code nameCount}.
     *
     * @param type the type currently being visited by {@link ProjectWideNameScanner}
     * @param nameCount mutable map; use {@code nameCount.merge(name, 1, Integer::sum)}
     * @throws JavaModelException on JDT model errors
     */
    void extract(IType type, Map<String, Integer> nameCount) throws JavaModelException;
}
