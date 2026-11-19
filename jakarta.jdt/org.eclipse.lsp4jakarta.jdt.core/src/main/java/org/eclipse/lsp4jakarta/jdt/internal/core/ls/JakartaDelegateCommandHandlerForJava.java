/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
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
package org.eclipse.lsp4jakarta.jdt.internal.core.ls;

import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.*;

import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getBoolean;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getCodeActionContext;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getFirst;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getObject;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getPosition;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getRange;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getString;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getStringList;
import static org.eclipse.lsp4jakarta.jdt.internal.core.ls.ArgumentUtils.getTextDocumentIdentifier;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;
import org.eclipse.jdt.ls.core.internal.JavaLanguageServerPlugin;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionContext;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCodeActionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaCompletionResult;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams;
import org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsSettings;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfo;
import org.eclipse.lsp4jakarta.commons.JakartaJavaFileInfoParams;
import org.eclipse.lsp4jakarta.commons.JavaCursorContextResult;
import org.eclipse.lsp4jakarta.commons.codeaction.CodeActionResolveData;
import org.eclipse.lsp4jakarta.commons.utils.JSONUtility;
import org.eclipse.lsp4jakarta.jdt.core.PropertiesManagerForJava;

/**
 * Delegate Command Handler for LSP4Jakarta JDT LS extension commands
 */
public class JakartaDelegateCommandHandlerForJava extends AbstractJakartaDelegateCommandHandler {

    private static final String FILE_INFO_COMMAND_ID = "jakarta/java/fileInfo";
    private static final String JAVA_CODEACTION_COMMAND_ID = "jakarta/java/codeAction";
    private static final String JAVA_CODEACTION_RESOLVE_COMMAND_ID = "jakarta/java/codeActionResolve";
    private static final String JAVA_COMPLETION_COMMAND_ID = "jakarta/java/completion";
    private static final String JAVA_DIAGNOSTICS_COMMAND_ID = "jakarta/java/diagnostics";

    public JakartaDelegateCommandHandlerForJava() {}

    /**
     * Return the result for the given commandId
     *
     * @param commandId String name of command message
     * @param arguments request data from the Jakarta LS
     * @param monitor
     * @return the resulting response object for the given commandId based on the data in argument map
     * @throws Exception
     */
    @Override
    public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {
        JavaLanguageServerPlugin.logInfo(String.format("Executing command '%s' in LSP4Jakarta JDT LS extension", commandId));
        switch (commandId) {
            case FILE_INFO_COMMAND_ID:
                return getFileInfo(arguments, commandId);
            case JAVA_CODEACTION_COMMAND_ID:
                return getCodeActionForJava(arguments, commandId, monitor);
            case JAVA_CODEACTION_RESOLVE_COMMAND_ID:
                return resolveCodeActionForJava(arguments, commandId, monitor);
            case JAVA_COMPLETION_COMMAND_ID:
                return getCompletionForJava(arguments, commandId, monitor);
            case JAVA_DIAGNOSTICS_COMMAND_ID:
                return getDiagnosticsForJava(arguments, commandId, monitor);
            default:
                throw new UnsupportedOperationException(String.format("Unsupported command '%s'!", commandId));
        }
    }

    /**
     * Returns the file information (package name, etc) for the given Java file.
     *
     * @param arguments
     * @param commandId
     * @param monitor
     * @return the file information (package name, etc) for the given Java file.
     * @throws CoreException
     * @throws JavaModelException
     */
    private static JakartaJavaFileInfo getFileInfo(List<Object> arguments, String commandId) throws JavaModelException, CoreException {
        // Create java file information parameter
        JakartaJavaFileInfoParams params = createJavaFileInfoParams(arguments, commandId);
        // Return file information from the parameter
        return PropertiesManagerForJava.getInstance().fileInfo(params, JDTUtilsLSImpl.getInstance());
    }

