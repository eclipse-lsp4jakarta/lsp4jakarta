/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
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
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
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
     * Returns the LSP range for the given member (field or method).
     *
     * @param member the java member (IField or IMethod).
     * @param utils the JDT utilities.
     * @return the LSP range for the given member name.
     * @throws JavaModelException
     */
    public static Range toNameRange(IMember member, IJDTUtils utils) throws JavaModelException {
        if (member instanceof IField) {
            return toNameRange((IField) member, utils);
        } else if (member instanceof IMethod) {
            return toNameRange((IMethod) member, utils);
        } else if (member instanceof IType) {
            return toNameRange((IType) member, utils);
        }
        // Fallback for other IMember types
        IOpenable openable = member.getCompilationUnit();
        ISourceRange sourceRange = member.getNameRange();
        return utils.toRange(openable, sourceRange.getOffset(), sourceRange.getLength());
    }
}
