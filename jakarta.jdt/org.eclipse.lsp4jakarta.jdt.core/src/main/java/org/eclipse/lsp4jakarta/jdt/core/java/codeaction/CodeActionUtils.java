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
package org.eclipse.lsp4jakarta.jdt.core.java.codeaction;

import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Utility methods for code action (quickfix) participants.
 */
public class CodeActionUtils {

    private CodeActionUtils() {
        // utility class
    }

    /**
     * Checks if the given binding (class, field, or method) has the specified annotation.
     *
     * @param binding The binding — may be an {@link ITypeBinding}, {@link IVariableBinding},
     *            or {@link IMethodBinding}
     * @param annotationFQN The fully qualified annotation name
     * @return true if the annotation is present, false otherwise
     */
    public static boolean hasAnnotation(IBinding binding, String annotationFQN) {
        IAnnotationBinding[] annotations;
        if (binding instanceof ITypeBinding) {
            annotations = ((ITypeBinding) binding).getAnnotations();
        } else if (binding instanceof IVariableBinding) {
            annotations = ((IVariableBinding) binding).getAnnotations();
        } else if (binding instanceof IMethodBinding) {
            annotations = ((IMethodBinding) binding).getAnnotations();
        } else {
            return false;
        }
        return Stream.of(annotations).anyMatch(annotation -> annotation.getAnnotationType().getQualifiedName().equals(annotationFQN));
    }
}