    /**
     * Create the Java file information parameter from the given arguments map.
     *
     * @param arguments
     * @param commandId
     *
     * @return the Java file information parameter.
     */
    private static JakartaJavaFileInfoParams createJavaFileInfoParams(List<Object> arguments, String commandId) {
        Map<String, Object> obj = getFirst(arguments);
        if (obj == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with one JakartaJavaFileInfoParams argument!", commandId));
        }
        // Get project name from the java file URI
        String javaFileUri = getString(obj, "uri");
        if (javaFileUri == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with required JakartaJavaFileInfoParams.uri (java file URI)!",
                                                                  commandId));
        }
        JakartaJavaFileInfoParams params = new JakartaJavaFileInfoParams();
        params.setUri(javaFileUri);
        return params;
    }

    /**
     * Return the completion result for the given arguments
     *
     * @param arguments Map of completion request data from Jakarta LS
     * @param commandId String name of command message
     * @param monitor
     * @return the completion result for the given arguments
     * @throws JavaModelException
     * @throws CoreException
     */
    private static JakartaJavaCompletionResult getCompletionForJava(List<Object> arguments, String commandId,
                                                                    IProgressMonitor monitor) throws JavaModelException, CoreException {
        JakartaJavaCompletionParams params = createJakartaJavaCompletionParams(arguments, commandId);
        CompletionList completionList = PropertiesManagerForJava.getInstance().completion(params,
                                                                                          JDTUtilsLSImpl.getInstance(), monitor);
        JavaCursorContextResult cursorContext = PropertiesManagerForJava.getInstance().javaCursorContext(params,
                                                                                                         JDTUtilsLSImpl.getInstance(), monitor);
        return new JakartaJavaCompletionResult(completionList, cursorContext);
    }

    /**
     * Create the completion parameters from the given argument map
     *
     * @param arguments Map of completion data from Jakarta LS
     * @param commandId String name of command message
     * @return the completion results parameter object based on the given argument map
     */
    private static JakartaJavaCompletionParams createJakartaJavaCompletionParams(List<Object> arguments,
                                                                                 String commandId) {
        Map<String, Object> obj = getFirst(arguments);
        if (obj == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with one JakartaJavaCompletionParams argument!", commandId));
        }
        String javaFileUri = getString(obj, "uri");
        if (javaFileUri == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with required JakartaJavaCompletionParams.uri (java URI)!",
                                                                  commandId));
        }
        Position position = getPosition(obj, "position");
        if (position == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with required JakartaJavaCompletionParams.position (completion trigger location)!",
                                                                  commandId));
        }
        JakartaJavaCompletionParams params = new JakartaJavaCompletionParams(javaFileUri, position);
        return params;
    }

    /**
     * Returns the code action for the given Java file.
     *
     * @param arguments Map of CodeAction data from Jakarta LS
     * @param commandId String name of command message
     * @param monitor
     * @return the code action for the given Java file.
     * @throws CoreException
     * @throws JavaModelException
     */
    private static List<? extends CodeAction> getCodeActionForJava(List<Object> arguments, String commandId,
                                                                   IProgressMonitor monitor) throws JavaModelException, CoreException {
        // Create java code action parameter
        JakartaJavaCodeActionParams params = createJakartaJavaCodeActionParams(arguments, commandId);
        // Return code action from the code action parameter
        return PropertiesManagerForJava.getInstance().codeAction(params, JDTUtilsLSImpl.getInstance(), monitor);
    }

    /**
     * Create java code action parameter from the given arguments map.
     *
     * @param arguments Map of code action data from Jakarta LS
     * @param commandId String name of command message
     *
     * @return java code action parameter
     */
    private static JakartaJavaCodeActionParams createJakartaJavaCodeActionParams(List<Object> arguments,
                                                                                 String commandId) {
        Map<String, Object> obj = getFirst(arguments);
        if (obj == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with one JakartaJavaCodeActionParams argument!", commandId));
        }
        TextDocumentIdentifier texdDocumentIdentifier = getTextDocumentIdentifier(obj, "textDocument");
        if (texdDocumentIdentifier == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with required JakartaJavaCodeActionParams.texdDocumentIdentifier",
                                                                  commandId));
        }
        Range range = getRange(obj, "range");
        CodeActionContext context = getCodeActionContext(obj, "context");
        boolean resourceOperationSupported = getBoolean(obj, "resourceOperationSupported");
        boolean commandConfigurationUpdateSupported = getBoolean(obj, "commandConfigurationUpdateSupported");
        boolean resolveSupported = getBoolean(obj, "resolveSupported");
        JakartaJavaCodeActionParams params = new JakartaJavaCodeActionParams();
        params.setTextDocument(texdDocumentIdentifier);
        params.setRange(range);
        params.setContext(context);
        params.setResourceOperationSupported(resourceOperationSupported);
        params.setCommandConfigurationUpdateSupported(commandConfigurationUpdateSupported);
        params.setResolveSupported(resolveSupported);
        return params;
    }

    /**
     * Return the resolveCodeAction result for the given arguments
     *
     * @param arguments Map of code action data from Jakarta LS
     * @param commandId String name of command message
     * @param monitor
     * @return the resolved CodeAction result for the given map of arguments
     * @throws JavaModelException
     * @throws CoreException
     */
    private static CodeAction resolveCodeActionForJava(List<Object> arguments, String commandId,
                                                       IProgressMonitor monitor) throws JavaModelException, CoreException {
        // Create java code action parameter
        CodeAction unresolved = createJakartaJavaCodeActionResolveParams(arguments, commandId);
        // Return code action from the code action parameter
        return PropertiesManagerForJava.getInstance().resolveCodeAction(unresolved, JDTUtilsLSImpl.getInstance(),
                                                                        monitor);
    }

    /**
     * Create java resolve code action parameter from the given arguments map.
     *
     * @param arguments Map of code action data from Jakarta LS
     * @param commandId String name of command message
     *
     * @return java resolved code action parameter
     */
    private static CodeAction createJakartaJavaCodeActionResolveParams(List<Object> arguments, String commandId) {
        Map<String, Object> obj = getFirst(arguments);
        if (obj == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with one CodeAction argument!", commandId));
        }
        CodeAction codeAction = JSONUtility.toModel(obj, CodeAction.class);
        if (codeAction == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with one CodeAction argument!", commandId));
        }
        CodeActionResolveData resolveData = JSONUtility.toModel(codeAction.getData(), CodeActionResolveData.class);
        if (resolveData == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with a CodeAction that has CodeActionResolveData!", commandId));
        }
        codeAction.setData(resolveData);
        return codeAction;
    }

    /**
     * Returns the publish diagnostics list for a given java file URIs in arguments map
     *
     * @param arguments map of code action data from Jakarta LS
     * @param monitor
     * @return list of diagnostics as
     *         List<PublishDiagnosticsParams>>
     */
    private static List<PublishDiagnosticsParams> getDiagnosticsForJava(List<Object> arguments, String commandId,
                                                                        IProgressMonitor monitor) throws JavaModelException {
        // Create java diagnostics parameter
        JakartaJavaDiagnosticsParams params = createJakartaJavaDiagnosticsParams(arguments, commandId);
        // Return diagnostics from parameter
        return PropertiesManagerForJava.getInstance().diagnostics(params, JDTUtilsLSImpl.getInstance(), monitor);
    }

    /**
     * Returns the java diagnostics parameters from the given arguments map.
     *
     * @param arguments JakartaJavaDiagnosticsParams @see
     *            org.eclipse.lsp4jakarta.commons.JakartaJavaDiagnosticsParams
     * @param commandId String name of command message
     *
     * @return the java diagnostics parameters
     */
    private static JakartaJavaDiagnosticsParams createJakartaJavaDiagnosticsParams(List<Object> arguments,
                                                                                   String commandId) {
        Map<String, Object> obj = getFirst(arguments);
        if (obj == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with one JakartaJavaDiagnosticsParams argument!", commandId));
        }
        List<String> javaFileUri = getStringList(obj, "uris");
        if (javaFileUri == null) {
            throw new UnsupportedOperationException(String.format(
                                                                  "Command '%s' must be called with required JakartaJavaDiagnosticsParams.uri (java URIs)!",
                                                                  commandId));
        }
        JakartaJavaDiagnosticsSettings settings = null;
        Map<String, Object> settingsObj = getObject(obj, "settings");
        if (settingsObj != null) {
            List<String> patterns = getStringList(settingsObj, "patterns");
            settings = new JakartaJavaDiagnosticsSettings(patterns);
            settings.setSelectedVersion(getString(settingsObj, "selectedVersion"));
            settings.setAvailableVersions(getStringList(settingsObj, "availableVersions"));
        }
        return new JakartaJavaDiagnosticsParams(javaFileUri, settings);
    }
}
