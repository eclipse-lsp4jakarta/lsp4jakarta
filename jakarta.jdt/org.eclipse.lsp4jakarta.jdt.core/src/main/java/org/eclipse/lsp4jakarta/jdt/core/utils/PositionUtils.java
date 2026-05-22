/*******************************************************************************
* Copyright (c) 2020, 2026 Red Hat Inc. and others.
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License v. 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
* which is available at https://www.apache.org/licenses/LICENSE-2.0.
*
* SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4jakarta.jdt.core.utils;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4j.Range;

/**
 * Position utilities.
 *
 * @author Angelo ZERR
 *
 */
public class PositionUtils {

    /**
     * Returns the LSP range for the given field name.
     *
     * @param field the java field.
     * @param utils the JDT utilities.
     * @return the LSP range for the given field name.
     * @throws JavaModelException
     */
    public static Range toNameRange(IField field, IJDTUtils utils) throws JavaModelException {
        IOpenable openable = field.getCompilationUnit();
        ISourceRange sourceRange = field.getNameRange();
        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }

    /**
     * Returns the LSP range for the given type name.
     *
     * @param type the java type.
     * @param utils the JDT utilities.
     * @return the LSP range for the given type name.
     * @throws JavaModelException
     */
    public static Range toNameRange(IType type, IJDTUtils utils) throws JavaModelException {
        IOpenable openable = type.getCompilationUnit();
        ISourceRange sourceRange = type.getNameRange();
        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }

    /**
     * Returns the LSP range for the given method name.
     *
     * @param method the java type.
     * @param utils the JDT utilities.
     * @return the LSP range for the given method name.
     * @throws JavaModelException
     */
    public static Range toNameRange(IMethod method, IJDTUtils utils) throws JavaModelException {
        IOpenable openable = method.getCompilationUnit();
        ISourceRange sourceRange = method.getNameRange();
        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }

    /**
     * Returns the LSP range for the given annotation.
     *
     * @param annotation the java type.
     * @param utils the JDT utilities.
     * @return the LSP range for the given annotation.
     * @throws JavaModelException
     */
    public static Range toNameRange(IAnnotation annotation, IJDTUtils utils) throws JavaModelException {
        IOpenable openable = annotation.getOpenable();
        ISourceRange sourceRange = annotation.getSourceRange();
        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }

    /**
     * Returns the LSP range for the given Local variable.
     *
     * @param localVariable the java type.
     * @param utils the JDT utilities.
     * @return the LSP range for the given annotation.
     * @throws JavaModelException
     */
    public static Range toNameRange(ILocalVariable localVariable, IJDTUtils utils) throws JavaModelException {
        IOpenable openable = localVariable.getOpenable();
        ISourceRange sourceRange = localVariable.getNameRange();
        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }

    /**
     * toNameRange
     * Returns the LSP range for the given Java element name.
     *
     * @param element the Java element (IField, IMethod, ILocalVariable, IType, or IAnnotation)
     * @param utils the JDT utilities
     * @return the LSP range for the given element name
     * @throws JavaModelException if there's an error accessing the element
     */
    public static Range toNameRange(IJavaElement element, IJDTUtils utils) throws JavaModelException {
        if (!(element instanceof ISourceReference)) {
            throw new IllegalArgumentException("Element must implement ISourceReference: " + element.getClass().getName());
        }

        ISourceReference sourceRef = (ISourceReference) element;
        ISourceRange sourceRange;
        IOpenable openable;

        // IAnnotation uses getSourceRange(), others use getNameRange()
        if (element instanceof IAnnotation) {
            IAnnotation annotation = (IAnnotation) element;
            sourceRange = annotation.getSourceRange();
            openable = annotation.getOpenable();
        } else {
            sourceRange = sourceRef.getNameRange();
            openable = element.getOpenable();
        }

        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }
}
