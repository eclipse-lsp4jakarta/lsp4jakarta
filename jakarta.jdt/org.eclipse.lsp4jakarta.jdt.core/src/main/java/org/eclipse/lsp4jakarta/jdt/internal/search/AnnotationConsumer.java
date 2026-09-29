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

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.JavaModelException;

/**
 * A checked-exception variant of {@link java.util.function.Consumer} for
 * {@link IAnnotation} values.
 *
 * <p>Used wherever JDT model traversal needs to pass each visited annotation
 * to a callback that may throw {@link JavaModelException} (e.g. reading
 * annotation member values inside {@code forEachNestedGraph}).
 *
 * <p>{@link java.util.function.Consumer} cannot be used directly here because
 * Java's standard functional interfaces do not declare checked exceptions.
 */
@FunctionalInterface
public interface AnnotationConsumer {

    /**
     * Processes the given annotation.
     *
     * @param ann the annotation to process
     * @throws JavaModelException if a JDT model error occurs
     */
    void accept(IAnnotation ann) throws JavaModelException;
}
