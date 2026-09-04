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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.lsp4jakarta.jdt.internal.DiagnosticUtils;
import org.eclipse.lsp4jakarta.jdt.internal.SourceTypeScanner;

/**
 * Utility class for Jakarta Persistence diagnostic participants.
 *
 * <p>Provides shared helpers used across persistence diagnostic participants,
 * such as project-wide entity type scanning.
 */
public class PersistenceUtils {

    private PersistenceUtils() {
        // utility class — not instantiable
    }

    /**
     * Scans all source types in the given project and returns a map from simple
     * class name to {@link IType} for every type annotated with
     * {@code @jakarta.persistence.Entity}.
     *
     * @param javaProject the project to scan
     * @return a map from simple entity class name to its {@link IType}
     */
    public static Map<String, IType> findAnnotatedEntityTypes(IJavaProject javaProject) {
        Map<String, IType> entityTypeMap = new HashMap<>();
        SourceTypeScanner.scanSourceTypes(javaProject, (cu, scannedType) -> {
            if (DiagnosticUtils.isMatchedAnnotation(cu, scannedType.getAnnotations(), Constants.ENTITY)) {
                entityTypeMap.put(scannedType.getElementName(), scannedType);
            }
        });
        return entityTypeMap;
    }
}
