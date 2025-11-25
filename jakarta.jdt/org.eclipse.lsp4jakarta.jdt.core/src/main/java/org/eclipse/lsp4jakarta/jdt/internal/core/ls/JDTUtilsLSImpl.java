/*******************************************************************************
* Copyright (c) 2019, 2023 Red Hat Inc. and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.core.ls;

import java.io.Reader;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.JDTUtils;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.jdt.ls.core.internal.ResourceUtils;
import org.eclipse.jdt.ls.core.internal.handlers.DocumentLifeCycleHandler;
import org.eclipse.jdt.ls.core.internal.handlers.JsonRpcHelpers;
import org.eclipse.jdt.ls.core.internal.javadoc.JavadocContentAccess2;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4jakarta.commons.DocumentFormat;
import org.eclipse.lsp4jakarta.jdt.core.utils.IJDTUtils;

/**
 * {@link IJDTUtils} implementation with JDT S {@link JDTUtils}.
 *
 * Based on:
 * https://github.com/eclipse/lsp4mp/blob/0.9.0/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/internal/core/ls/JDTUtilsLSImpl.java
 *
 * @author Angelo ZERR
 *
 */
public class JDTUtilsLSImpl implements IJDTUtils {

    private static final int COMPILATION_UNIT_UPDATE_TIMEOUT = 3000;

    private static final IJDTUtils INSTANCE = new JDTUtilsLSImpl();

    public static IJDTUtils getInstance() {
        return INSTANCE;
    }

    private JDTUtilsLSImpl() {}

    @Override
    public IFile findFile(String uriString) {
        return JDTUtils.findFile(uriString);
    }

    @Override
    public ICompilationUnit resolveCompilationUnit(String uriString) {
        ICompilationUnit unit = JDTUtils.resolveCompilationUnit(uriString);
        if (unit != null) {
            try {
                // Give underlying resource time to catch up
                // (timeout at COMPILATION_UNIT_UPDATE_TIMEOUT milliseconds).
                long endTime = System.currentTimeMillis() + COMPILATION_UNIT_UPDATE_TIMEOUT;
                while (!unit.isConsistent() && System.currentTimeMillis() < endTime) {
                }
            } catch (JavaModelException e) {
            }
        }
        return unit;
    }

    @Override
    public IClassFile resolveClassFile(String uriString) {
        return JDTUtils.resolveClassFile(uriString);
    }

    @Override
    public boolean isHiddenGeneratedElement(IJavaElement element) {
        return JDTUtils.isHiddenGeneratedElement(element);
    }

    @Override
    public Range toRange(IOpenable openable, int offset, int length) throws JavaModelException {
        return JDTUtils.toRange(openable, offset, length);
    }

    @Override
    public String toClientUri(String uri) {
        return ResourceUtils.toClientUri(uri);
    }

    @Override
    public String toUri(ITypeRoot typeRoot) {
        return JDTUtils.toUri(typeRoot);
    }

    @Override
    public void waitForLifecycleJobs(IProgressMonitor monitor) {
        try {
            Job.getJobManager().join(DocumentLifeCycleHandler.DOCUMENT_LIFE_CYCLE_JOBS, monitor);
        } catch (OperationCanceledException ignorable) {
            // No need to pollute logs when query is cancelled
        } catch (Exception e) {
            JavaLanguageServerPlugin.logException(e.getMessage(), e);
        }
    }

    @Override
    public int toOffset(IBuffer buffer, int line, int column) {
        return JsonRpcHelpers.toOffset(buffer, line, column);
    }

    @Override
    public Location toLocation(IJavaElement element) throws JavaModelException {
        return JDTUtils.toLocation(element);
    }

    @Override
    public String getJavadoc(IMember member, DocumentFormat documentFormat) throws JavaModelException {
        boolean markdown = DocumentFormat.Markdown.equals(documentFormat);
        Reader reader = markdown ? JavadocContentAccess2.getMarkdownContentReader(member) : JavadocContentAccess2.getPlainTextContentReader(member);
        return reader != null ? toString(reader) : null;
    }

    private static String toString(Reader reader) {
        try (Scanner s = new java.util.Scanner(reader)) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }
}