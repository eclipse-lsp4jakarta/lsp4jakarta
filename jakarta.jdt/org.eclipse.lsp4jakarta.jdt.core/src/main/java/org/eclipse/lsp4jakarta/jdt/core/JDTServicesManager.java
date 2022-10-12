/*******************************************************************************
* Copyright (c) 2019, 2022 Red Hat Inc. and others.
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

package org.eclipse.lsp4jakarta.jdt.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.jdt.codeAction.CodeActionHandler;
import org.eclipse.lsp4jakarta.jdt.core.annotations.AnnotationDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.beanvalidation.BeanValidationDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.cdi.ManagedBeanDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.di.DependencyInjectionDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.jax_rs.Jax_RSClassDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.jax_rs.ResourceMethodDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.jsonb.JsonbDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.jsonp.JsonpDiagnosticCollector;
import org.eclipse.lsp4jakarta.jdt.core.persistence.PersistenceEntityDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.persistence.PersistenceMapKeyDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.servlet.FilterDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.servlet.ListenerDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.servlet.ServletDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.transactions.TransactionsDiagnosticsCollector;
import org.eclipse.lsp4jakarta.jdt.core.websocket.WebSocketDiagnosticsCollector;

/**
 * JDT manager for Java files Modified from
 * https://github.com/eclipse/lsp4mp/blob/master/microprofile.jdt/org.eclipse.lsp4mp.jdt.core/src/main/java/org/eclipse/lsp4mp/jdt/core/PropertiesManagerForJava.java
 * with methods modified and removed to fit the purposes of the Jakarta Language
 * Server
 * 
 */
public class JDTServicesManager {

    private List<DiagnosticsCollector> diagnosticsCollectors = new ArrayList<>();

    private static final JDTServicesManager INSTANCE = new JDTServicesManager();

    private final CodeActionHandler codeActionHandler;

    public static JDTServicesManager getInstance() {
        return INSTANCE;
    }

    private JDTServicesManager() {
        diagnosticsCollectors.add(new ServletDiagnosticsCollector());
//        diagnosticsCollectors.add(new AnnotationDiagnosticsCollector());
//        diagnosticsCollectors.add(new FilterDiagnosticsCollector());
//        diagnosticsCollectors.add(new ListenerDiagnosticsCollector());
//        diagnosticsCollectors.add(new BeanValidationDiagnosticsCollector());
//        diagnosticsCollectors.add(new PersistenceEntityDiagnosticsCollector());
//        diagnosticsCollectors.add(new PersistenceMapKeyDiagnosticsCollector());
//        diagnosticsCollectors.add(new ResourceMethodDiagnosticsCollector());
//        diagnosticsCollectors.add(new Jax_RSClassDiagnosticsCollector());
//        diagnosticsCollectors.add(new JsonbDiagnosticsCollector());
//        diagnosticsCollectors.add(new ManagedBeanDiagnosticsCollector());
//        diagnosticsCollectors.add(new DependencyInjectionDiagnosticsCollector());
//        diagnosticsCollectors.add(new JsonpDiagnosticCollector());
//        diagnosticsCollectors.add(new WebSocketDiagnosticsCollector());
//        diagnosticsCollectors.add(new TransactionsDiagnosticsCollector());
        this.codeActionHandler = new CodeActionHandler();
    }

    /**
     * Returns diagnostics for the given uris from the JakartaDiagnosticsParams.
     * 
     * @param javaParams the diagnostics parameters
     * @return diagnostics
     */
    public List<PublishDiagnosticsParams> getJavaDiagnostics(JakartaDiagnosticsParams javaParams) {
        return getJavaDiagnostics(javaParams.getUris(), new NullProgressMonitor());
    }

    /**
     * Returns diagnostics for the given uris from the JakartaDiagnosticsParams.
     * 
     * @param javaParams the diagnostics parameters
     * @return diagnostics
     */
    public List<PublishDiagnosticsParams> getJavaDiagnostics(List<String> uris,
            IProgressMonitor monitor) {
        JavaLanguageServerPlugin.logInfo("JDTServicesManager 1");
//        List<String> uris = javaParams.getUris();
        if (uris == null) {
            return Collections.emptyList();
        }

        JavaLanguageServerPlugin.logInfo("JDTServicesManager 2");
        List<PublishDiagnosticsParams> publishDiagnostics = new ArrayList<PublishDiagnosticsParams>();
        for (String uri : uris) {
            List<Diagnostic> diagnostics = new ArrayList<>();
            URI u = JDTUtils.toURI(uri);
            JavaLanguageServerPlugin.logInfo("JDTServicesManager 3");
            ICompilationUnit unit = JDTUtils.resolveCompilationUnit(u);
            JavaLanguageServerPlugin.logInfo("JDTServicesManager 4 unit: " + unit);
            for (DiagnosticsCollector d : diagnosticsCollectors) {
                if (monitor.isCanceled()) {
                    break;
                }
                // TODO debug here
                JavaLanguageServerPlugin.logInfo("JDTServicesManager 5");
                d.collectDiagnostics(unit, diagnostics);
            }
            JavaLanguageServerPlugin.logInfo("JDTServicesManager 6");
            PublishDiagnosticsParams publishDiagnostic = new PublishDiagnosticsParams(uri, diagnostics);
            publishDiagnostics.add(publishDiagnostic);
            if (monitor.isCanceled()) {
                return Collections.emptyList();
            }
        }
        JavaLanguageServerPlugin.logInfo("JDTServicesManager 7: " + publishDiagnostics);
        return publishDiagnostics;
    }

    /**
     * @author ankushsharma
     * @brief Gets all snippet contexts that exist in the current project classpath
     * @param uri            - String representing file from which to derive project
     *                       classpath
     * @param snippetContext - get all the context fields from the snippets and
     *                       check if they exist in this method
     * @return List<String>
     */
    public List<String> getExistingContextsFromClassPath(String uri, List<String> snippetContexts) {
        JavaLanguageServerPlugin.logInfo("getExistingContextsFromClassPath");
        // Initialize the list that will hold the classpath
        List<String> classpath = new ArrayList<>();
        // Convert URI into a compilation unit
        ICompilationUnit unit = JDTUtils.resolveCompilationUnit(JDTUtils.toURI(uri));
        // Get Java Project
        JavaProject project = (JavaProject) unit.getJavaProject();
        // Get Java Project
        if (project != null) {
            snippetContexts.forEach(ctx -> {
                IType classPathctx = null;
                try {
                    classPathctx = project.findType(ctx);
                    if (classPathctx != null) {
                        classpath.add(ctx);
                    } else {
                        classpath.add(null);
                    }
                } catch (JavaModelException e) {
                    JavaLanguageServerPlugin.logException("Failed to retrieve projectContext from JDT...", e);
                }
            });
        } else {
            // Populate the Array with nulls up to length of snippetContext
            snippetContexts.forEach(ctx -> {
                classpath.add(null);
            });
        }
        JavaLanguageServerPlugin.logInfo("Returning classpath: " + classpath);
        return classpath;
    }

    public List<CodeAction> getCodeAction(JakartaJavaCodeActionParams params, JDTUtils utils, IProgressMonitor monitor)
            throws JavaModelException {
        return codeActionHandler.codeAction(params, utils, monitor);
    }
}
